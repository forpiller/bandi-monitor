package it.bandimonitor.web.rest;

import it.bandimonitor.domain.ProfiloDto;
import it.bandimonitor.service.ProfiloService;
import it.bandimonitor.web.vm.CreaProfiloRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Versione JSON del CRUD profili, pensata per essere usata da un agente AI
 * (tramite mcp-forge) oltre che dal form HTML esistente in {@link ProfiloController}.
 */
@RestController
@RequestMapping("/api/profili")
@RequiredArgsConstructor
public class ProfiloRestController {

    private final ProfiloService profiloService;

    @GetMapping
    public List<ProfiloDto> lista() {
        return profiloService.findAll();
    }

    @PostMapping
    public ResponseEntity<ProfiloDto> crea(@Valid @RequestBody CreaProfiloRequest request) {
        Long id = profiloService.salva(request.getNome(), request.getKeywords(), request.getCpvCodes());
        return ResponseEntity.status(HttpStatus.CREATED).body(profiloService.findById(id));
    }
}
