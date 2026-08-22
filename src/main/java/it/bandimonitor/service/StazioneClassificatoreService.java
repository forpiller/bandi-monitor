package it.bandimonitor.service;

import it.bandimonitor.entity.StazioneClassificazione;
import it.bandimonitor.repository.StazioneClassificazioneRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class StazioneClassificatoreService {

    private final StazioneClassificazioneRepository classificazioneRepo;

    private final ConcurrentHashMap<String, StazioneClasse> cache = new ConcurrentHashMap<>();

    /** Carica in cache tutte le classificazioni Ollama salvate nel DB. */
    @PostConstruct
    void caricaDaDb() {
        classificazioneRepo.findAll().forEach(r ->
                cache.put(r.getNomeStazione(), new StazioneClasse(r.getRegione(), r.getTipo())));
        log.info("StazioneClassificatoreService: {} classificazioni caricate da DB", cache.size());
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Classifica per keyword (sincrono, istantaneo). regioneAnac può essere null. */
    public StazioneClasse classifica(String stazione, String regioneAnac) {
        return cache.computeIfAbsent(stazione, s -> classificaInterno(s, regioneAnac));
    }

    /** Aggiorna la cache con la classificazione di Ollama e la persiste nel DB. */
    @Transactional
    public void aggiornaCacheOllama(String stazione, String regione, String tipo) {
        cache.merge(stazione, new StazioneClasse(regione, tipo), (old, nuovo) -> {
            // "Nazionale" può essere corretto da Ollama se l'ente non è davvero nazionale
            boolean regioneNota = old.regione() != null
                    && !"Non classificato".equals(old.regione())
                    && !"Nazionale".equals(old.regione());
            String r = regioneNota ? old.regione() : nuovo.regione();
            String t = (old.tipo() != null && !"Altro".equals(old.tipo())) ? old.tipo() : nuovo.tipo();
            return new StazioneClasse(r, t);
        });
        StazioneClasse risultato = cache.get(stazione);
        if (risultato != null) {
            classificazioneRepo.findByNomeStazione(stazione).ifPresentOrElse(
                    row -> {
                        row.setRegione(risultato.regione());
                        row.setTipo(risultato.tipo());
                        classificazioneRepo.save(row);
                    },
                    () -> classificazioneRepo.save(
                            new StazioneClassificazione(stazione, risultato.regione(), risultato.tipo()))
            );
        }
    }

    // -----------------------------------------------------------------------
    // Keyword classification
    // -----------------------------------------------------------------------

    // Valori ANAC che indicano "ente nazionale" — non sono vere regioni
    private static final java.util.Set<String> NON_REGIONI_ANAC = java.util.Set.of(
            "centrale", "nazionale", "national", "centrale/nazionale", ""
    );

    private StazioneClasse classificaInterno(String stazione, String regioneAnac) {
        String up = stazione.toUpperCase().trim();
        String tipo = rilevaTipo(up);

        String regione = null;
        // Usa la regione ANAC solo se è una vera regione italiana (non "CENTRALE" o simili)
        if (regioneAnac != null && !NON_REGIONI_ANAC.contains(regioneAnac.toLowerCase().trim())) {
            regione = normalizeRegione(regioneAnac);
        }
        // Se la regione ANAC è assente o "CENTRALE", prova a ricavarla dal nome
        if (regione == null) {
            regione = rilevaRegione(up);
        }
        // Se ancora non trovata e ANAC dice "CENTRALE", l'ente è nazionale
        if (regione == null) {
            boolean anac_centrale = regioneAnac != null &&
                    NON_REGIONI_ANAC.contains(regioneAnac.toLowerCase().trim()) &&
                    !regioneAnac.isBlank();
            regione = anac_centrale ? "Nazionale" : "Non classificato";
        }
        return new StazioneClasse(regione, tipo != null ? tipo : null);
    }

    private String rilevaTipo(String up) {
        if (matchesAny(up, "COMUNE DI ", "MUNICIPIO DI ", "MUNICIPALITÀ DI ")) return "Comune";
        if (matchesAny(up, "PROVINCIA DI ", "PROVINCIA AUTONOMA DI ", "CITTÀ METROPOLITANA DI ", "CITTA METROPOLITANA DI ")) return "Provincia";
        if (up.startsWith("REGIONE ") || up.startsWith("GIUNTA REGIONALE") || up.startsWith("CONSIGLIO REGIONALE")) return "Regione";
        if (matchesAny(up, "UNIVERSITÀ ", "UNIVERSITA ", "UNIVERSITÀ DEGLI STUDI", "UNIVERSITA DEGLI STUDI",
                "ATENEO ", "POLITECNICO DI ", "POLITECNICO DI", "SCUOLA SUPERIORE ",
                "SCUOLA NORMALE ", "SCUOLA INTERNAZIONALE")) return "Università";
        if (matchesAny(up, "ASL ", "A.S.L.", "AUSL ", "AZIENDA SANITARIA", "AZIENDA U.S.L.",
                "AZIENDA USL", "AZIENDA SOCIO SANITARIA", "AZIENDA OSPEDALIERO UNIVERSITARIA",
                "AZIENDA OSPEDALIERA ", "A.O. ", "A.O.U.", "OSPEDALE ", "POLICLINICO ",
                "IRCCS", "ISTITUTO TUMORI", "ISTITUTO NEUROLOGICO", "ISTITUTO ORTOPEDICO",
                "FONDAZIONE IRCCS", "PRESIDIO OSPEDALIERO", "AZIENDA PROVINCIALE PER I SERVIZI SANITARI")) return "Sanità";
        if (matchesAny(up, "MINISTERO ", "PRESIDENZA DEL CONSIGLIO", "DIPARTIMENTO DELLA RAGIONERIA",
                "AGENZIA DELLE ENTRATE", "AGENZIA DELLE DOGANE", "AGENZIA DEL DEMANIO",
                "CORTE DEI CONTI", "CONSIGLIO DI STATO")) return "Ministero/Governo";
        if (matchesAny(up, "INPS", "INAIL", "INPGI", "INPDAP", "ENPAM", "CIPAG")) return "Ente Previdenziale";
        if (matchesAny(up, "CAMERA DI COMMERCIO", "CCIAA", "UNIONCAMERE")) return "Camera di Commercio";
        if (matchesAny(up, "ISTITUTO COMPRENSIVO", "ISTITUTO SUPERIORE", "LICEO ", "I.C. ",
                "ISTITUTO TECNICO", "ISTITUTO PROFESSIONALE", "SCUOLA PRIMARIA", "SCUOLA MEDIA",
                "SCUOLA ELEMENTARE", "DIREZIONE DIDATTICA")) return "Istruzione";
        if (matchesAny(up, "CARABINIERI", "GUARDIA DI FINANZA", "VIGILI DEL FUOCO", "PREFETTURA",
                "QUESTURA", "POLIZIA DI STATO", "POLIZIA LOCALE", "POLIZIA MUNICIPALE",
                "COMANDO MILITARE", "MARINA MILITARE", "AERONAUTICA MILITARE", "ESERCITO ITALIANO")) return "Forze Armate/Sicurezza";
        if (matchesAny(up, "TRIBUNALE", "PROCURA DELLA REPUBBLICA", "CORTE D'APPELLO",
                "CORTE DI APPELLO", "MAGISTRATURA", "MINISTERO DELLA GIUSTIZIA")) return "Giustizia";
        if (up.contains("CONSIP")) return "CONSIP";
        if (matchesAny(up, "AUTORITÀ PORTUALE", "AUTORITÀ DI SISTEMA PORTUALE",
                "AUTORITÀ GARANTE", "AUTORITÀ NAZIONALE ANTICORRUZIONE", "ANAC",
                "AUTORITÀ PER L'ENERGIA")) return "Autorità";
        if (matchesAny(up, "COMUNITÀ MONTANA", "UNIONE DI COMUNI", "UNIONE DEI COMUNI",
                "CONSORZIO ", "ENTE PARCO", "PARCO NAZIONALE", "PARCO REGIONALE")) return "Ente Pubblico Locale";
        if (matchesAny(up, "FONDAZIONE ", "ISTITUTO ")) return "Ente Pubblico";
        return null;
    }

    private boolean matchesAny(String up, String... prefixes) {
        for (String p : prefixes) {
            if (up.contains(p)) return true;
        }
        return false;
    }

    // Patterns for city extraction
    private static final Pattern PTN_COMUNE = Pattern.compile("COMUNE DI\\s+([A-ZÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖÙÚÛÜ ']+?)(?:\\s+(?:DEL|DELLA|DELLE|DEGLI|DI|E|-)\\s+.+)?$");
    private static final Pattern PTN_PROV   = Pattern.compile("PROVINCIA(?:\\s+AUTONOMA)?\\s+DI\\s+([A-ZÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖÙÚÛÜ ']+?)(?:\\s+(?:DEL|DELLA|DI)\\s+.+)?$");

    private String rilevaRegione(String up) {
        // Direct region name in string
        for (Map.Entry<String, String> e : REGION_KEYWORDS.entrySet()) {
            if (up.contains(e.getKey())) return e.getValue();
        }
        // Try multi-word city match first (es. "REGGIO CALABRIA", "LA SPEZIA")
        for (Map.Entry<String, String> e : CITY_TO_REGION.entrySet()) {
            if (up.contains(e.getKey())) return e.getValue();
        }
        // Extract city from structured patterns
        String city = estraiCitta(up);
        if (city != null) {
            String regione = CITY_TO_REGION.get(city.trim());
            if (regione != null) return regione;
        }
        return null;
    }

    private String estraiCitta(String up) {
        for (Pattern ptn : new Pattern[]{PTN_COMUNE, PTN_PROV}) {
            Matcher m = ptn.matcher(up);
            if (m.find()) return m.group(1).trim();
        }
        // es. "UNIVERSITA DEGLI STUDI DI PADOVA" → "PADOVA"
        int idx = up.lastIndexOf(" DI ");
        if (idx >= 0) {
            String dopo = up.substring(idx + 4).split("[\\s,(]")[0].trim();
            if (!dopo.isEmpty() && dopo.length() > 1) return dopo;
        }
        return null;
    }

    private String normalizeRegione(String r) {
        if (r == null) return null;
        String up = r.toUpperCase().trim();
        for (Map.Entry<String, String> e : REGION_KEYWORDS.entrySet()) {
            if (up.contains(e.getKey())) return e.getValue();
        }
        return toTitleCase(r.trim());
    }

    private static String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(' ');
        }
        return sb.toString().trim();
    }

    // -----------------------------------------------------------------------
    // Static data tables — solo capoluoghi di provincia (~110 città)
    // Tutto il resto viene classificato da Ollama e salvato in DB.
    // -----------------------------------------------------------------------

    private static final Map<String, String> REGION_KEYWORDS = Map.ofEntries(
            Map.entry("PIEMONTE", "Piemonte"),
            Map.entry("VALLE D'AOSTA", "Valle d'Aosta"),
            Map.entry("VALLE DAOSTA", "Valle d'Aosta"),
            Map.entry("LOMBARD", "Lombardia"),
            Map.entry("TRENTINO", "Trentino-Alto Adige"),
            Map.entry("ALTO ADIGE", "Trentino-Alto Adige"),
            Map.entry("SÜDTIROL", "Trentino-Alto Adige"),
            Map.entry("SUDTIROL", "Trentino-Alto Adige"),
            Map.entry("VENETO", "Veneto"),
            Map.entry("FRIULI", "Friuli-Venezia Giulia"),
            Map.entry("VENEZIA GIULIA", "Friuli-Venezia Giulia"),
            Map.entry("ISONTINA", "Friuli-Venezia Giulia"),
            Map.entry("LIGURIA", "Liguria"),
            Map.entry("EMILIA-ROMAGNA", "Emilia-Romagna"),
            Map.entry("EMILIA ROMAGNA", "Emilia-Romagna"),
            Map.entry("TOSCANA", "Toscana"),
            Map.entry("UMBRIA", "Umbria"),
            Map.entry("MARCHE", "Marche"),
            Map.entry("LAZIO", "Lazio"),
            Map.entry("ABRUZZO", "Abruzzo"),
            Map.entry("MOLISE", "Molise"),
            Map.entry("CAMPANIA", "Campania"),
            Map.entry("PUGLIA", "Puglia"),
            Map.entry("BASILICATA", "Basilicata"),
            Map.entry("CALABRIA", "Calabria"),
            Map.entry("SICILIA", "Sicilia"),
            Map.entry("SARDEGNA", "Sardegna"),
            Map.entry("SARDA", "Sardegna")
    );

    private static final Map<String, String> CITY_TO_REGION;

    static {
        CITY_TO_REGION = new ConcurrentHashMap<>();
        // Piemonte
        put("TORINO", "Piemonte"); put("NOVARA", "Piemonte"); put("CUNEO", "Piemonte");
        put("ASTI", "Piemonte"); put("ALESSANDRIA", "Piemonte"); put("BIELLA", "Piemonte");
        put("VERCELLI", "Piemonte"); put("VERBANIA", "Piemonte");
        // Valle d'Aosta
        put("AOSTA", "Valle d'Aosta");
        // Lombardia
        put("MILANO", "Lombardia"); put("BERGAMO", "Lombardia"); put("BRESCIA", "Lombardia");
        put("COMO", "Lombardia"); put("CREMONA", "Lombardia"); put("LECCO", "Lombardia");
        put("LODI", "Lombardia"); put("MANTOVA", "Lombardia"); put("MONZA", "Lombardia");
        put("PAVIA", "Lombardia"); put("SONDRIO", "Lombardia"); put("VARESE", "Lombardia");
        // Trentino-Alto Adige
        put("TRENTO", "Trentino-Alto Adige"); put("BOLZANO", "Trentino-Alto Adige");
        // Veneto
        put("VENEZIA", "Veneto"); put("VERONA", "Veneto"); put("VICENZA", "Veneto");
        put("PADOVA", "Veneto"); put("TREVISO", "Veneto"); put("BELLUNO", "Veneto");
        put("ROVIGO", "Veneto");
        // Friuli-Venezia Giulia
        put("TRIESTE", "Friuli-Venezia Giulia"); put("UDINE", "Friuli-Venezia Giulia");
        put("GORIZIA", "Friuli-Venezia Giulia"); put("PORDENONE", "Friuli-Venezia Giulia");
        // Liguria
        put("GENOVA", "Liguria"); put("SAVONA", "Liguria"); put("IMPERIA", "Liguria");
        put("LA SPEZIA", "Liguria");
        // Emilia-Romagna
        put("BOLOGNA", "Emilia-Romagna"); put("FERRARA", "Emilia-Romagna"); put("MODENA", "Emilia-Romagna");
        put("PARMA", "Emilia-Romagna"); put("PIACENZA", "Emilia-Romagna"); put("RAVENNA", "Emilia-Romagna");
        put("REGGIO EMILIA", "Emilia-Romagna"); put("REGGIO NELL'EMILIA", "Emilia-Romagna");
        put("RIMINI", "Emilia-Romagna"); put("FORLÌ", "Emilia-Romagna"); put("FORLI", "Emilia-Romagna");
        // Toscana
        put("FIRENZE", "Toscana"); put("SIENA", "Toscana"); put("PISA", "Toscana");
        put("LUCCA", "Toscana"); put("AREZZO", "Toscana"); put("GROSSETO", "Toscana");
        put("LIVORNO", "Toscana"); put("MASSA", "Toscana"); put("PRATO", "Toscana");
        put("PISTOIA", "Toscana");
        // Umbria
        put("PERUGIA", "Umbria"); put("TERNI", "Umbria");
        // Marche
        put("ANCONA", "Marche"); put("PESARO", "Marche"); put("MACERATA", "Marche");
        put("ASCOLI PICENO", "Marche"); put("FERMO", "Marche");
        // Lazio
        put("ROMA", "Lazio"); put("FROSINONE", "Lazio"); put("LATINA", "Lazio");
        put("RIETI", "Lazio"); put("VITERBO", "Lazio");
        // Abruzzo
        put("L'AQUILA", "Abruzzo"); put("L AQUILA", "Abruzzo"); put("CHIETI", "Abruzzo");
        put("PESCARA", "Abruzzo"); put("TERAMO", "Abruzzo");
        // Molise
        put("CAMPOBASSO", "Molise"); put("ISERNIA", "Molise");
        // Campania
        put("NAPOLI", "Campania"); put("SALERNO", "Campania"); put("CASERTA", "Campania");
        put("BENEVENTO", "Campania"); put("AVELLINO", "Campania");
        // Puglia
        put("BARI", "Puglia"); put("BRINDISI", "Puglia"); put("LECCE", "Puglia");
        put("TARANTO", "Puglia"); put("FOGGIA", "Puglia"); put("BARLETTA", "Puglia");
        put("ANDRIA", "Puglia"); put("TRANI", "Puglia");
        // Basilicata
        put("POTENZA", "Basilicata"); put("MATERA", "Basilicata");
        // Calabria
        put("CATANZARO", "Calabria"); put("REGGIO CALABRIA", "Calabria"); put("COSENZA", "Calabria");
        put("CROTONE", "Calabria"); put("VIBO VALENTIA", "Calabria");
        // Sicilia
        put("PALERMO", "Sicilia"); put("CATANIA", "Sicilia"); put("MESSINA", "Sicilia");
        put("AGRIGENTO", "Sicilia"); put("CALTANISSETTA", "Sicilia"); put("ENNA", "Sicilia");
        put("RAGUSA", "Sicilia"); put("SIRACUSA", "Sicilia"); put("TRAPANI", "Sicilia");
        // Sardegna
        put("CAGLIARI", "Sardegna"); put("SASSARI", "Sardegna"); put("NUORO", "Sardegna");
        put("ORISTANO", "Sardegna"); put("OLBIA", "Sardegna");
    }

    private static void put(String city, String regione) {
        CITY_TO_REGION.put(city, regione);
    }
}
