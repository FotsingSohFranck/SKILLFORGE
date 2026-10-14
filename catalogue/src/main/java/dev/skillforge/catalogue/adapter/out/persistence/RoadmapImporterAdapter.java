package dev.skillforge.catalogue.adapter.out.persistence;

import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.port.in.RoadmapImport;
import dev.skillforge.catalogue.domain.port.out.RoadmapImporter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptateur R2DBC : persistance <b>idempotente</b> d'une roadmap importée.
 * Upsert par clés naturelles via {@code ON CONFLICT} (slug de roadmap/nœud, code de certif)
 * ou appariement manuel (phases par titre, domaines par nom), en <b>préservant les id</b>.
 * Opération transactionnelle (tout ou rien).
 */
@Component
public class RoadmapImporterAdapter implements RoadmapImporter {

    private final DatabaseClient db;

    public RoadmapImporterAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    @Transactional
    public Mono<RoadmapId> importRoadmap(RoadmapImport c) {
        String kind = c.kind() == null ? "TRACK" : c.kind().trim().toUpperCase();
        return upsertRoadmap(c, kind)
                .flatMap(roadmapId -> upsertCertification(c, kind, roadmapId)
                        .flatMap(domainIds -> upsertPhases(c, roadmapId)
                                .flatMap(phaseIds -> upsertNodes(c, roadmapId, phaseIds, domainIds)
                                        .flatMap(nodeIds -> upsertEdges(c, roadmapId, nodeIds)
                                                .thenReturn(new RoadmapId(roadmapId))))));
    }

