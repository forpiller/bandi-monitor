package it.bandimonitor.service;

import it.bandimonitor.domain.BandoCreateCommand;
import it.bandimonitor.domain.sintel.SintelRecord;
import it.bandimonitor.entity.EnteIpa;
import it.bandimonitor.entity.TipoDataset;
import it.bandimonitor.repository.EnteIpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SintelImportService {

    private static final String BASE_URL = "https://www.dati.lombardia.it/resource/8txy-zjw2.json";
    private static final int PAGE_SIZE = 1000;

    private final BandoService bandoService;
    private final FonteService fonteService;
    private final BandoPersistService persistService;
    private final EnteIpaRepository enteIpaRepository;

    private final RestClient restClient = RestClient.create();

    public int importa() {
        log.info("Avvio importazione bandi Sintel Lombardia");

        bandoService.deleteByTipoDatasetIn(List.of(TipoDataset.SINTEL_LOMBARDIA));
        log.info("Dati Sintel precedenti eliminati");

        Long fonteId = fonteService.getFonteId("Sintel Lombardia");

        int offset = 0;
        int totaleSalvati = 0;
        List<SintelRecord> pagina;
        do {
            pagina = scaricaPagina(offset);
            if (pagina == null || pagina.isEmpty()) break;

            List<BandoCreateCommand> comandi = pagina.stream()
                    .filter(r -> r.getIdProcedura() != null && !r.getIdProcedura().isBlank())
                    .map(r -> mappa(r, fonteId))
                    .collect(Collectors.toMap(
                            BandoCreateCommand::getCodiceEsterno,
                            cmd -> cmd,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ))
                    .values().stream()
                    .collect(Collectors.toList());

            totaleSalvati += persistService.salvaBatch(comandi);
            offset += PAGE_SIZE;
            log.info("Sintel: pagina offset={} — {} record processati finora", offset, totaleSalvati);
        } while (pagina.size() == PAGE_SIZE);

        fonteService.aggiornaUltimaSincronizzazione("Sintel Lombardia", LocalDateTime.now());
        log.info("Importazione Sintel completata: {} record salvati", totaleSalvati);
        return totaleSalvati;
    }

    private List<SintelRecord> scaricaPagina(int offset) {
        try {
            return restClient.get()
                    .uri(BASE_URL + "?$limit=" + PAGE_SIZE + "&$offset=" + offset)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SintelRecord>>() {});
        } catch (Exception e) {
            log.error("Errore download Sintel offset={}: {}", offset, e.getMessage());
            return List.of();
        }
    }

    private BandoCreateCommand mappa(SintelRecord r, Long fonteId) {
        boolean haLotto = valorizzato(r.getIdLotto());
        String codiceEsterno = "SINTEL-" + (haLotto ? r.getIdLotto() : r.getIdProcedura());

        String titolo = r.getNomeProcedura() != null ? r.getNomeProcedura() : "";
        if ("SI".equalsIgnoreCase(r.getMultilotto()) && valorizzato(r.getNomeLotto())) {
            titolo = titolo + " - " + r.getNomeLotto();
        }
        if (titolo.length() > 900) titolo = titolo.substring(0, 900);

        String statoGara = valorizzato(r.getStatoLotto()) ? r.getStatoLotto() : r.getStatoProcedura();
        BigDecimal importo = parseImporto(r.getBaseAstaLotto());
        if (importo == null) importo = parseImporto(r.getValoreEconomicoProcedura());

        String url = r.getLinkLotto() != null && valorizzato(r.getLinkLotto().getUrl())
                ? r.getLinkLotto().getUrl()
                : (r.getLinkProcedura() != null ? r.getLinkProcedura().getUrl() : null);

        LocalDate dataPubblicazione = parseData(r.getDataInizioNegoziazione());

        var builder = BandoCreateCommand.builder()
                .codiceEsterno(codiceEsterno)
                .titolo(titolo)
                .stazioneAppaltante(tronca(r.getStazioneAppaltante(), 500))
                .importoBase(importo)
                .dataPubblicazione(dataPubblicazione)
                .dataScadenza(parseData(r.getDataFineNegoziazione()))
                .fonteId(fonteId)
                .tipoDataset(TipoDataset.SINTEL_LOMBARDIA)
                .statoGara(tronca(statoGara, 200))
                .categoriaGara(tronca(r.getCategoriaMerceologiche(), 200))
                .codCpv(r.getCodiceCpv())
                .tipoProcedura(tronca(r.getModalitaSceltaContraente(), 100))
                .annoRiferimento(dataPubblicazione != null ? dataPubblicazione.getYear() : null)
                .urlOriginale(tronca(url, 2000));

        arricchisciConIpa(r.getCfStazioneAppaltante(), builder);

        return builder.build();
    }

    /** Se disponibile un'anagrafica IPA per il CF della stazione, popola regione/tipo amministrazione. */
    private void arricchisciConIpa(String cf, BandoCreateCommand.BandoCreateCommandBuilder builder) {
        if (cf == null || cf.isBlank()) return;
        Optional<EnteIpa> ente = enteIpaRepository.findByCf(cf.trim());
        ente.ifPresent(e -> builder
                .regionePa(e.getRegione())
                .tipoAmministrazione(e.getTipologiaAmm()));
    }

    private String tronca(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max);
    }

    private boolean valorizzato(String s) {
        return s != null && !s.isBlank() && !"N/D".equalsIgnoreCase(s.trim());
    }

    private LocalDate parseData(String valore) {
        if (valore == null || valore.length() < 10) return null;
        try {
            return LocalDate.parse(valore.substring(0, 10));
        } catch (Exception e) {
            log.debug("Data Sintel non parsabile: {}", valore);
            return null;
        }
    }

    private BigDecimal parseImporto(String valore) {
        if (valore == null || valore.isBlank()) return null;
        try {
            return new BigDecimal(valore.trim());
        } catch (Exception e) {
            log.debug("Importo Sintel non parsabile: {}", valore);
            return null;
        }
    }
}
