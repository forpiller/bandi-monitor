package it.bandimonitor.service;

import it.bandimonitor.domain.BandoCreateCommand;
import it.bandimonitor.domain.consip.BandiGareRecord;
import it.bandimonitor.domain.consip.GareAspRecord;
import it.bandimonitor.entity.TipoDataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsipImportService {

    private final BandoService bandoService;
    private final FonteService fonteService;
    private final BandoPersistService persistService;

    private final RestClient restClient = RestClient.create();

    @Value("${bandi.import.anni:2025,2026}")
    private String anniConfig;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Scheduled(cron = "${bandi.import.cron:0 0 1 1 * *}")
    public void importa() {
        log.info("Avvio importazione bandi da dati.consip.it");

        bandoService.deleteByTipoDatasetIn(List.of(TipoDataset.GARE_ASP, TipoDataset.BANDI_GARE));
        log.info("Dati Consip precedenti eliminati");

        Long fonteId = fonteService.getFonteId("Consip");

        List<Integer> anni = Arrays.stream(anniConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        for (Integer anno : anni) {
            log.info("Importazione anno {}", anno);
            importaGareAsp(anno, fonteId);
            importaBandiGare(anno, fonteId);
        }

        fonteService.aggiornaUltimaSincronizzazione("Consip", LocalDateTime.now());
        log.info("Importazione completata per anni: {}", anni);
    }

    private void importaGareAsp(int anno, Long fonteId) {
        String url = "https://dati.consip.it/download/dataset/gareasp" + anno + ".json";
        log.info("Download GARE ASP {}: {}", anno, url);

        List<GareAspRecord> records;
        try {
            records = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GareAspRecord>>() {});
        } catch (Exception e) {
            log.error("Errore download GARE ASP {}: {}", anno, e.getMessage());
            return;
        }

        if (records == null || records.isEmpty()) {
            log.warn("Nessun record GARE ASP per anno {}", anno);
            return;
        }

        List<BandoCreateCommand> comandi = records.stream()
                .map(r -> mapGareAsp(r, anno, fonteId))
                .collect(Collectors.toMap(
                        BandoCreateCommand::getCodiceEsterno,
                        cmd -> cmd,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ))
                .values().stream()
                .collect(Collectors.toList());

        int salvati = persistService.salvaBatch(comandi);
        log.info("GARE ASP {}: {} record salvati su {} totali (deduplicati)", anno, salvati, records.size());
    }

    private BandoCreateCommand mapGareAsp(GareAspRecord r, int anno, Long fonteId) {
        String titolo = r.getDenominazioneGara() != null ? r.getDenominazioneGara() : "";
        if (titolo.length() > 900) titolo = titolo.substring(0, 900);

        int annoRif = anno;
        if (r.getAnnoRiferimento() != null && !r.getAnnoRiferimento().isBlank()) {
            try {
                annoRif = Integer.parseInt(r.getAnnoRiferimento().trim());
            } catch (NumberFormatException ignored) {
            }
        }

        return BandoCreateCommand.builder()
                .codiceEsterno("ASP-" + anno + "-" + r.getIdentificativoLotto())
                .titolo(titolo)
                .descrizione(r.getDenominazioneLotto())
                .stazioneAppaltante(r.getDenominazioneAmministrazione())
                .denominazioneAmministrazione(r.getDenominazioneAmministrazione())
                .importoBase(parseImporto(r.getBaseAsta()))
                .dataPubblicazione(parseData(r.getDataPubblicazione()))
                .fonteId(fonteId)
                .tipoDataset(TipoDataset.GARE_ASP)
                .categoriaGara(r.getCategoriaMerceologica())
                .statoGara(r.getStatoGara())
                .tipoAmministrazione(r.getTipologiaAmministrazione())
                .regionePa(r.getRegionePa())
                .provinciaPa(r.getProvinciaPa())
                .tipoProcedura(r.getTipoProcedura())
                .annoRiferimento(annoRif)
                .oggettoAppalto(r.getOggettoAppalto())
                .criterioAggiudicazione(r.getCriterioAggiudicazione())
                .valoreAggiudicato(parseImporto(r.getValoreAggiudicato()))
                .comparto(r.getComparto())
                .statoLotto(r.getStatoLotto())
                .siglaProvincia(r.getSiglaProvincia())
                .latitudinePa(r.getLatitudinePa())
                .longitudinePa(r.getLongitudinePa())
                .build();
    }

    private void importaBandiGare(int anno, Long fonteId) {
        String url = "https://dati.consip.it/download/dataset/bandiegare" + anno + ".json";
        log.info("Download BANDI E GARE {}: {}", anno, url);

        List<BandiGareRecord> records;
        try {
            records = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BandiGareRecord>>() {});
        } catch (Exception e) {
            log.error("Errore download BANDI E GARE {}: {}", anno, e.getMessage());
            return;
        }

        if (records == null || records.isEmpty()) {
            log.warn("Nessun record BANDI E GARE per anno {}", anno);
            return;
        }

        List<BandoCreateCommand> comandi = records.stream()
                .map(r -> mapBandiGare(r, anno, fonteId))
                .collect(Collectors.toMap(
                        BandoCreateCommand::getCodiceEsterno,
                        cmd -> cmd,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ))
                .values().stream()
                .collect(Collectors.toList());

        int salvati = persistService.salvaBatch(comandi);
        log.info("BANDI E GARE {}: {} record salvati su {} totali (deduplicati)", anno, salvati, records.size());
    }

    private BandoCreateCommand mapBandiGare(BandiGareRecord r, int anno, Long fonteId) {
        String denominazioneBando = r.getDenominazioneBando() != null ? r.getDenominazioneBando() : "";
        String denominazioneLotto = r.getDenominazioneLotto() != null ? r.getDenominazioneLotto() : "";
        String titolo = denominazioneBando + " - " + denominazioneLotto;
        if (titolo.length() > 900) titolo = titolo.substring(0, 900);

        return BandoCreateCommand.builder()
                .codiceEsterno("BG-" + anno + "-" + r.getIdentificativoLotto())
                .titolo(titolo)
                .stazioneAppaltante("Consip")
                .importoBase(parseImporto(r.getBaseAsta()))
                .dataPubblicazione(parseData(r.getDataPubblicazione()))
                .dataScadenza(parseData(r.getDataTermine()))
                .fonteId(fonteId)
                .tipoDataset(TipoDataset.BANDI_GARE)
                .categoriaGara(r.getCategoriaMerceologica())
                .tipoStrumento(r.getTipoStrumento())
                .tipoProcedura(r.getTipoProcedura())
                .annoRiferimento(anno)
                .build();
    }

    private LocalDate parseData(String valore) {
        if (valore == null || valore.isBlank()) return null;
        try {
            return LocalDate.parse(valore.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.debug("Data non parsabile: {}", valore);
            return null;
        }
    }

    private BigDecimal parseImporto(String valore) {
        if (valore == null || valore.isBlank()) return null;
        try {
            String cleaned = valore.replace(",", ".").replaceAll("[^0-9.]", "");
            if (cleaned.isBlank()) return null;
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            log.debug("Importo non parsabile: {}", valore);
            return null;
        }
    }
}
