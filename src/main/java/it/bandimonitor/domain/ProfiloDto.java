package it.bandimonitor.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProfiloDto {

    private Long id;
    private String nome;
    private String keywords;
    private String cpvCodes;
    private List<String> cpvCodesList;
    private boolean attivo;
    private LocalDateTime dataCreazione;

    public String getQueryParam() {
        return keywords != null ? keywords.trim() : "";
    }
}
