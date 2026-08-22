package it.bandimonitor.service;

import it.bandimonitor.domain.FonteInfoDto;
import it.bandimonitor.entity.FonteMonitorata;
import it.bandimonitor.repository.FonteMonitorataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FonteService {

    private final FonteMonitorataRepository fonteRepository;

    public Optional<FonteInfoDto> findFonteInfo(String nome) {
        return fonteRepository.findByNome(nome).map(this::toDto);
    }

    public Long getFonteId(String nome) {
        return fonteRepository.findByNome(nome)
                .map(FonteMonitorata::getId)
                .orElseThrow(() -> new IllegalStateException("Fonte '" + nome + "' non trovata nel DB"));
    }

    public void aggiornaUltimaSincronizzazione(String nome, LocalDateTime timestamp) {
        fonteRepository.findByNome(nome).ifPresent(f -> {
            f.setUltimaSincronizzazione(timestamp);
            fonteRepository.save(f);
        });
    }

    private FonteInfoDto toDto(FonteMonitorata f) {
        FonteInfoDto dto = new FonteInfoDto();
        dto.setNome(f.getNome());
        dto.setUltimaSincronizzazione(f.getUltimaSincronizzazione());
        dto.setUrlSito(f.getUrlSito());
        return dto;
    }
}
