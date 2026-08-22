package it.bandimonitor.domain;

import it.bandimonitor.entity.TipoDataset;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BandoCreateCommand {

    private String codiceEsterno;
    private String titolo;
    private String descrizione;
    private String stazioneAppaltante;
    private String denominazioneAmministrazione;
    private BigDecimal importoBase;
    private LocalDate dataPubblicazione;
    private LocalDate dataScadenza;
    private LocalDateTime dataAggiornamento;
    private String urlOriginale;
    private String tags;
    private LocalDateTime dataImportazione;

    private Long fonteId;
    private TipoDataset tipoDataset;
    private String categoriaGara;
    private String statoGara;
    private String tipoAmministrazione;
    private String regionePa;
    private String provinciaPa;
    private Integer annoRiferimento;
    private Integer meseRiferimento;
    private String tipoStrumento;
    private String tipoProcedura;
    private String codCpv;
    private String descrizioneCpv;

    // Campi extra GARE_ASP — mappati su bando_extra_asp
    private String oggettoAppalto;
    private String criterioAggiudicazione;
    private BigDecimal valoreAggiudicato;
    private String comparto;
    private String statoLotto;
    private String siglaProvincia;
    private String latitudinePa;
    private String longitudinePa;
}
