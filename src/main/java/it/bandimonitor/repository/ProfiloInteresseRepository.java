package it.bandimonitor.repository;

import it.bandimonitor.entity.ProfiloInteresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfiloInteresseRepository extends JpaRepository<ProfiloInteresse, Long> {

    List<ProfiloInteresse> findAllByOrderByDataCreazioneAsc();

    List<ProfiloInteresse> findByAttivoTrueOrderByDataCreazioneAsc();
}
