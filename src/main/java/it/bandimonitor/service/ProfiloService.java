package it.bandimonitor.service;

import it.bandimonitor.domain.ProfiloDto;
import it.bandimonitor.entity.ProfiloInteresse;
import it.bandimonitor.repository.ProfiloInteresseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfiloService {

    private final ProfiloInteresseRepository profiloRepository;

    public List<ProfiloDto> findAll() {
        return profiloRepository.findAllByOrderByDataCreazioneAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public List<ProfiloDto> findAttivi() {
        return profiloRepository.findByAttivoTrueOrderByDataCreazioneAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public Long salva(String nome, String keywords, String cpvCodes) {
        ProfiloInteresse p = new ProfiloInteresse();
        p.setNome(nome.trim());
        p.setKeywords(keywords.trim());
        p.setCpvCodes(cpvCodes != null && !cpvCodes.isBlank() ? cpvCodes.trim() : null);
        return profiloRepository.save(p).getId();
    }

    public void aggiorna(Long id, String nome, String keywords, String cpvCodes) {
        ProfiloInteresse p = profiloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setNome(nome.trim());
        p.setKeywords(keywords.trim());
        p.setCpvCodes(cpvCodes != null && !cpvCodes.isBlank() ? cpvCodes.trim() : null);
        profiloRepository.save(p);
    }

    public void toggleAttivo(Long id) {
        ProfiloInteresse p = profiloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setAttivo(!p.isAttivo());
        profiloRepository.save(p);
    }

    public void elimina(Long id) {
        profiloRepository.deleteById(id);
    }

    public ProfiloDto findById(Long id) {
        return profiloRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private ProfiloDto toDto(ProfiloInteresse p) {
        ProfiloDto dto = new ProfiloDto();
        dto.setId(p.getId());
        dto.setNome(p.getNome());
        dto.setKeywords(p.getKeywords());
        dto.setCpvCodes(p.getCpvCodes());
        dto.setAttivo(p.isAttivo());
        dto.setDataCreazione(p.getDataCreazione());
        if (p.getCpvCodes() != null && !p.getCpvCodes().isBlank()) {
            dto.setCpvCodesList(Arrays.stream(p.getCpvCodes().split("[,\\s]+"))
                    .map(String::trim).filter(s -> !s.isBlank()).toList());
        } else {
            dto.setCpvCodesList(List.of());
        }
        return dto;
    }
}
