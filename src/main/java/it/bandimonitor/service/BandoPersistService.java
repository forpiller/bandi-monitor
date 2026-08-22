package it.bandimonitor.service;

import it.bandimonitor.domain.BandoCreateCommand;
import it.bandimonitor.entity.Bando;
import it.bandimonitor.entity.BandoExtraAsp;
import it.bandimonitor.entity.FonteMonitorata;
import it.bandimonitor.entity.StatoBando;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BandoPersistService {

    private static final int BATCH_SIZE = 500;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public int salvaBatch(List<BandoCreateCommand> comandi) {
        int count = 0;
        for (int i = 0; i < comandi.size(); i++) {
            entityManager.persist(toEntity(comandi.get(i)));
            count++;
            if (count % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
                log.debug("Batch flush dopo {} record", count);
            }
        }
        entityManager.flush();
        entityManager.clear();
        return count;
    }

    private Bando toEntity(BandoCreateCommand cmd) {
        Bando bando = new Bando();
        bando.setCodiceEsterno(cmd.getCodiceEsterno());
        bando.setTitolo(cmd.getTitolo());
        bando.setDescrizione(cmd.getDescrizione());
        bando.setStazioneAppaltante(cmd.getStazioneAppaltante());
        bando.setDenominazioneAmministrazione(cmd.getDenominazioneAmministrazione());
        bando.setImportoBase(cmd.getImportoBase());
        bando.setDataPubblicazione(cmd.getDataPubblicazione());
        bando.setDataScadenza(cmd.getDataScadenza());
        bando.setUrlOriginale(cmd.getUrlOriginale());
        bando.setTags(cmd.getTags());
        bando.setDataImportazione(cmd.getDataImportazione() != null ? cmd.getDataImportazione() : LocalDateTime.now());
        bando.setDataAggiornamento(cmd.getDataAggiornamento());
        bando.setStato(StatoBando.IMPORTATO);
        bando.setFonte(entityManager.getReference(FonteMonitorata.class, cmd.getFonteId()));
        bando.setTipoDataset(cmd.getTipoDataset());
        bando.setCategoriaGara(cmd.getCategoriaGara());
        bando.setStatoGara(cmd.getStatoGara());
        bando.setTipoAmministrazione(cmd.getTipoAmministrazione());
        bando.setRegionePa(cmd.getRegionePa());
        bando.setProvinciaPa(cmd.getProvinciaPa());
        bando.setAnnoRiferimento(cmd.getAnnoRiferimento());
        bando.setMeseRiferimento(cmd.getMeseRiferimento());
        bando.setTipoStrumento(cmd.getTipoStrumento());
        bando.setTipoProcedura(cmd.getTipoProcedura());
        bando.setCodCpv(cmd.getCodCpv());
        bando.setDescrizioneCpv(cmd.getDescrizioneCpv());

        if (hasAspExtra(cmd)) {
            BandoExtraAsp extra = new BandoExtraAsp();
            extra.setBando(bando);
            extra.setComparto(cmd.getComparto());
            extra.setStatoLotto(cmd.getStatoLotto());
            extra.setOggettoAppalto(cmd.getOggettoAppalto());
            extra.setCriterioAggiudicazione(cmd.getCriterioAggiudicazione());
            extra.setValoreAggiudicato(cmd.getValoreAggiudicato());
            extra.setSiglaProvincia(cmd.getSiglaProvincia());
            extra.setLatitudinePa(cmd.getLatitudinePa());
            extra.setLongitudinePa(cmd.getLongitudinePa());
            bando.setExtraAsp(extra);
        }

        return bando;
    }

    private boolean hasAspExtra(BandoCreateCommand cmd) {
        return cmd.getOggettoAppalto() != null || cmd.getCriterioAggiudicazione() != null
                || cmd.getValoreAggiudicato() != null || cmd.getComparto() != null
                || cmd.getStatoLotto() != null || cmd.getSiglaProvincia() != null
                || cmd.getLatitudinePa() != null || cmd.getLongitudinePa() != null;
    }
}
