package it.bandimonitor.domain.consip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnacCigRecord {

    @JsonProperty("cig")
    private String cig;

    @JsonProperty("oggetto_lotto")
    private String oggettoLotto;

    @JsonProperty("oggetto_gara")
    private String oggettoGara;

    @JsonProperty("importo_lotto")
    private Double importoLotto;

    @JsonProperty("stato")
    private String stato;

    @JsonProperty("data_pubblicazione")
    private String dataPubblicazione;

    @JsonProperty("data_scadenza_offerta")
    private String dataScadenzaOfferta;

    @JsonProperty("denominazione_amministrazione_appaltante")
    private String denominazioneAmministrazioneAppaltante;

    @JsonProperty("sezione_regionale")
    private String sezioneRegionale;

    @JsonProperty("descrizione_cpv")
    private String descrizioneCpv;

    @JsonProperty("cod_cpv")
    private String codCpv;

    @JsonProperty("oggetto_principale_contratto")
    private String oggettoPrincipaleContratto;

    @JsonProperty("tipo_scelta_contraente")
    private String tipoSceltaContraente;

    @JsonProperty("anno_pubblicazione")
    private Integer annoPubblicazione;

    @JsonProperty("ESITO")
    private String esito;

    public String getCig() { return cig; }
    public String getOggettoLotto() { return oggettoLotto; }
    public String getOggettoGara() { return oggettoGara; }
    public Double getImportoLotto() { return importoLotto; }
    public String getStato() { return stato; }
    public String getDataPubblicazione() { return dataPubblicazione; }
    public String getDataScadenzaOfferta() { return dataScadenzaOfferta; }
    public String getDenominazioneAmministrazioneAppaltante() { return denominazioneAmministrazioneAppaltante; }
    public String getSezioneRegionale() { return sezioneRegionale; }
    public String getDescrizioneCpv() { return descrizioneCpv; }
    public String getCodCpv() { return codCpv; }
    public String getOggettoPrincipaleContratto() { return oggettoPrincipaleContratto; }
    public String getTipoSceltaContraente() { return tipoSceltaContraente; }
    public Integer getAnnoPubblicazione() { return annoPubblicazione; }
    public String getEsito() { return esito; }
}
