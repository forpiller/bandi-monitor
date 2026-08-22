package it.bandimonitor.repository;

import it.bandimonitor.entity.StazioneClassificazione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StazioneClassificazioneRepository extends JpaRepository<StazioneClassificazione, Long> {

    Optional<StazioneClassificazione> findByNomeStazione(String nomeStazione);
}
