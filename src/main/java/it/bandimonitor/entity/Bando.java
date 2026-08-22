package it.bandimonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bandi", indexes = {
        @Index(name = "idx_bandi_codice_esterno", columnList = "codice_esterno"),
        @Index(name = "idx_bandi_fonte_id", columnList = "fonte_id"),
        @Index(name = "idx_bandi_stato", columnList = "stato"),
        @Index(name = "idx_bandi_data_importazione", columnList = "data_importazione"),
        @Index(name = "idx_bandi_titolo", columnList = "titolo"),
        @Index(name = "idx_bandi_tipo_dataset", columnList = "tipo_dataset"),
        @Index(name = "idx_bandi_categoria_gara", columnList = "categoria_gara"),
        @Index(name = "idx_bandi_stato_gara", columnList = "stato_gara"),
        @Index(name = "idx_bandi_regione_pa", columnList = "regione_pa"),
        @Index(name = "idx_bandi_anno_rif", columnList = "anno_riferimento"),
        @Index(name = "idx_bandi_tipo_amm", columnList = "tipo_amministrazione"),
        @Index(name = "idx_bandi_data_scadenza", columnList = "data_scadenza"),
        @Index(name = "idx_bandi_cod_cpv", columnList = "cod_cpv"),
        @Index(name = "idx_bandi_stazione_appaltante", columnList = "stazione_appaltante")
})
public class Bando {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice_esterno", nullable = false, unique = true)
    private String codiceEsterno;

    @Column(name = "titolo", nullable = false, length = 1000)
    private String titolo;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "stazione_appaltante", length = 500)
    private String stazioneAppaltante;

    @Column(name = "importo_base", precision = 15, scale = 2)
    private BigDecimal importoBase;

    @Column(name = "data_pubblicazione")
    private LocalDate dataPubblicazione;

    @Column(name = "data_scadenza")
    private LocalDate dataScadenza;

    @Column(name = "url_originale", length = 2000)
    private String urlOriginale;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false, length = 50)
    private StatoBando stato = StatoBando.IMPORTATO;

    @Column(name = "data_importazione", nullable = false)
    private LocalDateTime dataImportazione;

    @Column(name = "data_aggiornamento")
    private LocalDateTime dataAggiornamento;

    @ManyToOne
    @JoinColumn(name = "fonte_id", nullable = false)
    private FonteMonitorata fonte;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dataset", length = 20)
    private TipoDataset tipoDataset;

    @Column(name = "categoria_gara", length = 200)
    private String categoriaGara;

    @Column(name = "stato_gara", length = 200)
    private String statoGara;

    @Column(name = "tipo_amministrazione", length = 200)
    private String tipoAmministrazione;

    @Column(name = "denominazione_amministrazione", length = 500)
    private String denominazioneAmministrazione;

    @Column(name = "regione_pa", length = 100)
    private String regionePa;

    @Column(name = "provincia_pa", length = 100)
    private String provinciaPa;

    @Column(name = "anno_riferimento")
    private Integer annoRiferimento;

    @Column(name = "tipo_strumento", length = 100)
    private String tipoStrumento;

    @Column(name = "tipo_procedura", length = 100)
    private String tipoProcedura;

    @Column(name = "mese_riferimento")
    private Integer meseRiferimento;

    @Column(name = "cod_cpv", length = 20)
    private String codCpv;

    @Column(name = "descrizione_cpv", length = 300)
    private String descrizioneCpv;

    @OneToOne(mappedBy = "bando", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private BandoExtraAsp extraAsp;

    @OneToOne(mappedBy = "bando", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private BandoExtraAnac extraAnac;
}
