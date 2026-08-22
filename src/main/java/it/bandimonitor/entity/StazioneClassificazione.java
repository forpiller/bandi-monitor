package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stazione_classificazione",
        uniqueConstraints = @UniqueConstraint(name = "uk_staz_cl_nome", columnNames = "nome_stazione"))
@Getter
@Setter
@NoArgsConstructor
public class StazioneClassificazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_stazione", nullable = false, length = 512)
    private String nomeStazione;

    @Column(name = "regione", length = 100)
    private String regione;

    @Column(name = "tipo", length = 100)
    private String tipo;

    public StazioneClassificazione(String nomeStazione, String regione, String tipo) {
        this.nomeStazione = nomeStazione;
        this.regione = regione;
        this.tipo = tipo;
    }
}
