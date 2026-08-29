package it.bandimonitor.repository;

import it.bandimonitor.entity.EnteIpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnteIpaRepository extends JpaRepository<EnteIpa, Long> {

    Optional<EnteIpa> findByCf(String cf);
}
