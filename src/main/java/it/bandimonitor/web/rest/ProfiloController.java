package it.bandimonitor.web.rest;

import it.bandimonitor.service.BandoService;
import it.bandimonitor.service.ProfiloService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profili")
public class ProfiloController {

    private final ProfiloService profiloService;
    private final BandoService bandoService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("profili", profiloService.findAll());
        model.addAttribute("cpvList", bandoService.findCpvConConteggio());
        return "profili";
    }

    @PostMapping("/nuovo")
    public String nuovo(
            @RequestParam String nome,
            @RequestParam String keywords,
            @RequestParam(required = false) String cpvCodes,
            RedirectAttributes ra) {
        profiloService.salva(nome, keywords, cpvCodes);
        ra.addFlashAttribute("successMessage", "Profilo \"" + nome + "\" creato");
        return "redirect:/profili";
    }

    @PostMapping("/{id}/aggiorna")
    public String aggiorna(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam String keywords,
            @RequestParam(required = false) String cpvCodes,
            RedirectAttributes ra) {
        profiloService.aggiorna(id, nome, keywords, cpvCodes);
        ra.addFlashAttribute("successMessage", "Profilo aggiornato");
        return "redirect:/profili";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam(required = false, defaultValue = "/profili") String redirect) {
        profiloService.toggleAttivo(id);
        return "redirect:" + (redirect.startsWith("/") ? redirect : "/profili");
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, RedirectAttributes ra) {
        profiloService.elimina(id);
        ra.addFlashAttribute("successMessage", "Profilo eliminato");
        return "redirect:/profili";
    }
}
