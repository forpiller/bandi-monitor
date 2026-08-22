package it.bandimonitor.service;

public record StazioneClasse(String regione, String tipo) {
    public boolean isCompleta() {
        return regione != null && tipo != null;
    }
}
