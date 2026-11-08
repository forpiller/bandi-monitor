package it.bandimonitor.web.vm;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreaProfiloRequest {

    @NotBlank
    private String nome;

    @NotBlank
    private String keywords;

    private String cpvCodes;
}
