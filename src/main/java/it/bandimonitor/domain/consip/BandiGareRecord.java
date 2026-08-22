package it.bandimonitor.domain.consip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BandiGareRecord {

    @JsonProperty("#Denominazione_Bando")
    private String denominazioneBando;

    @JsonProperty("Identificativo_Lotto")
    private String identificativoLotto;

    @JsonProperty("Denominazione_Lotto")
    private String denominazioneLotto;

    @JsonProperty("Categoria_Merceologica")
    private String categoriaMerceologica;

    @JsonProperty("Tipo_Strumento")
    private String tipoStrumento;

    @JsonProperty("Tipo_Procedura")
    private String tipoProcedura;

    @JsonProperty("Data_Pubblicazione")
    private String dataPubblicazione;

    @JsonProperty("Data_Termine")
    private String dataTermine;

    @JsonProperty("Data_Attivazione")
    private String dataAttivazione;

    @JsonProperty("Base_Asta")
    private String baseAsta;

    @JsonProperty("Criterio_Aggiudicazione")
    private String criterioAggiudicazione;

    @JsonProperty("Tipologia_Lotto")
    private String tipologiaLotto;

    @JsonProperty("Unita_Misura")
    private String unitaMisura;

    @JsonProperty("Verde")
    private String verde;
}
