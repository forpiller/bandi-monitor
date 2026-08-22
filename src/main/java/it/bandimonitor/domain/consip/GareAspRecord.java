package it.bandimonitor.domain.consip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GareAspRecord {

    @JsonProperty("Comparto")
    private String comparto;

    @JsonProperty("#Anno_Riferimento")
    private String annoRiferimento;

    @JsonProperty("Identificativo_lotto")
    private String identificativoLotto;

    @JsonProperty("Denominazione_gara")
    private String denominazioneGara;

    @JsonProperty("Denominazione_lotto")
    private String denominazioneLotto;

    @JsonProperty("Categoria_merceologica")
    private String categoriaMerceologica;

    @JsonProperty("Stato_gara")
    private String statoGara;

    @JsonProperty("Stato_lotto")
    private String statoLotto;

    @JsonProperty("Data_pubblicazione")
    private String dataPubblicazione;

    @JsonProperty("Base_asta")
    private String baseAsta;

    @JsonProperty("Valore_aggiudicato")
    private String valoreAggiudicato;

    @JsonProperty("Tipologia_Amministrazione")
    private String tipologiaAmministrazione;

    @JsonProperty("Denominazione_amministrazione")
    private String denominazioneAmministrazione;

    @JsonProperty("Regione_PA")
    private String regionePa;

    @JsonProperty("Provincia_PA")
    private String provinciaPa;

    @JsonProperty("Sigla_prov_PA")
    private String siglaProvincia;

    @JsonProperty("Tipo_procedura")
    private String tipoProcedura;

    @JsonProperty("Criterio_aggiudicazione")
    private String criterioAggiudicazione;

    @JsonProperty("Oggetto_appalto")
    private String oggettoAppalto;

    @JsonProperty("Latitudine_PA")
    private String latitudinePa;

    @JsonProperty("Longitudine_PA")
    private String longitudinePa;
}
