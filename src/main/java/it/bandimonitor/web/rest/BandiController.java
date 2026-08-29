package it.bandimonitor.web.rest;

import it.bandimonitor.domain.BandoDto;
import it.bandimonitor.domain.WebSearchResult;
import it.bandimonitor.entity.TipoDataset;
import it.bandimonitor.service.AiSearchService;
import it.bandimonitor.service.StazioneClasse;
import it.bandimonitor.service.StazioneClassificatoreService;
import it.bandimonitor.service.StazioneOllamaEnricher;
import it.bandimonitor.service.AnacImportService;
import it.bandimonitor.service.BandoService;
import it.bandimonitor.service.ConsipImportService;
import it.bandimonitor.service.FonteService;
import it.bandimonitor.service.IpaImportService;
import it.bandimonitor.service.ProfiloService;
import it.bandimonitor.service.SintelImportService;
import it.bandimonitor.service.WebSearchService;
import it.bandimonitor.web.vm.BandiSearchRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BandiController {

    private final BandoService bandoService;
    private final FonteService fonteService;
    private final ConsipImportService importService;
    private final AnacImportService anacImportService;
    private final SintelImportService sintelImportService;
    private final IpaImportService ipaImportService;
    private final AiSearchService aiSearchService;
    private final WebSearchService webSearchService;
    private final ProfiloService profiloService;
    private final StazioneClassificatoreService stazioneClassificatoreService;
    private final StazioneOllamaEnricher stazioneOllamaEnricher;

    @GetMapping("/")
    public String home(BandiSearchRequest req, Model model) {
        String q = req.getQ() != null ? req.getQ() : "";
        String pq = req.getPq() != null ? req.getPq().trim() : "";

        String elaboratedQ = q;
        String queryElaborata = null;
        if (!q.isBlank() && !req.isNoAi()) {
            elaboratedQ = aiSearchService.elaboraQuery(q, req.getStrictness());
            if (!elaboratedQ.equals(q)) queryElaborata = elaboratedQ;
        }

        // pq (profile query) is always used as-is; q goes through AI if enabled
        String queryEffettiva = pq.isBlank() ? elaboratedQ
                : elaboratedQ.isBlank() ? pq
                : pq + " " + elaboratedQ;

        Page<BandoDto> bandi = bandoService.cerca(req, queryEffettiva);

        model.addAttribute("bandi", bandi);
        model.addAttribute("q", q);
        model.addAttribute("pq", pq);
        model.addAttribute("pname", req.getPname() != null ? req.getPname() : "");
        model.addAttribute("tipoDatasetSelezionato", req.getTipoDataset() != null ? req.getTipoDataset() : "");
        model.addAttribute("categorieSelezionate", filterList(req.getCategoria()));
        model.addAttribute("statoSelezionato", filterList(req.getStato()));
        model.addAttribute("regioneSelezionata", filterList(req.getRegione()));
        model.addAttribute("annoSelezionato", req.getAnno());
        model.addAttribute("tipoAmministrazioneSelezionato", filterList(req.getTipoAmministrazione()));
        model.addAttribute("tipoStrumentoSelezionato", filterList(req.getTipoStrumento()));
        model.addAttribute("excludeStato", filterList(req.getExcludeStato()));
        model.addAttribute("excludeRegione", filterList(req.getExcludeRegione()));
        model.addAttribute("excludeTipoAmministrazione", filterList(req.getExcludeTipoAmministrazione()));
        model.addAttribute("excludeTipoStrumento", filterList(req.getExcludeTipoStrumento()));
        model.addAttribute("scadenzaDal", req.getScadenzaDal());
        model.addAttribute("scadenzaAl", req.getScadenzaAl());
        model.addAttribute("scadeEntro", req.getScadeEntro());
        model.addAttribute("soloNonScadute", req.isSoloNonScadute());
        model.addAttribute("importoDa", req.getImportoDa());
        model.addAttribute("importoA", req.getImportoA());
        model.addAttribute("codCpvSelezionato", filterList(req.getCodCpv()));
        model.addAttribute("stazioneAppaltanteSelezionata", filterList(req.getStazioneAppaltante()));
        model.addAttribute("excludeStazioneAppaltante", filterList(req.getExcludeStazioneAppaltante()));
        model.addAttribute("enableConsip", req.getEnableConsip());
        model.addAttribute("enableAnac", req.getEnableAnac());
        model.addAttribute("enableSintel", req.getEnableSintel());

        List<Object[]> cpvList = bandoService.findCpvConConteggio();
        model.addAttribute("cpvList", cpvList);
        Map<String, String> cpvDescMap = cpvList.stream()
                .filter(r -> r[0] != null)
                .collect(Collectors.toMap(r -> (String) r[0], r -> r[1] != null ? (String) r[1] : "", (a, b) -> a));
        model.addAttribute("cpvDescMap", cpvDescMap);
        model.addAttribute("strictness", req.getStrictness());
        model.addAttribute("queryElaborata", queryElaborata);
        model.addAttribute("totaleBandi", bandoService.count());

        List<TipoDataset> tipiConsip = List.of(TipoDataset.GARE_ASP, TipoDataset.BANDI_GARE);
        List<TipoDataset> tipiAnac   = List.of(TipoDataset.ANAC_CIG);
        List<TipoDataset> tipiSintel = List.of(TipoDataset.SINTEL_LOMBARDIA);
        model.addAttribute("fonteConsip", fonteService.findFonteInfo("Consip").orElse(null));
        model.addAttribute("fonteAnac",   fonteService.findFonteInfo("ANAC").orElse(null));
        model.addAttribute("fonteSintel", fonteService.findFonteInfo("Sintel Lombardia").orElse(null));
        model.addAttribute("countConsip", bandoService.countByTipoDatasetIn(tipiConsip));
        model.addAttribute("countAnac",   bandoService.countByTipoDatasetIn(tipiAnac));
        model.addAttribute("countSintel", bandoService.countByTipoDatasetIn(tipiSintel));
        model.addAttribute("minAnnoConsip", bandoService.findMinAnnoByTipiDataset(tipiConsip));
        model.addAttribute("maxAnnoConsip", bandoService.findMaxAnnoByTipiDataset(tipiConsip));
        model.addAttribute("minAnnoAnac",   bandoService.findMinAnnoByTipiDataset(tipiAnac));
        model.addAttribute("maxAnnoAnac",   bandoService.findMaxAnnoByTipiDataset(tipiAnac));
        model.addAttribute("minAnnoSintel", bandoService.findMinAnnoByTipiDataset(tipiSintel));
        model.addAttribute("maxAnnoSintel", bandoService.findMaxAnnoByTipiDataset(tipiSintel));

        model.addAttribute("categorie",           bandoService.findDistinctCategorie());
        model.addAttribute("statiGara",           bandoService.findDistinctStatiGara());
        model.addAttribute("regioni",             bandoService.findDistinctRegioni());
        model.addAttribute("anni",                bandoService.findDistinctAnni());
        model.addAttribute("tipiAmministrazione", bandoService.findDistinctTipiAmministrazione());
        model.addAttribute("tipiStrumento",       bandoService.findDistinctTipiStrumento());
        // Stazioni ANAC (con regione e tipo ANAC)
        Map<String, Object[]> stazioniAnacMap = new java.util.LinkedHashMap<>();
        for (Object[] row : bandoService.findStazioniAppaltantiAnacConDettagli()) {
            String nome = (String) row[0];
            if (nome != null) stazioniAnacMap.put(nome, row);
        }

        // Stazioni Consip — solo quelle non già presenti in ANAC
        Map<String, Long> stazioniConsipExtra = new java.util.LinkedHashMap<>();
        for (Object[] row : bandoService.findStazioniAppaltantiConsipConConteggio()) {
            String nome = (String) row[0];
            if (nome != null && !stazioniAnacMap.containsKey(nome)) {
                stazioniConsipExtra.put(nome, ((Number) row[1]).longValue());
            }
        }

        List<String> nomiStazioni = new ArrayList<>();
        List<Map<String, Object>> stazioniData = new ArrayList<>();

        // Prima le stazioni ANAC
        for (Object[] row : stazioniAnacMap.values()) {
            String nome = (String) row[0];
            String regioneAnac = row[1] != null ? (String) row[1] : null;
            String tipoAnac = row[2] != null ? (String) row[2] : null;
            long count = ((Number) row[3]).longValue();
            nomiStazioni.add(nome);
            StazioneClasse classe = stazioneClassificatoreService.classifica(nome, regioneAnac);
            Map<String, Object> m = new HashMap<>();
            m.put("v", nome);
            m.put("c", count);
            m.put("r", classe.regione());
            m.put("t", classe.tipo() != null ? classe.tipo() : (tipoAnac != null ? toTitleCase(tipoAnac) : "Altro"));
            stazioniData.add(m);
        }

        // Poi le stazioni Consip non presenti in ANAC
        for (Map.Entry<String, Long> e : stazioniConsipExtra.entrySet()) {
            String nome = e.getKey();
            nomiStazioni.add(nome);
            StazioneClasse classe = stazioneClassificatoreService.classifica(nome, null);
            Map<String, Object> m = new HashMap<>();
            m.put("v", nome);
            m.put("c", e.getValue());
            m.put("r", classe.regione());
            m.put("t", classe.tipo() != null ? classe.tipo() : "Altro");
            stazioniData.add(m);
        }

        // Infine le stazioni Sintel non già presenti in ANAC o Consip
        for (Object[] row : bandoService.findStazioniAppaltantiSintelConDettagli()) {
            String nome = (String) row[0];
            if (nome == null || stazioniAnacMap.containsKey(nome) || stazioniConsipExtra.containsKey(nome)) continue;
            String regioneSintel = row[1] != null ? (String) row[1] : null;
            String tipoSintel = row[2] != null ? (String) row[2] : null;
            long count = ((Number) row[3]).longValue();
            nomiStazioni.add(nome);
            StazioneClasse classe = stazioneClassificatoreService.classifica(nome, regioneSintel);
            Map<String, Object> m = new HashMap<>();
            m.put("v", nome);
            m.put("c", count);
            m.put("r", classe.regione());
            m.put("t", classe.tipo() != null ? classe.tipo() : (tipoSintel != null ? toTitleCase(tipoSintel) : "Altro"));
            stazioniData.add(m);
        }

        stazioneOllamaEnricher.enrichAsync(nomiStazioni);
        model.addAttribute("stazioniData", stazioniData);
        model.addAttribute("profiliAttivi",        profiloService.findAttivi());

        return "index";
    }

    @GetMapping("/bandi/{id}")
    public String dettaglioBando(@PathVariable Long id, Model model) {
        BandoDto bando = bandoService.findById(id);
        model.addAttribute("bando", bando);
        return "bando-detail";
    }

    @GetMapping("/bandi/{id}/cerca-web")
    @ResponseBody
    public WebSearchResult cercaWeb(@PathVariable Long id) {
        BandoDto bando = bandoService.findById(id);
        return webSearchService.cerca(bando.getTitolo(), bando.getCodiceEsterno(), bando.getFonteName());
    }

    @GetMapping("/bandi/{id}/chat")
    @ResponseBody
    public String chatBando(
            @PathVariable Long id,
            @RequestParam String message,
            @RequestParam(required = false) String context) {
        BandoDto bando = bandoService.findById(id);
        return webSearchService.chat(message, bando.getTitolo(), bando.getCodiceEsterno(), context);
    }

    @GetMapping("/chat")
    @ResponseBody
    public String chatGeneral(
            @RequestParam String message,
            @RequestParam(required = false) String context) {
        return webSearchService.chat(message, null, null, context);
    }

    @GetMapping("/ai-summary")
    @ResponseBody
    public String aiSummary(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") long totalResults,
            @RequestParam(required = false) List<String> filtriAttivi) {
        if (q.isBlank()) return "";
        String risposta = aiSearchService.generaRisposta(q, totalResults, filtriAttivi);
        return risposta != null ? risposta : "";
    }

    @PostMapping("/import")
    public String triggerImport(RedirectAttributes redirectAttributes) {
        try {
            importService.importa();
            redirectAttributes.addFlashAttribute("successMessage", "Importazione Consip completata con successo");
        } catch (Exception e) {
            log.error("Errore importazione Consip", e);
            String msg = e.getMessage();
            if (msg != null && msg.length() > 200) msg = msg.substring(0, 200) + "…";
            redirectAttributes.addFlashAttribute("errorMessage", "Errore Consip: " + msg);
        }
        return "redirect:/";
    }

    @PostMapping("/import-anac")
    public String triggerImportAnac(
            @RequestParam(defaultValue = "3") int mesiIndietro,
            RedirectAttributes redirectAttributes) {
        try {
            anacImportService.importa(mesiIndietro);
            redirectAttributes.addFlashAttribute("successMessage", "Importazione ANAC completata con successo");
        } catch (Exception e) {
            log.error("Errore importazione ANAC", e);
            String msg = e.getMessage();
            if (msg != null && msg.length() > 200) msg = msg.substring(0, 200) + "…";
            redirectAttributes.addFlashAttribute("errorMessage", "Errore ANAC: " + msg);
        }
        return "redirect:/";
    }

    @PostMapping("/import-sintel")
    public String triggerImportSintel(RedirectAttributes redirectAttributes) {
        try {
            sintelImportService.importa();
            redirectAttributes.addFlashAttribute("successMessage", "Importazione Sintel Lombardia completata con successo");
        } catch (Exception e) {
            log.error("Errore importazione Sintel", e);
            String msg = e.getMessage();
            if (msg != null && msg.length() > 200) msg = msg.substring(0, 200) + "…";
            redirectAttributes.addFlashAttribute("errorMessage", "Errore Sintel: " + msg);
        }
        return "redirect:/";
    }

    @PostMapping("/import-ipa")
    public String triggerImportIpa(RedirectAttributes redirectAttributes) {
        try {
            ipaImportService.importa();
            redirectAttributes.addFlashAttribute("successMessage", "Importazione anagrafica IPA completata con successo");
        } catch (Exception e) {
            log.error("Errore importazione IPA", e);
            String msg = e.getMessage();
            if (msg != null && msg.length() > 200) msg = msg.substring(0, 200) + "…";
            redirectAttributes.addFlashAttribute("errorMessage", "Errore IPA: " + msg);
        }
        return "redirect:/";
    }

    private static List<String> filterList(List<String> list) {
        if (list == null) return List.of();
        return list.stream().filter(s -> s != null && !s.isBlank()).toList();
    }

    private static String toTitleCase(String s) {
        if (s == null || s.isBlank()) return s;
        String[] words = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(' ');
        }
        return sb.toString().trim();
    }
}
