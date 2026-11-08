package it.bandimonitor.service;

import it.bandimonitor.domain.BandoDto;
import it.bandimonitor.entity.Bando;
import it.bandimonitor.entity.TipoDataset;
import it.bandimonitor.repository.BandoRepository;
import it.bandimonitor.web.vm.BandiSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BandoService {

    private final BandoRepository bandoRepository;

    public Page<BandoDto> cerca(BandiSearchRequest req, String queryEffettiva) {
        boolean requireAll = "precisa".equals(req.getStrictness());

        Specification<Bando> spec = Specification
                .where(BandoSpec.hasKeyword(queryEffettiva, requireAll))
                .and(BandoSpec.hasTipoDataset(parseTipoDataset(req.getTipoDataset())))
                .and(BandoSpec.hasCategoria(req.getCategoria()))
                .and(BandoSpec.hasStato(filterList(req.getStato())))
                .and(BandoSpec.hasRegione(filterList(req.getRegione())))
                .and(BandoSpec.hasAnno(req.getAnno()))
                .and(BandoSpec.hasTipoAmministrazione(filterList(req.getTipoAmministrazione())))
                .and(BandoSpec.hasTipoStrumento(filterList(req.getTipoStrumento())))
                .and(BandoSpec.excludeStato(filterList(req.getExcludeStato())))
                .and(BandoSpec.excludeRegione(filterList(req.getExcludeRegione())))
                .and(BandoSpec.excludeTipoAmministrazione(filterList(req.getExcludeTipoAmministrazione())))
                .and(BandoSpec.excludeTipoStrumento(filterList(req.getExcludeTipoStrumento())))
                .and(BandoSpec.hasScadenzaDal(req.getScadenzaDal()))
                .and(BandoSpec.hasScadenzaAl(req.getScadenzaAl()))
                .and(BandoSpec.scadeEntro(req.getScadeEntro()))
                .and(BandoSpec.hasImportoDa(req.getImportoDa()))
                .and(BandoSpec.hasImportoA(req.getImportoA()))
                .and(BandoSpec.hasCodCpv(req.getCodCpv()))
                .and(BandoSpec.hasStazioneAppaltante(filterList(req.getStazioneAppaltante())))
                .and(BandoSpec.excludeStazioneAppaltante(filterList(req.getExcludeStazioneAppaltante())))
                .and(BandoSpec.soloNonScadute(req.isSoloNonScadute()))
                .and(BandoSpec.includeSorgenti(req.getEnableConsip(), req.getEnableAnac(), req.getEnableSintel()));

        PageRequest pageable = PageRequest.of(req.getPage(), 20, Sort.by("dataImportazione").descending());
        return bandoRepository.findAll(spec, pageable).map(this::toDto);
    }

    public BandoDto findById(Long id) {
        return bandoRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bando non trovato"));
    }

    public long count() {
        return bandoRepository.count();
    }

    public long countByTipoDatasetIn(List<TipoDataset> tipi) {
        return bandoRepository.countByTipoDatasetIn(tipi);
    }

    public Integer findMinAnnoByTipiDataset(List<TipoDataset> tipi) {
        return bandoRepository.findMinAnnoByTipiDataset(tipi);
    }

    public Integer findMaxAnnoByTipiDataset(List<TipoDataset> tipi) {
        return bandoRepository.findMaxAnnoByTipiDataset(tipi);
    }

    public List<String> findDistinctCategorie()           { return bandoRepository.findDistinctCategorie(); }
    public List<String> findDistinctStatiGara()           { return bandoRepository.findDistinctStatiGara(); }
    public List<String> findDistinctRegioni()             { return bandoRepository.findDistinctRegioni(); }
    public List<Integer> findDistinctAnni()               { return bandoRepository.findDistinctAnni(); }
    public List<String> findDistinctTipiAmministrazione() { return bandoRepository.findDistinctTipiAmministrazione(); }
    public List<String> findDistinctTipiStrumento()       { return bandoRepository.findDistinctTipiStrumento(); }
    public List<Object[]> findCpvConConteggio()           { return bandoRepository.findCpvConConteggio(); }
    public List<Object[]> findStazioniAppaltantiAnacConDettagli()      { return bandoRepository.findStazioniAppaltantiAnacConDettagli(); }
    public List<Object[]> findStazioniAppaltantiConsipConConteggio()   { return bandoRepository.findStazioniAppaltantiConsipConConteggio(); }
    public List<Object[]> findStazioniAppaltantiSintelConDettagli()    { return bandoRepository.findStazioniAppaltantiSintelConDettagli(); }

    public void deleteByTipoDatasetIn(List<TipoDataset> tipi) {
        List<String> tipiStr = tipi.stream().map(TipoDataset::name).toList();
        bandoRepository.deleteExtraAspByTipiDataset(tipiStr);
        bandoRepository.deleteAllByTipoDatasetIn(tipi);
    }

    private BandoDto toDto(Bando b) {
        BandoDto dto = new BandoDto();
        dto.setId(b.getId());
        dto.setCodiceEsterno(b.getCodiceEsterno());
        dto.setTitolo(b.getTitolo());
        dto.setDescrizione(b.getDescrizione());
        dto.setStazioneAppaltante(b.getStazioneAppaltante());
        dto.setDenominazioneAmministrazione(b.getDenominazioneAmministrazione());
        dto.setImportoBase(b.getImportoBase());
        dto.setDataPubblicazione(b.getDataPubblicazione());
        dto.setDataScadenza(b.getDataScadenza());
        dto.setUrlOriginale(b.getUrlOriginale());
        dto.setStatoGara(b.getStatoGara());
        dto.setDataImportazione(b.getDataImportazione());
        dto.setDataAggiornamento(b.getDataAggiornamento());
        dto.setStato(b.getStato());
        dto.setTags(b.getTags());
        dto.setTipoDataset(b.getTipoDataset());
        dto.setCategoriaGara(b.getCategoriaGara());
        dto.setTipoAmministrazione(b.getTipoAmministrazione());
        dto.setRegionePa(b.getRegionePa());
        dto.setProvinciaPa(b.getProvinciaPa());
        dto.setAnnoRiferimento(b.getAnnoRiferimento());
        dto.setMeseRiferimento(b.getMeseRiferimento());
        dto.setTipoStrumento(b.getTipoStrumento());
        dto.setTipoProcedura(b.getTipoProcedura());
        dto.setCodCpv(b.getCodCpv());
        dto.setDescrizioneCpv(b.getDescrizioneCpv());
        if (b.getExtraAsp() != null) {
            var asp = b.getExtraAsp();
            dto.setOggettoAppalto(asp.getOggettoAppalto());
            dto.setCriterioAggiudicazione(asp.getCriterioAggiudicazione());
            dto.setValoreAggiudicato(asp.getValoreAggiudicato());
            dto.setComparto(asp.getComparto());
            dto.setStatoLotto(asp.getStatoLotto());
            dto.setSiglaProvincia(asp.getSiglaProvincia());
            dto.setLatitudinePa(asp.getLatitudinePa());
            dto.setLongitudinePa(asp.getLongitudinePa());
        }
        if (b.getFonte() != null) {
            dto.setFonteName(b.getFonte().getNome());
            dto.setFonteUrlSito(b.getFonte().getUrlSito());
        }
        return dto;
    }

    private TipoDataset parseTipoDataset(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return TipoDataset.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<String> filterList(List<String> list) {
        if (list == null) return List.of();
        return list.stream().filter(s -> s != null && !s.isBlank()).toList();
    }
}
