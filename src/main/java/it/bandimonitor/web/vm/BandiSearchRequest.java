package it.bandimonitor.web.vm;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BandiSearchRequest {

    private String q = "";
    private String tipoDataset = "";
    private List<String> categoria;
    private List<String> stato;
    private List<String> regione;
    private Integer anno;
    private List<String> tipoAmministrazione;
    private List<String> tipoStrumento;
    private List<String> excludeStato;
    private List<String> excludeRegione;
    private List<String> excludeTipoAmministrazione;
    private List<String> excludeTipoStrumento;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate scadenzaDal;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate scadenzaAl;

    private Integer scadeEntro;
    private boolean soloNonScadute = false;
    private BigDecimal importoDa;
    private BigDecimal importoA;
    private List<String> codCpv;
    private List<String> stazioneAppaltante;
    private List<String> excludeStazioneAppaltante;
    private Boolean enableConsip;
    private Boolean enableAnac;
    private String strictness = "normale";
    private int page = 0;
    private boolean noAi = false;
    private String pq = "";    // profile query — not shown in textarea, not AI-processed
    private String pname = ""; // profile display name for the badge
}
