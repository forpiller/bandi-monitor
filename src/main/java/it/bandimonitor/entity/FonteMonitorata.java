package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "fonti_monitorate", indexes = {
        @Index(name = "idx_fonti_nome", columnList = "nome")
})
public class FonteMonitorata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    @Column(name = "url_api", nullable = false, length = 500)
    private String urlApi;

    @Column(name = "url_sito", length = 500)
    private String urlSito;

    @Column(name = "ultima_sincronizzazione")
    private LocalDateTime ultimaSincronizzazione;

    @Column(name = "attiva", nullable = false)
    private boolean attiva = true;
}
