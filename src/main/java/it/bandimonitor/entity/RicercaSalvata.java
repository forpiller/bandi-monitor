package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ricerche_salvate", indexes = {
        @Index(name = "idx_ricerche_data", columnList = "data_ricerca")
})
public class RicercaSalvata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_testo", nullable = false, length = 1000)
    private String queryTesto;

    @Column(name = "filtri_json", columnDefinition = "TEXT")
    private String filtriJson;

    @Column(name = "data_ricerca", nullable = false)
    private LocalDateTime dataRicerca;

    @Column(name = "numero_risultati")
    private Integer numeroRisultati;
}
