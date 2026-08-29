package it.bandimonitor.service;

import it.bandimonitor.entity.EnteIpa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class IpaImportService {

    private static final String URL = "https://indicepa.gov.it/ipa-dati/dataset/502ff370-1b2c-4310-94c7-f39ceb7500e3/resource/3ed63523-ff9c-41f6-a6fe-980f3d9e501f/download/amministrazioni.txt";
    private static final int BATCH_SIZE = 500;

    private final RestClient restClient = RestClient.create();

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public int importa() {
        log.info("Avvio importazione anagrafica enti IPA da {}", URL);

        String body = restClient.get().uri(URL).retrieve().body(String.class);
        if (body == null || body.isBlank()) {
            log.warn("File IPA vuoto o non raggiungibile");
            return 0;
        }
        // Rimuove il BOM UTF-8 se presente all'inizio del file
        if (body.charAt(0) == '﻿') body = body.substring(1);

        String[] righe = body.split("\r?\n");
        if (righe.length < 2) {
            log.warn("File IPA senza dati");
            return 0;
        }

        Map<String, Integer> colonne = indicizzaColonne(righe[0]);
        Integer idxCf = colonne.get("cf");
        Integer idxDenominazione = colonne.get("des_amm");
        Integer idxComune = colonne.get("comune");
        Integer idxProvincia = colonne.get("provincia");
        Integer idxRegione = colonne.get("regione");
        Integer idxTipologia = colonne.get("tipologia_amm");

        if (idxCf == null) {
            log.error("Colonna 'cf' non trovata nell'header IPA, import annullato");
            return 0;
        }

        entityManager.createQuery("DELETE FROM EnteIpa").executeUpdate();
        log.info("Anagrafica IPA precedente eliminata");

        int count = 0;
        for (int i = 1; i < righe.length; i++) {
            String riga = righe[i];
            if (riga.isBlank()) continue;
            String[] campi = riga.split("\t", -1);
            String cf = campo(campi, idxCf);
            if (cf == null || cf.isBlank()) continue;

            EnteIpa ente = new EnteIpa();
            ente.setCf(cf.trim());
            ente.setDenominazione(campo(campi, idxDenominazione));
            ente.setComune(campo(campi, idxComune));
            ente.setProvincia(campo(campi, idxProvincia));
            ente.setRegione(campo(campi, idxRegione));
            ente.setTipologiaAmm(campo(campi, idxTipologia));

            entityManager.persist(ente);
            count++;
            if (count % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        log.info("Importazione IPA completata: {} enti caricati", count);
        return count;
    }

    private Map<String, Integer> indicizzaColonne(String headerLine) {
        String[] intestazioni = headerLine.split("\t", -1);
        Map<String, Integer> colonne = new HashMap<>();
        for (int i = 0; i < intestazioni.length; i++) {
            colonne.put(intestazioni[i].trim().toLowerCase(), i);
        }
        return colonne;
    }

    private String campo(String[] campi, Integer idx) {
        if (idx == null || idx >= campi.length) return null;
        String valore = campi[idx].trim();
        return (valore.isEmpty() || "null".equalsIgnoreCase(valore)) ? null : valore;
    }
}
