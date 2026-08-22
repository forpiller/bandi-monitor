package it.bandimonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiSearchService {

    private final ChatClient chatClient;

    public AiSearchService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String elaboraQuery(String descrizione, String strictness) {
        if (descrizione == null || descrizione.isBlank()) return descrizione;

        String livelloDescrizione = switch (strictness) {
            case "precisa" -> "SOLO i termini specifici e distintivi presenti nella richiesta. NON aggiungere parole generiche come 'bandi', 'gare', 'appalto', 'pubblici', 'regione', 'enti', 'locali', 'servizi'. Solo termini che caratterizzano univocamente l'oggetto.";
            case "ampia"   -> "inclusive: aggiungi sinonimi specifici, categorie merceologiche affini e termini tecnici correlati all'oggetto. Evita parole troppo generiche.";
            default        -> "principali e distintive dell'oggetto cercato, con qualche sinonimo specifico. Evita parole generiche come 'bandi', 'gare', 'appalto'.";
        };

        String prompt = """
                Sei un assistente specializzato nella ricerca di bandi e gare d'appalto pubblici italiani.

                L'utente vuole trovare: "%s"

                Estrai le parole chiave %s
                Le parole chiave saranno usate in una ricerca AND: ogni parola DEVE comparire nel documento trovato.
                Quindi includi SOLO parole che ci si aspetta trovino nel titolo o descrizione del bando cercato.

                Rispondi con SOLO le parole chiave in italiano separate da spazio, senza punteggiatura e senza spiegazioni.
                Massimo 6 parole.
                """.formatted(descrizione.trim(), livelloDescrizione);

        try {
            String result = chatClient.prompt(prompt).call().content();
            if (result == null || result.isBlank()) return descrizione;
            log.info("AI search: '{}' → '{}'", descrizione, result.trim());
            return result.trim();
        } catch (Exception e) {
            log.warn("Hermes non raggiungibile, uso query originale: {}", e.getMessage());
            return descrizione;
        }
    }

    public String generaRisposta(String query, long risultati, java.util.List<String> filtriAttivi) {
        String filtriTesto = (filtriAttivi == null || filtriAttivi.isEmpty())
                ? "nessun filtro aggiuntivo attivo"
                : "filtri attivi nei box: " + String.join(", ", filtriAttivi);

        String prompt = """
                Sei Bandi-do, un assistente simpatico e competente specializzato in bandi e gare d'appalto pubblici italiani.
                Il tuo stile è diretto, professionale ma amichevole, e usi emoji pertinenti (2-4 per risposta) per rendere il testo più vivace.

                L'utente ha cercato: "%s"
                Risultati trovati: %d
                %s

                Scrivi 3-4 frasi in italiano. NON usare saluti, intestazioni o formule di chiusura (niente "Cara utente", "Cordiali saluti" o simili). Vai subito al punto.
                Includi: breve sintesi di cosa ha cercato; commento sul numero di risultati; se ci sono filtri attivi menzionali; eventuale consiglio pratico se i risultati sono pochi.
                Usa emoji naturalmente nel testo, non in modo eccessivo.
                """.formatted(query, risultati, filtriTesto);

        try {
            String result = chatClient.prompt(prompt).call().content();
            if (result == null || result.isBlank()) return null;
            return result.trim();
        } catch (Exception e) {
            log.warn("Hermes non raggiungibile per summary: {}", e.getMessage());
            return null;
        }
    }
}
