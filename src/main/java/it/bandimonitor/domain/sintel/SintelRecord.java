package it.bandimonitor.domain.sintel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SintelRecord {

    @JsonProperty("multilotto")
    private String multilotto;

    @JsonProperty("id_procedura")
    private String idProcedura;

    @JsonProperty("nome_procedura")
    private String nomeProcedura;

    @JsonProperty("valore_economico_procedura")
    private String valoreEconomicoProcedura;

    @JsonProperty("stato_procedura")
    private String statoProcedura;

    @JsonProperty("stazione_appaltante")
    private String stazioneAppaltante;

    @JsonProperty("cf_stazione_appaltante")
    private String cfStazioneAppaltante;

    @JsonProperty("data_inizio_negoziazione")
    private String dataInizioNegoziazione;

    @JsonProperty("data_fine_negoziazione")
    private String dataFineNegoziazione;

    @JsonProperty("modalita_scelta_contraente")
    private String modalitaSceltaContraente;

    @JsonProperty("id_lotto")
    private String idLotto;

    @JsonProperty("nome_lotto")
    private String nomeLotto;

    @JsonProperty("stato_lotto")
    private String statoLotto;

    @JsonProperty("base_asta_lotto")
    private String baseAstaLotto;

    @JsonProperty("categoria_merceologiche")
    private String categoriaMerceologiche;

    @JsonProperty("codice_cpv")
    private String codiceCpv;

    @JsonProperty("link_procedura")
    private LinkRef linkProcedura;

    @JsonProperty("link_lotto")
    private LinkRef linkLotto;

    public String getMultilotto() { return multilotto; }
    public String getIdProcedura() { return idProcedura; }
    public String getNomeProcedura() { return nomeProcedura; }
    public String getValoreEconomicoProcedura() { return valoreEconomicoProcedura; }
    public String getStatoProcedura() { return statoProcedura; }
    public String getStazioneAppaltante() { return stazioneAppaltante; }
    public String getCfStazioneAppaltante() { return cfStazioneAppaltante; }
    public String getDataInizioNegoziazione() { return dataInizioNegoziazione; }
    public String getDataFineNegoziazione() { return dataFineNegoziazione; }
    public String getModalitaSceltaContraente() { return modalitaSceltaContraente; }
    public String getIdLotto() { return idLotto; }
    public String getNomeLotto() { return nomeLotto; }
    public String getStatoLotto() { return statoLotto; }
    public String getBaseAstaLotto() { return baseAstaLotto; }
    public String getCategoriaMerceologiche() { return categoriaMerceologiche; }
    public String getCodiceCpv() { return codiceCpv; }
    public LinkRef getLinkProcedura() { return linkProcedura; }
    public LinkRef getLinkLotto() { return linkLotto; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkRef {
        @JsonProperty("url")
        private String url;

        public String getUrl() { return url; }
    }
}
