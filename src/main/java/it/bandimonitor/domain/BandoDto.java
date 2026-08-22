package it.bandimonitor.domain;

import it.bandimonitor.entity.StatoBando;
import it.bandimonitor.entity.TipoDataset;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BandoDto {

    private Long id;
    private String codiceEsterno;
    private String titolo;
    private String descrizione;
    private String stazioneAppaltante;
    private String denominazioneAmministrazione;
    private BigDecimal importoBase;
    private LocalDate dataPubblicazione;
    private LocalDate dataScadenza;
    private String urlOriginale;
    private String statoGara;
    private LocalDateTime dataImportazione;
    private LocalDateTime dataAggiornamento;
    private StatoBando stato;
    private String tags;

    private TipoDataset tipoDataset;
    private String categoriaGara;
    private String tipoAmministrazione;
    private String regionePa;
    private String provinciaPa;
    private Integer annoRiferimento;
    private Integer meseRiferimento;
    private String tipoStrumento;
    private String tipoProcedura;
    private String codCpv;
    private String descrizioneCpv;

    private String oggettoAppalto;
    private String criterioAggiudicazione;
    private BigDecimal valoreAggiudicato;
    private String comparto;
    private String statoLotto;
    private String siglaProvincia;
    private String latitudinePa;
    private String longitudinePa;

    private String fonteName;
    private String fonteUrlSito;
}
