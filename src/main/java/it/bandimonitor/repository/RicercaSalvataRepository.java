package it.bandimonitor.repository;

import it.bandimonitor.entity.RicercaSalvata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RicercaSalvataRepository extends JpaRepository<RicercaSalvata, Long> {

    Page<RicercaSalvata> findAllByOrderByDataRicercaDesc(Pageable pageable);
}