    // ---------- Roadmap ----------
    private Mono<UUID> upsertRoadmap(RoadmapImport c, String kind) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                INSERT INTO roadmaps(slug, title, summary, kind, position)
                VALUES (:slug, :title, :summary, :kind, :position)
                ON CONFLICT (slug) DO UPDATE SET
                    title = EXCLUDED.title, summary = EXCLUDED.summary,
                    kind = EXCLUDED.kind, position = EXCLUDED.position
                RETURNING id
                """)
                .bind("slug", c.slug())
                .bind("title", c.title())
                .bind("kind", kind)
                .bind("position", c.position() == null ? 0 : c.position());
        spec = bindNullable(spec, "summary", c.summary(), String.class);
        return spec.map((row, meta) -> row.get("id", UUID.class)).one();
    }

    // ---------- Certification + domaines d'examen ----------
    private Mono<Map<String, UUID>> upsertCertification(RoadmapImport c, String kind, UUID roadmapId) {
        if (!"CERTIFICATION".equals(kind) || c.certification() == null) {
            return Mono.just(Map.of());
        }
        RoadmapImport.Certification cert = c.certification();
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                INSERT INTO certifications(roadmap_id, code, exam_format, duration_minutes, passing_score)
                VALUES (:rid, :code, :fmt, :dur, :pass)
                ON CONFLICT (code) DO UPDATE SET
                    roadmap_id = EXCLUDED.roadmap_id, exam_format = EXCLUDED.exam_format,
                    duration_minutes = EXCLUDED.duration_minutes, passing_score = EXCLUDED.passing_score
                RETURNING id
                """)
                .bind("rid", roadmapId)
                .bind("code", cert.code())
                .bind("fmt", cert.examFormat() == null ? "MCQ" : cert.examFormat().trim().toUpperCase())
                .bind("dur", cert.durationMinutes() == null ? 0 : cert.durationMinutes());
        spec = bindNullable(spec, "pass", cert.passingScore(), Double.class);
        return spec.map((row, meta) -> row.get("id", UUID.class)).one()
                .flatMap(certId -> Flux.fromIterable(safe(cert.domains()))
                        .concatMap(d -> upsertExamDomain(certId, d)
                                .map(id -> Map.entry(d.name(), id)))
                        .collectMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Mono<UUID> upsertExamDomain(UUID certId, RoadmapImport.ExamDomain d) {
        return db.sql("SELECT id FROM exam_domains WHERE certification_id = :cid AND name = :name")
                .bind("cid", certId).bind("name", d.name())
                .map((row, meta) -> row.get("id", UUID.class)).one()
                .flatMap(existing -> db.sql(
                                "UPDATE exam_domains SET weight = :w, position = :p WHERE id = :id")
                        .bind("w", d.weight() == null ? 0 : d.weight())
                        .bind("p", d.position() == null ? 0 : d.position())
                        .bind("id", existing).fetch().rowsUpdated().thenReturn(existing))
                .switchIfEmpty(db.sql("""
                                INSERT INTO exam_domains(certification_id, name, weight, position)
                                VALUES (:cid, :name, :w, :p) RETURNING id
                                """)
                        .bind("cid", certId).bind("name", d.name())
                        .bind("w", d.weight() == null ? 0 : d.weight())
                        .bind("p", d.position() == null ? 0 : d.position())
                        .map((row, meta) -> row.get("id", UUID.class)).one());
    }

    // ---------- Phases (appariées par titre) ----------
    private Mono<Map<String, UUID>> upsertPhases(RoadmapImport c, UUID roadmapId) {
        return Flux.fromIterable(safe(c.phases()))
                .concatMap(p -> upsertPhase(roadmapId, p).map(id -> Map.entry(p.title(), id)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<UUID> upsertPhase(UUID roadmapId, RoadmapImport.Phase p) {
        int position = p.position() == null ? 0 : p.position();
        return db.sql("SELECT id FROM phases WHERE roadmap_id = :rid AND title = :title")
                .bind("rid", roadmapId).bind("title", p.title())
                .map((row, meta) -> row.get("id", UUID.class)).one()
                .flatMap(existing -> db.sql("UPDATE phases SET position = :pos WHERE id = :id")
                        .bind("pos", position).bind("id", existing)
                        .fetch().rowsUpdated().thenReturn(existing))
                .switchIfEmpty(db.sql(
                                "INSERT INTO phases(roadmap_id, title, position) VALUES (:rid, :title, :pos) RETURNING id")
                        .bind("rid", roadmapId).bind("title", p.title()).bind("pos", position)
                        .map((row, meta) -> row.get("id", UUID.class)).one());
    }

    // ---------- Nœuds (upsert par (roadmap_id, slug)) ----------
    private Mono<Map<String, UUID>> upsertNodes(RoadmapImport c, UUID roadmapId,
                                                Map<String, UUID> phaseIds, Map<String, UUID> domainIds) {
        return Flux.fromIterable(safe(c.nodes()))
                .concatMap(n -> upsertNode(roadmapId, n, phaseIds, domainIds)
                        .map(id -> Map.entry(n.slug(), id)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<UUID> upsertNode(UUID roadmapId, RoadmapImport.Node n,
                                  Map<String, UUID> phaseIds, Map<String, UUID> domainIds) {
        UUID phaseId = n.phase() == null ? null : phaseIds.get(n.phase());
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                INSERT INTO nodes(roadmap_id, phase_id, slug, title, summary, importance, estimated_minutes, position)
                VALUES (:rid, :phase, :slug, :title, :summary, :importance, :estimated, :position)
                ON CONFLICT (roadmap_id, slug) DO UPDATE SET
                    phase_id = EXCLUDED.phase_id, title = EXCLUDED.title, summary = EXCLUDED.summary,
                    importance = EXCLUDED.importance, estimated_minutes = EXCLUDED.estimated_minutes,
                    position = EXCLUDED.position
                RETURNING id
                """)
                .bind("rid", roadmapId)
                .bind("slug", n.slug())
                .bind("title", n.title())
                .bind("importance", n.importance() == null ? "RECOMMENDED" : n.importance().trim().toUpperCase())
                .bind("position", n.position() == null ? 0 : n.position());
        spec = bindNullable(spec, "phase", phaseId, UUID.class);
        spec = bindNullable(spec, "summary", n.summary(), String.class);
        spec = bindNullable(spec, "estimated", n.estimatedMinutes(), Integer.class);
        return spec.map((row, meta) -> row.get("id", UUID.class)).one()
                .flatMap(nodeId -> replaceNodeExamDomains(nodeId, n, domainIds).thenReturn(nodeId));
    }

    private Mono<Void> replaceNodeExamDomains(UUID nodeId, RoadmapImport.Node n, Map<String, UUID> domainIds) {
        return db.sql("DELETE FROM node_exam_domains WHERE node_id = :nid")
                .bind("nid", nodeId).fetch().rowsUpdated()
                .thenMany(Flux.fromIterable(safe(n.examDomains())))
                .concatMap(ed -> {
                    UUID domainId = domainIds.get(ed.domain());
                    if (domainId == null) {
                        return Mono.empty();
                    }
                    return db.sql("""
                                    INSERT INTO node_exam_domains(node_id, exam_domain_id, weight)
                                    VALUES (:nid, :did, :w)
                                    ON CONFLICT (node_id, exam_domain_id) DO UPDATE SET weight = EXCLUDED.weight
                                    """)
                            .bind("nid", nodeId).bind("did", domainId)
                            .bind("w", ed.weight() == null ? 0 : ed.weight())
                            .fetch().rowsUpdated();
                })
                .then();
    }

    // ---------- Arêtes (upsert par (source, target, kind)) ----------
    private Mono<Void> upsertEdges(RoadmapImport c, UUID roadmapId, Map<String, UUID> nodeIds) {
        return Flux.fromIterable(safe(c.edges()))
                .concatMap(e -> {
                    UUID source = nodeIds.get(e.source());
                    UUID target = nodeIds.get(e.target());
                    if (source == null || target == null) {
                        return Mono.empty();
                    }
                    return db.sql("""
                                    INSERT INTO edges(roadmap_id, source_node_id, target_node_id, kind)
                                    VALUES (:rid, :src, :tgt, :kind)
                                    ON CONFLICT (source_node_id, target_node_id, kind) DO NOTHING
                                    """)
                            .bind("rid", roadmapId).bind("src", source).bind("tgt", target)
                            .bind("kind", e.kind() == null ? "PREREQ" : e.kind().trim().toUpperCase())
                            .fetch().rowsUpdated();
                })
                .then();
    }

    // ---------- Utilitaires ----------
    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec, String name, Object value, Class<?> type) {
        return value != null ? spec.bind(name, value) : spec.bindNull(name, type);
    }
}
