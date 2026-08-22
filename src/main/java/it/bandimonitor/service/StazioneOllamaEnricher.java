package it.bandimonitor.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StazioneOllamaEnricher {

    private final StazioneClassificatoreService classificatoreService;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private static final int BATCH = 15;

    // Tipi che per definizione appartengono ad enti nazionali — non vanno reclassificati
    private static final java.util.Set<String> TIPI_NAZIONALI = java.util.Set.of(
            "Ministero/Governo", "Ente Previdenziale", "CONSIP",
            "Forze Armate/Sicurezza", "Giustizia", "Autorità"
    );

    /**
     * Chiama Hermes per classificare le stazioni che il keyword classifier
     * non è riuscito a classificare completamente. Non blocca il thread chiamante.
     */
    @Async
    public void enrichAsync(List<String> stazioni) {
        if (!running.compareAndSet(false, true)) {
            log.debug("StazioneOllamaEnricher già in esecuzione, skip");
            return;
        }
        try {
            List<String> daClassificare = stazioni.stream()
                    .filter(s -> {
                        StazioneClasse c = classificatoreService.classifica(s, null);
                        if (c.tipo() == null) return true;
                        if ("Non classificato".equals(c.regione())) return true;
                        // "Nazionale" con tipo non veramente nazionale = ANAC ha sbagliato regione
                        if ("Nazionale".equals(c.regione()) && !TIPI_NAZIONALI.contains(c.tipo())) return true;
                        return false;
                    })
                    .collect(Collectors.toList());

            if (daClassificare.isEmpty()) return;
            log.info("StazioneOllamaEnricher: {} stazioni da classificare con Ollama", daClassificare.size());

            ChatClient chatClient = chatClientBuilder.build();
            for (int i = 0; i < daClassificare.size(); i += BATCH) {
                List<String> batch = daClassificare.subList(i, Math.min(i + BATCH, daClassificare.size()));
                classificaBatch(chatClient, batch);
            }
        } catch (Exception e) {
            log.warn("StazioneOllamaEnricher errore generale: {}", e.getMessage());
        } finally {
            running.set(false);
        }
    }

    private void classificaBatch(ChatClient chatClient, List<String> batch) {
        String lista = batch.stream().map(s -> "- " + s).collect(Collectors.joining("\n"));
        String prompt = """
                Sei un esperto di pubblica amministrazione italiana.
                Per ciascuna stazione appaltante in lista indica regione e tipo di ente.

                Tipi disponibili (scegli SOLO da questa lista):
                Comune, Provincia, Regione, Università, Sanità, Ministero/Governo,
                Ente Previdenziale, Camera di Commercio, Istruzione, Forze Armate/Sicurezza,
                Giustizia, CONSIP, Autorità, Ente Pubblico Locale, Ente Pubblico, Altro

                Regioni: usa il nome standard della regione italiana (es. "Lombardia", "Veneto", "Sicilia").
                Se l'ente è a livello nazionale (Ministeri, INPS, CONSIP, ecc.) scrivi "Nazionale".

                Rispondi SOLO con un JSON array, zero testo aggiuntivo:
                [{"s":"nome stazione","r":"regione","t":"tipo"},...]

                Stazioni:
                """ + lista;
        try {
            String response = chatClient.prompt(prompt).call().content();
            parseAndUpdateCache(response, batch);
        } catch (Exception e) {
            log.warn("Ollama batch classification fallita: {}", e.getMessage());
        }
    }

    private static final Pattern JSON_ARRAY = Pattern.compile("\\[.*?]", Pattern.DOTALL);

    private void parseAndUpdateCache(String response, List<String> batch) {
        try {
            String json = response;
            Matcher m = JSON_ARRAY.matcher(response);
            if (m.find()) json = m.group();
            JsonNode arr = objectMapper.readTree(json);
            if (!arr.isArray()) return;
            for (JsonNode node : arr) {
                String s = node.has("s") ? node.get("s").asText() : null;
                String r = node.has("r") ? node.get("r").asText() : null;
                String t = node.has("t") ? node.get("t").asText() : null;
                if (s != null) classificatoreService.aggiornaCacheOllama(s, r, t);
            }
            log.debug("Ollama classificate {} stazioni", arr.size());
        } catch (Exception e) {
            log.warn("Errore parsing risposta Ollama: {}", e.getMessage());
        }
    }
}
