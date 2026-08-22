package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bando_extra_anac")
public class BandoExtraAnac {

    @Id
    @Column(name = "bando_id")
    private Long bandoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "bando_id")
    private Bando bando;

    // Spazio per campi ANAC-specifici futuri
}
