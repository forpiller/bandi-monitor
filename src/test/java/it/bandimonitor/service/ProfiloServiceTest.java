package it.bandimonitor.service;

import it.bandimonitor.domain.ProfiloDto;
import it.bandimonitor.entity.ProfiloInteresse;
import it.bandimonitor.repository.ProfiloInteresseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfiloServiceTest {

    @Mock
    private ProfiloInteresseRepository profiloRepository;

    private ProfiloService profiloService;

    @BeforeEach
    void setUp() {
        profiloService = new ProfiloService(profiloRepository);
    }

    @Test
    void salvaTrimsFieldsAndReturnsSavedId() {
        ProfiloInteresse saved = new ProfiloInteresse();
        saved.setId(42L);
        when(profiloRepository.save(any(ProfiloInteresse.class))).thenReturn(saved);

        Long id = profiloService.salva("  Laboratori informatici  ", "  pc, laboratorio  ", null);

        assertThat(id).isEqualTo(42L);
        verify(profiloRepository).save(any(ProfiloInteresse.class));
    }

    @Test
    void salvaSetsCpvCodesToNullWhenBlank() {
        when(profiloRepository.save(any(ProfiloInteresse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        profiloService.salva("Nome", "keyword", "   ");

        verify(profiloRepository).save(argThatCpvCodesIsNull());
    }

    @Test
    void aggiornaThrowsNotFoundWhenProfiloMissing() {
        when(profiloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profiloService.aggiorna(99L, "Nome", "keyword", null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void toggleAttivoFlipsFlag() {
        ProfiloInteresse p = new ProfiloInteresse();
        p.setId(1L);
        p.setAttivo(true);
        when(profiloRepository.findById(1L)).thenReturn(Optional.of(p));
        when(profiloRepository.save(any(ProfiloInteresse.class))).thenReturn(p);

        profiloService.toggleAttivo(1L);

        assertThat(p.isAttivo()).isFalse();
    }

    @Test
    void findByIdThrowsNotFoundWhenMissing() {
        when(profiloRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profiloService.findById(7L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void findByIdSplitsCpvCodesIntoList() {
        ProfiloInteresse p = new ProfiloInteresse();
        p.setId(3L);
        p.setNome("Profilo");
        p.setKeywords("stampanti");
        p.setCpvCodes("30200000, 30232000  30121000");
        when(profiloRepository.findById(3L)).thenReturn(Optional.of(p));

        ProfiloDto dto = profiloService.findById(3L);

        assertThat(dto.getCpvCodesList()).containsExactly("30200000", "30232000", "30121000");
    }

    @Test
    void findByIdReturnsEmptyCpvCodesListWhenNull() {
        ProfiloInteresse p = new ProfiloInteresse();
        p.setId(4L);
        p.setNome("Profilo");
        p.setKeywords("pc");
        when(profiloRepository.findById(4L)).thenReturn(Optional.of(p));

        ProfiloDto dto = profiloService.findById(4L);

        assertThat(dto.getCpvCodesList()).isEmpty();
    }

    private ProfiloInteresse argThatCpvCodesIsNull() {
        return org.mockito.ArgumentMatchers.argThat(p -> p.getCpvCodes() == null);
    }
}
