package it.bandimonitor.service;

import it.bandimonitor.entity.Bando;
import it.bandimonitor.entity.TipoDataset;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public final class BandoSpec {

    private BandoSpec() {
    }

    private static final Specification<Bando> NEUTRO = (root, query, cb) -> null;

    public static Specification<Bando> hasKeyword(String q, boolean requireAll) {
        if (q == null || q.isBlank()) return NEUTRO;
        List<String> keywords = Arrays.stream(q.trim().toLowerCase().split("\\s+"))
                .filter(w -> w.length() >= 3)
                .toList();
        if (keywords.isEmpty()) return NEUTRO;
        return (root, query, cb) -> {
            Predicate[] predicates = keywords.stream().map(word -> {
                String pattern = "%" + word + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("titolo")), pattern),
                        cb.like(cb.lower(root.get("descrizione")), pattern),
                        cb.like(cb.lower(root.get("denominazioneAmministrazione")), pattern)
                );
            }).toArray(Predicate[]::new);
            // Precisa: tutte le parole devono comparire (AND)
            // Normale/Ampia: basta una parola (OR) — utile quando Hermes espande con sinonimi
            return requireAll ? cb.and(predicates) : cb.or(predicates);
        };
    }

    public static Specification<Bando> hasTipoDataset(TipoDataset tipo) {
        if (tipo == null) return NEUTRO;
        return (root, query, cb) -> cb.equal(root.get("tipoDataset"), tipo);
    }

    public static Specification<Bando> hasCategoria(List<String> categorie) {
        if (categorie == null || categorie.isEmpty()) return NEUTRO;
        List<String> nonempty = categorie.stream().filter(c -> c != null && !c.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> root.get("categoriaGara").in(nonempty);
    }

    public static Specification<Bando> hasStato(List<String> stati) {
        if (stati == null || stati.isEmpty()) return NEUTRO;
        List<String> nonempty = stati.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> root.get("statoGara").in(nonempty);
    }

    public static Specification<Bando> hasRegione(List<String> regioni) {
        if (regioni == null || regioni.isEmpty()) return NEUTRO;
        List<String> nonempty = regioni.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> root.get("regionePa").in(nonempty);
    }

    public static Specification<Bando> hasAnno(Integer anno) {
        if (anno == null) return NEUTRO;
        return (root, query, cb) -> cb.equal(root.get("annoRiferimento"), anno);
    }

    public static Specification<Bando> hasTipoAmministrazione(List<String> tipiAmm) {
        if (tipiAmm == null || tipiAmm.isEmpty()) return NEUTRO;
        List<String> nonempty = tipiAmm.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> root.get("tipoAmministrazione").in(nonempty);
    }

    public static Specification<Bando> hasTipoStrumento(List<String> tipiStr) {
        if (tipiStr == null || tipiStr.isEmpty()) return NEUTRO;
        List<String> nonempty = tipiStr.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> root.get("tipoStrumento").in(nonempty);
    }

    public static Specification<Bando> excludeStato(List<String> stati) {
        if (stati == null || stati.isEmpty()) return NEUTRO;
        List<String> nonempty = stati.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> {
            Predicate[] p = nonempty.stream()
                .map(s -> (Predicate) cb.or(cb.isNull(root.get("statoGara")), cb.notEqual(root.get("statoGara"), s)))
                .toArray(Predicate[]::new);
            return cb.and(p);
        };
    }

    public static Specification<Bando> excludeRegione(List<String> regioni) {
        if (regioni == null || regioni.isEmpty()) return NEUTRO;
        List<String> nonempty = regioni.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> {
            Predicate[] p = nonempty.stream()
                .map(s -> (Predicate) cb.or(cb.isNull(root.get("regionePa")), cb.notEqual(root.get("regionePa"), s)))
                .toArray(Predicate[]::new);
            return cb.and(p);
        };
    }

    public static Specification<Bando> excludeTipoAmministrazione(List<String> tipiAmm) {
        if (tipiAmm == null || tipiAmm.isEmpty()) return NEUTRO;
        List<String> nonempty = tipiAmm.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> {
            Predicate[] p = nonempty.stream()
                .map(s -> (Predicate) cb.or(cb.isNull(root.get("tipoAmministrazione")), cb.notEqual(root.get("tipoAmministrazione"), s)))
                .toArray(Predicate[]::new);
            return cb.and(p);
        };
    }

    public static Specification<Bando> excludeTipoStrumento(List<String> tipiStr) {
        if (tipiStr == null || tipiStr.isEmpty()) return NEUTRO;
        List<String> nonempty = tipiStr.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> {
            Predicate[] p = nonempty.stream()
                .map(s -> (Predicate) cb.or(cb.isNull(root.get("tipoStrumento")), cb.notEqual(root.get("tipoStrumento"), s)))
                .toArray(Predicate[]::new);
            return cb.and(p);
        };
    }

    public static Specification<Bando> hasScadenzaDal(LocalDate dal) {
        if (dal == null) return NEUTRO;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataScadenza"), dal);
    }

    public static Specification<Bando> hasScadenzaAl(LocalDate al) {
        if (al == null) return NEUTRO;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataScadenza"), al);
    }

    public static Specification<Bando> hasCodCpv(List<String> codCpv) {
        if (codCpv == null || codCpv.isEmpty()) return NEUTRO;
        List<String> nonempty = codCpv.stream().filter(c -> c != null && !c.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> root.get("codCpv").in(nonempty);
    }

    public static Specification<Bando> hasImportoDa(BigDecimal da) {
        if (da == null) return NEUTRO;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("importoBase"), da);
    }

    public static Specification<Bando> hasImportoA(BigDecimal a) {
        if (a == null) return NEUTRO;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("importoBase"), a);
    }

    public static Specification<Bando> hasStazioneAppaltante(List<String> valori) {
        if (valori == null || valori.isEmpty()) return NEUTRO;
        List<String> nonempty = valori.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> root.get("stazioneAppaltante").in(nonempty);
    }

    public static Specification<Bando> excludeStazioneAppaltante(List<String> valori) {
        if (valori == null || valori.isEmpty()) return NEUTRO;
        List<String> nonempty = valori.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (nonempty.isEmpty()) return NEUTRO;
        return (root, query, cb) -> {
            Predicate[] p = nonempty.stream()
                .map(s -> (Predicate) cb.or(cb.isNull(root.get("stazioneAppaltante")), cb.notEqual(root.get("stazioneAppaltante"), s)))
                .toArray(Predicate[]::new);
            return cb.and(p);
        };
    }

    public static Specification<Bando> scadeEntro(Integer giorni) {
        if (giorni == null) return NEUTRO;
        LocalDate oggi = LocalDate.now();
        LocalDate limite = oggi.plusDays(giorni);
        return (root, query, cb) -> cb.between(root.get("dataScadenza"), oggi, limite);
    }

    public static Specification<Bando> includeSorgenti(Boolean enableConsip, Boolean enableAnac) {
        boolean incC = enableConsip == null || enableConsip;
        boolean incA = enableAnac == null || enableAnac;
        if (incC && incA) return NEUTRO;
        List<TipoDataset> allowed = new java.util.ArrayList<>();
        if (incC) { allowed.add(TipoDataset.GARE_ASP); allowed.add(TipoDataset.BANDI_GARE); }
        if (incA) allowed.add(TipoDataset.ANAC_CIG);
        if (allowed.isEmpty()) return (root, query, cb) -> cb.disjunction();
        return (root, query, cb) -> root.get("tipoDataset").in(allowed);
    }

    public static Specification<Bando> soloNonScadute(boolean attivo) {
        if (!attivo) return NEUTRO;
        LocalDate oggi = LocalDate.now();
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("dataScadenza")),
                cb.greaterThanOrEqualTo(root.get("dataScadenza"), oggi)
        );
    }
}
