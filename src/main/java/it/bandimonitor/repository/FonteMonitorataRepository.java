package it.bandimonitor.repository;

import it.bandimonitor.entity.FonteMonitorata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FonteMonitorataRepository extends JpaRepository<FonteMonitorata, Long> {

    Optional<FonteMonitorata> findByNome(String nome);
}
