package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ente_ipa", uniqueConstraints = @UniqueConstraint(name = "uk_ente_ipa_cf", columnNames = "cf"))
public class EnteIpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cf", nullable = false, length = 20)
    private String cf;

    @Column(name = "denominazione", length = 500)
    private String denominazione;

    @Column(name = "comune", length = 200)
    private String comune;

    @Column(name = "provincia", length = 10)
    private String provincia;

    @Column(name = "regione", length = 100)
    private String regione;

    @Column(name = "tipologia_amm", length = 200)
    private String tipologiaAmm;
}
