package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "bando_extra_asp")
public class BandoExtraAsp {

    @Id
    @Column(name = "bando_id")
    private Long bandoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "bando_id")
    private Bando bando;

    @Column(name = "comparto", length = 100)
    private String comparto;

    @Column(name = "stato_lotto", length = 200)
    private String statoLotto;

    @Column(name = "oggetto_appalto", columnDefinition = "text")
    private String oggettoAppalto;

    @Column(name = "criterio_aggiudicazione", length = 300)
    private String criterioAggiudicazione;

    @Column(name = "valore_aggiudicato", precision = 15, scale = 2)
    private BigDecimal valoreAggiudicato;

    @Column(name = "sigla_provincia", length = 10)
    private String siglaProvincia;

    @Column(name = "latitudine_pa", length = 30)
    private String latitudinePa;

    @Column(name = "longitudine_pa", length = 30)
    private String longitudinePa;
}
