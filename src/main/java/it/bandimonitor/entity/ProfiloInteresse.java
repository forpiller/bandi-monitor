package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "profili_interesse", indexes = {
        @Index(name = "idx_profili_attivo", columnList = "attivo")
})
public class ProfiloInteresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "keywords", nullable = false, columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "cpv_codes", columnDefinition = "TEXT")
    private String cpvCodes;

    @Column(name = "attivo", nullable = false)
    private boolean attivo = true;

    @Column(name = "data_creazione", nullable = false)
    private LocalDateTime dataCreazione = LocalDateTime.now();
}
