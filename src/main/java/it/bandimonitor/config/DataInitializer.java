package it.bandimonitor.config;

import it.bandimonitor.entity.FonteMonitorata;
import it.bandimonitor.repository.FonteMonitorataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final FonteMonitorataRepository fonteRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (fonteRepository.findByNome("Consip").isEmpty()) {
            FonteMonitorata consip = new FonteMonitorata();
            consip.setNome("Consip");
            consip.setUrlApi("https://dati.consip.it/api/3/action");
            consip.setAttiva(true);
            fonteRepository.save(consip);
            log.info("Fonte 'Consip' inizializzata");
        }
        if (fonteRepository.findByNome("ANAC").isEmpty()) {
            FonteMonitorata anac = new FonteMonitorata();
            anac.setNome("ANAC");
            anac.setUrlApi("https://dati.anticorruzione.it/opendata");
            anac.setAttiva(true);
            fonteRepository.save(anac);
            log.info("Fonte 'ANAC' inizializzata");
        }
        if (fonteRepository.findByNome("Sintel Lombardia").isEmpty()) {
            FonteMonitorata sintel = new FonteMonitorata();
            sintel.setNome("Sintel Lombardia");
            sintel.setUrlApi("https://www.dati.lombardia.it/resource/8txy-zjw2.json");
            sintel.setUrlSito("https://www.sintel.regione.lombardia.it");
            sintel.setAttiva(true);
            fonteRepository.save(sintel);
            log.info("Fonte 'Sintel Lombardia' inizializzata");
        }
    }
}
