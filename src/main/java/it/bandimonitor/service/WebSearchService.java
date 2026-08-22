package it.bandimonitor.service;

import it.bandimonitor.domain.WebSearchResult;
import it.bandimonitor.domain.WebSearchResult.SearchLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WebSearchService {

    private final RestClient restClient;
    private final ChatClient chatClient;

    @Value("${bandi.searxng.url:http://localhost:8888}")
    private String searxngUrl;

    public WebSearchService(RestClient.Builder restClientBuilder, ChatClient.Builder chatClientBuilder) {
        this.restClient = restClientBuilder.build();
        this.chatClient = chatClientBuilder.build();
    }

    public WebSearchResult cerca(String titolo, String codiceEsterno, String fonte) {
        // Query 1: site-specific (consip.it o anticorruzione.it)
        String siteQuery = buildSiteQuery(titolo, fonte);
        // Query 2: ricerca generale con codice se disponibile
        String genericQuery = buildGenericQuery(titolo, codiceEsterno, fonte);

        log.info("Web search site: '{}'", siteQuery);
        log.info("Web search generic: '{}'", genericQuery);

        List<SearchLink> siteResults = searchSearxng(siteQuery);
        // I risultati del sito ufficiale sono già rilevanti per definizione
        List<SearchLink> siteFiltered = siteResults.stream()
                .filter(l -> isFromOfficialSite(l.url(), fonte))
                .limit(5)
                .toList();

        // Se il sito ufficiale non ha dato niente, prova la ricerca generica
        List<SearchLink> genericFiltered = List.of();
        if (siteFiltered.isEmpty()) {
            List<SearchLink> raw = searchSearxng(genericQuery);
            genericFiltered = filterRelevant(raw, titolo, codiceEsterno);
        }

        // Unisci: prima i risultati del sito ufficiale, poi quelli generici
        List<SearchLink> links = new java.util.ArrayList<>(siteFiltered);
        genericFiltered.forEach(l -> {
            if (links.stream().noneMatch(x -> x.url().equals(l.url()))) links.add(l);
        });

        if (links.isEmpty()) {
            return new WebSearchResult(
                    "🔍 Non ho trovato pagine specifiche su questo bando. Prova a cercarlo direttamente su Google con il titolo completo.",
                    List.of(), false, null);
        }

        String summary = generateSummary(titolo, codiceEsterno, links);
        return new WebSearchResult(summary, links, true, null);
    }

    private String buildSiteQuery(String titolo, String fonte) {
        String site = getSite(fonte);
        // Prendi le prime parole significative del titolo
        String keywords = Arrays.stream(titolo.split("\\s+"))
                .filter(w -> w.length() > 3)
                .limit(6)
                .collect(Collectors.joining(" "));
        return site != null ? "site:" + site + " " + keywords : keywords;
    }

    private String buildGenericQuery(String titolo, String codiceEsterno, String fonte) {
        String[] words = titolo.split("\\s+");
        String significative = Arrays.stream(words)
                .filter(w -> w.length() > 4)
                .limit(5)
                .collect(Collectors.joining(" "));
        if (codiceEsterno != null && !codiceEsterno.isBlank()) {
            return "\"" + codiceEsterno + "\" " + significative;
        }
        String quoted = Arrays.stream(words).limit(7).collect(Collectors.joining(" "));
        return "\"" + quoted + "\" " + (fonte != null ? fonte.toLowerCase() : "gara appalto");
    }

    private String getSite(String fonte) {
        if (fonte == null) return null;
        return switch (fonte.toLowerCase()) {
            case "consip" -> "consip.it";
            case "anac"   -> "anticorruzione.it";
            default       -> null;
        };
    }

    private boolean isFromOfficialSite(String url, String fonte) {
        String site = getSite(fonte);
        return site != null && url.contains(site);
    }

    public String chat(String domanda, String titoloBando, String codiceBando, String contestoRicerca) {
        String prompt = """
                Sei Bandi-do, assistente esperto in bandi e gare d'appalto italiani.
                Stai aiutando l'utente riguardo al bando: "%s" (codice: %s).

                %s

                L'utente chiede: "%s"

                Rispondi in italiano in modo diretto e utile. Usa 1-2 emoji pertinenti. Niente saluti o formule di chiusura.
                """.formatted(
                titoloBando,
                codiceBando != null ? codiceBando : "N/D",
                contestoRicerca != null ? "Contesto dalla ricerca web:\n" + contestoRicerca : "",
                domanda
        );

        try {
            String result = chatClient.prompt(prompt).call().content();
            return result != null ? result.trim() : "Mi dispiace, non riesco a rispondere al momento.";
        } catch (Exception e) {
            log.warn("Hermes non disponibile per chat: {}", e.getMessage());
            return "⚠️ Hermes non raggiungibile al momento.";
        }
    }

    // Kept for reference, replaced by buildSiteQuery + buildGenericQuery
    @SuppressWarnings("unused")
    private String buildQuery(String titolo, String codiceEsterno, String fonte) {
        // Usa le parole più significative del titolo in virgolette per ricerca esatta
        String[] words = titolo.split("\\s+");
        String significative = Arrays.stream(words)
                .filter(w -> w.length() > 4)
                .limit(5)
                .collect(Collectors.joining(" "));

        if (codiceEsterno != null && !codiceEsterno.isBlank()) {
            // Codice tra virgolette = ricerca esatta
            return "\"" + codiceEsterno + "\" " + significative;
        }
        // Senza codice: prime parole significative tra virgolette + fonte
        String quoted = Arrays.stream(words).limit(7).collect(Collectors.joining(" "));
        return "\"" + quoted + "\" " + (fonte != null ? fonte.toLowerCase() : "gara appalto");
    }

    // Filtra i risultati che parlano specificamente di questo bando
    private List<SearchLink> filterRelevant(List<SearchLink> results, String titolo, String codiceEsterno) {
        if (results.isEmpty()) return results;

        // Parole chiave significative del titolo (> 4 lettere)
        Set<String> titleWords = Arrays.stream(titolo.split("\\s+"))
                .filter(w -> w.length() > 4)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Codice esterno pulito (es. rimuovi prefisso "ANAC-", "BG-2026-")
        String codiceClean = codiceEsterno != null
                ? codiceEsterno.replaceAll("^(ANAC-|BG-\\d{4}-)", "").toLowerCase()
                : null;

        return results.stream()
                .filter(link -> isRelevant(link, titleWords, codiceClean, codiceEsterno))
                .limit(6)
                .toList();
    }

    private boolean isRelevant(SearchLink link, Set<String> titleWords, String codiceClean, String codiceEsterno) {
        String combined = (link.title() + " " + link.snippet() + " " + link.url()).toLowerCase();

        // Match diretto sul codice (massima rilevanza)
        if (codiceClean != null && combined.contains(codiceClean)) return true;
        if (codiceEsterno != null && combined.contains(codiceEsterno.toLowerCase())) return true;

        // Conta quante parole significative del titolo compaiono nel risultato
        long matchCount = titleWords.stream().filter(combined::contains).count();
        return matchCount >= Math.min(3, titleWords.size());
    }

    @SuppressWarnings("unchecked")
    private List<SearchLink> searchSearxng(String query) {
        try {
            String url = searxngUrl + "/search?q={q}&format=json&language=it-IT&categories=general";
            Map<String, Object> response = restClient.get()
                    .uri(url, query)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(Map.class);

            if (response == null) return List.of();
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null) return List.of();

            return results.stream()
                    .limit(20) // prendi più risultati per poi filtrare
                    .map(r -> new SearchLink(
                            safeStr(r.get("title")),
                            safeStr(r.get("url")),
                            safeStr(r.get("content")),
                            safeStr(r.get("engine"))
                    ))
                    .filter(l -> !l.url().isBlank())
                    .toList();

        } catch (Exception e) {
            log.warn("SearXNG non raggiungibile: {}", e.getMessage());
            return List.of();
        }
    }

    private String generateSummary(String titolo, String codice, List<SearchLink> links) {
        String context = links.stream()
                .map(l -> "- [" + l.engine() + "] " + l.title() + ": " + l.snippet())
                .collect(Collectors.joining("\n"));

        String prompt = """
                Sei Bandi-do, assistente esperto in bandi e gare d'appalto italiani.

                Bando cercato: "%s" (codice: %s)

                Risultati web trovati specificamente su questo bando:
                %s

                Scrivi 3-4 frasi in italiano che riassumono cosa rivelano questi risultati su questo specifico bando.
                Menziona documenti ufficiali, scadenze o informazioni concrete se presenti.
                Usa 2-3 emoji pertinenti. Vai diretto al punto, niente saluti.
                """.formatted(titolo, codice != null ? codice : "N/D", context);

        try {
            String result = chatClient.prompt(prompt).call().content();
            return result != null ? result.trim() : null;
        } catch (Exception e) {
            log.warn("Hermes non disponibile per summary web: {}", e.getMessage());
            return null;
        }
    }

    private String safeStr(Object o) {
        return o != null ? o.toString() : "";
    }
}
