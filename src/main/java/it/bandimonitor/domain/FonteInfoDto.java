package it.bandimonitor.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FonteInfoDto {

    private String nome;
    private LocalDateTime ultimaSincronizzazione;
    private String urlSito;
}
