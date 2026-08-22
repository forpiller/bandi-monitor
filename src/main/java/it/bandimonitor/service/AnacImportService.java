package it.bandimonitor.service;

import it.bandimonitor.domain.BandoCreateCommand;
import it.bandimonitor.domain.consip.AnacCigRecord;
import it.bandimonitor.entity.TipoDataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnacImportService {

    private static final String BASE_URL =
            "https://dati.anticorruzione.it/opendata/download/dataset/cig-%d/filesystem/cig_json_%d_%02d.zip";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BandoService bandoService;
    private final FonteService fonteService;
    private final BandoPersistService persistService;
    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.create();

    public void importa(int mesiIndietro) {
        log.info("Avvio importazione ANAC CIG — cerca ultimi {} mesi con dati", mesiIndietro);

        bandoService.deleteByTipoDatasetIn(List.of(TipoDataset.ANAC_CIG));
        log.info("Dati ANAC precedenti eliminati");

        Long fonteId = fonteService.getFonteId("ANAC");

        int mesiConDati = 0;
        int totale = 0;
        int maxTentativi = Math.max(mesiIndietro + 6, 18);
        for (int i = 0; i < maxTentativi && mesiConDati < mesiIndietro; i++) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            int trovati = importaMese(ym.getYear(), ym.getMonthValue(), fonteId);
            if (trovati > 0) {
                mesiConDati++;
                totale += trovati;
            }
        }

        fonteService.aggiornaUltimaSincronizzazione("ANAC", LocalDateTime.now());
        log.info("Importazione ANAC completata: {} record da {} mesi con dati", totale, mesiConDati);
    }

    private int importaMese(int anno, int mese, Long fonteId) {
        String url = String.format(BASE_URL, anno, anno, mese);
        log.info("Download ANAC CIG {}/{}", anno, String.format("%02d", mese));

        byte[] zipBytes;
        try {
            zipBytes = restClient.get()
                    .uri(url)
                    .header("User-Agent", USER_AGENT)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            log.warn("File ANAC {}/{} non disponibile: {}", anno, mese, e.getMessage());
            return 0;
        }

        if (zipBytes == null || zipBytes.length == 0) {
            log.warn("File ANAC {}/{} vuoto", anno, mese);
            return 0;
        }

        Map<String, BandoCreateCommand> comandiMap = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".json")) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(zis, StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isBlank()) continue;
                        try {
                            AnacCigRecord record = objectMapper.readValue(line, AnacCigRecord.class);
                            if (record.getCig() == null || record.getCig().isBlank()) continue;
                            BandoCreateCommand cmd = mapAnacCig(record, anno, mese, fonteId);
                            comandiMap.putIfAbsent(cmd.getCodiceEsterno(), cmd);
                        } catch (Exception e) {
                            log.debug("Record ANAC non parsabile: {}", e.getMessage());
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Errore parsing ZIP ANAC {}/{}: {}", anno, mese, e.getMessage());
            return 0;
        }

        List<BandoCreateCommand> comandi = new ArrayList<>(comandiMap.values());
        int salvati = persistService.salvaBatch(comandi);
        log.info("ANAC CIG {}/{}: {} record salvati", anno, mese, salvati);
        return salvati;
    }

    private BandoCreateCommand mapAnacCig(AnacCigRecord r, int anno, int mese, Long fonteId) {
        String titolo = r.getOggettoLotto() != null ? r.getOggettoLotto() : r.getOggettoGara();
        if (titolo == null || titolo.isBlank()) titolo = "CIG " + r.getCig();
        if (titolo.length() > 900) titolo = titolo.substring(0, 900);

        return BandoCreateCommand.builder()
                .codiceEsterno("ANAC-" + r.getCig())
                .titolo(titolo)
                .stazioneAppaltante(r.getDenominazioneAmministrazioneAppaltante())
                .importoBase(r.getImportoLotto() != null ? BigDecimal.valueOf(r.getImportoLotto()) : null)
                .dataPubblicazione(parseData(r.getDataPubblicazione()))
                .dataScadenza(parseData(r.getDataScadenzaOfferta()))
                .fonteId(fonteId)
                .tipoDataset(TipoDataset.ANAC_CIG)
                .categoriaGara(r.getDescrizioneCpv())
                .codCpv(r.getCodCpv())
                .descrizioneCpv(r.getDescrizioneCpv())
                .statoGara(buildStatoGara(r.getStato(), r.getEsito()))
                .regionePa(estraiRegione(r.getSezioneRegionale()))
                .tipoStrumento(r.getOggettoPrincipaleContratto())
                .tipoProcedura(r.getTipoSceltaContraente())
                .annoRiferimento(r.getAnnoPubblicazione() != null ? r.getAnnoPubblicazione() : anno)
                .meseRiferimento(mese)
                .build();
    }

    private String buildStatoGara(String stato, String esito) {
        if (esito != null && !esito.isBlank()) return esito;
        return stato;
    }

    private String estraiRegione(String sezioneRegionale) {
        if (sezioneRegionale == null) return null;
        return sezioneRegionale.replace("SEZIONE REGIONALE ", "").trim();
    }

    private LocalDate parseData(String valore) {
        if (valore == null || valore.isBlank()) return null;
        try {
            return LocalDate.parse(valore.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
