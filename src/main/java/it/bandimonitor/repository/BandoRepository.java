package it.bandimonitor.repository;

import it.bandimonitor.entity.Bando;
import it.bandimonitor.entity.StatoBando;
import it.bandimonitor.entity.TipoDataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface BandoRepository extends JpaRepository<Bando, Long>, JpaSpecificationExecutor<Bando> {

    Optional<Bando> findByCodiceEsterno(String codiceEsterno);

    Page<Bando> findByStato(StatoBando stato, Pageable pageable);

    @Query("SELECT DISTINCT b.categoriaGara FROM Bando b WHERE b.categoriaGara IS NOT NULL ORDER BY b.categoriaGara")
    List<String> findDistinctCategorie();

    @Query("SELECT DISTINCT b.statoGara FROM Bando b WHERE b.statoGara IS NOT NULL ORDER BY b.statoGara")
    List<String> findDistinctStatiGara();

    @Query("SELECT DISTINCT b.regionePa FROM Bando b WHERE b.regionePa IS NOT NULL ORDER BY b.regionePa")
    List<String> findDistinctRegioni();

    @Query("SELECT DISTINCT b.annoRiferimento FROM Bando b WHERE b.annoRiferimento IS NOT NULL ORDER BY b.annoRiferimento DESC")
    List<Integer> findDistinctAnni();

    @Query("SELECT DISTINCT b.tipoAmministrazione FROM Bando b WHERE b.tipoAmministrazione IS NOT NULL ORDER BY b.tipoAmministrazione")
    List<String> findDistinctTipiAmministrazione();

    @Query("SELECT DISTINCT b.tipoStrumento FROM Bando b WHERE b.tipoStrumento IS NOT NULL ORDER BY b.tipoStrumento")
    List<String> findDistinctTipiStrumento();

    @Query("SELECT b.codCpv, b.descrizioneCpv, COUNT(b) FROM Bando b WHERE b.codCpv IS NOT NULL GROUP BY b.codCpv, b.descrizioneCpv ORDER BY COUNT(b) DESC")
    List<Object[]> findCpvConConteggio();

    @Query(value = """
            SELECT sa.stazione_appaltante, sa.regione_pa, sa.tipo_amministrazione, sa.cnt
            FROM (
                SELECT b.stazione_appaltante, b.regione_pa, b.tipo_amministrazione,
                       COUNT(*) AS cnt,
                       ROW_NUMBER() OVER (PARTITION BY b.stazione_appaltante ORDER BY COUNT(*) DESC) AS rn
                FROM bandi b
                WHERE b.tipo_dataset = 'ANAC_CIG' AND b.stazione_appaltante IS NOT NULL
                GROUP BY b.stazione_appaltante, b.regione_pa, b.tipo_amministrazione
            ) sa
            WHERE sa.rn = 1
            ORDER BY sa.cnt DESC
            LIMIT 1000
            """, nativeQuery = true)
    List<Object[]> findStazioniAppaltantiAnacConDettagli();

    @Query(value = """
            SELECT b.stazione_appaltante, COUNT(*) AS cnt
            FROM bandi b
            WHERE b.tipo_dataset IN ('GARE_ASP', 'BANDI_GARE')
              AND b.stazione_appaltante IS NOT NULL
            GROUP BY b.stazione_appaltante
            ORDER BY cnt DESC
            LIMIT 500
            """, nativeQuery = true)
    List<Object[]> findStazioniAppaltantiConsipConConteggio();

    @Modifying
    @Transactional
    @Query("DELETE FROM Bando b WHERE b.tipoDataset = :tipo AND b.annoRiferimento = :anno")
    void deleteByTipoDatasetAndAnnoRiferimento(@Param("tipo") TipoDataset tipo, @Param("anno") Integer anno);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM bando_extra_asp WHERE bando_id IN (SELECT id FROM bandi WHERE tipo_dataset IN :tipi)", nativeQuery = true)
    void deleteExtraAspByTipiDataset(@Param("tipi") Collection<String> tipi);

    @Modifying
    @Transactional
    @Query("DELETE FROM Bando b WHERE b.tipoDataset IN :tipi")
    void deleteAllByTipoDatasetIn(@Param("tipi") Collection<TipoDataset> tipi);

    @Query("SELECT COUNT(b) FROM Bando b WHERE b.tipoDataset IN :tipi")
    long countByTipoDatasetIn(@Param("tipi") Collection<TipoDataset> tipi);

    @Query("SELECT MIN(b.annoRiferimento) FROM Bando b WHERE b.tipoDataset IN :tipi")
    Integer findMinAnnoByTipiDataset(@Param("tipi") Collection<TipoDataset> tipi);

    @Query("SELECT MAX(b.annoRiferimento) FROM Bando b WHERE b.tipoDataset IN :tipi")
    Integer findMaxAnnoByTipiDataset(@Param("tipi") Collection<TipoDataset> tipi);

    @Modifying
    @Transactional
    @Query("DELETE FROM Bando b WHERE b.tipoDataset = :tipo AND b.annoRiferimento = :anno AND b.meseRiferimento = :mese")
    void deleteByTipoDatasetAndAnnoRiferimentoAndMeseRiferimento(
            @Param("tipo") TipoDataset tipo, @Param("anno") Integer anno, @Param("mese") Integer mese);
}
