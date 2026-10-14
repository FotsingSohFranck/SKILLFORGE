package dev.skillforge.catalogue.adapter.out.persistence;

import dev.skillforge.catalogue.domain.model.Certification;
import dev.skillforge.catalogue.domain.model.ExamDomain;
import dev.skillforge.catalogue.domain.port.out.CertificationRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lecture ciblée de la certification d'un parcours + ses domaines (deux requêtes assemblées).
 * Adaptateur DatabaseClient dédié (hors machinerie entités/mapper du reste du contexte).
 */
@Component
public class CertificationRepositoryAdapter implements CertificationRepository {

    private final DatabaseClient db;

    public CertificationRepositoryAdapter(DatabaseClient db) {
        this.db = db;
    }

    private static final String SELECT_CERT =
            "SELECT c.id, c.roadmap_id, c.code, c.exam_format, c.duration_minutes, c.passing_score "
                    + "FROM certifications c ";

    @Override
    public Mono<Certification> findByRoadmapSlug(String slug) {
        return assemble(db.sql(SELECT_CERT + "JOIN roadmaps r ON r.id = c.roadmap_id WHERE r.slug = :slug")
                .bind("slug", slug));
    }

    @Override
    public Mono<Certification> findById(UUID id) {
        return assemble(db.sql(SELECT_CERT + "WHERE c.id = :id").bind("id", id));
    }

    private Mono<Certification> assemble(org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec spec) {
        return spec.map((row, meta) -> new Object[]{
                        row.get("id", UUID.class),
                        row.get("roadmap_id", UUID.class),
                        row.get("code", String.class),
                        row.get("exam_format", String.class),
                        row.get("duration_minutes", Integer.class),
                        row.get("passing_score", BigDecimal.class)})
                .one()
                .flatMap(c -> domainsOf((UUID) c[0]).map(domains -> new Certification(
                        (UUID) c[0], (UUID) c[1], (String) c[2], (String) c[3],
                        (Integer) c[4], c[5] == null ? null : ((BigDecimal) c[5]).doubleValue(),
                        domains)));
    }

    private Mono<java.util.List<ExamDomain>> domainsOf(UUID certificationId) {
        return db.sql("SELECT id, name, weight, position FROM exam_domains "
                        + "WHERE certification_id = :cid ORDER BY position")
                .bind("cid", certificationId)
                .map((row, meta) -> new ExamDomain(
                        row.get("id", UUID.class),
                        row.get("name", String.class),
                        row.get("weight", BigDecimal.class).doubleValue(),
                        row.get("position", Integer.class)))
                .all()
                .collectList();
    }
}
