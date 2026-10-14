package dev.skillforge.contenu.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.in.NodeContentImport;
import dev.skillforge.contenu.domain.port.out.NodeContentImporter;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

class IngestNodeContentServiceTest {

    private final AtomicInteger imports = new AtomicInteger();
    private final NodeContentImporter importer = command -> {
        imports.incrementAndGet();
        return Mono.just(command.lessons() == null ? 0 : command.lessons().size());
    };
    private final IngestNodeContentService service = new IngestNodeContentService(importer);

    private NodeContentImport valid() {
        return new NodeContentImport(new NodeId(UUID.randomUUID()), List.of(
                new NodeContentImport.LessonImport("intro", "Introduction", 0, List.of(
                        new NodeContentImport.SectionImport("CONCEPT", 0, "Notion", "texte", null, null, null, null),
                        new NodeContentImport.SectionImport("CODE", 1, "Exemple", "code", "java", null, null, null)))));
    }

    @Test
    void import_valide_delegue_et_renvoie_le_nombre_de_lecons() {
        StepVerifier.create(service.ingest(valid())).expectNext(1).verifyComplete();
    }

    @Test
    void rejette_slug_de_lecon_en_double() {
        NodeContentImport bad = new NodeContentImport(new NodeId(UUID.randomUUID()), List.of(
                new NodeContentImport.LessonImport("dup", "A", 0, List.of()),
                new NodeContentImport.LessonImport("dup", "B", 1, List.of())));
        expectInvalid(bad);
    }

    @Test
    void rejette_type_de_section_invalide() {
        NodeContentImport bad = new NodeContentImport(new NodeId(UUID.randomUUID()), List.of(
                new NodeContentImport.LessonImport("intro", "Intro", 0, List.of(
                        new NodeContentImport.SectionImport("SUPER", 0, null, null, null, null, null, null)))));
        expectInvalid(bad);
    }

    @Test
    void rejette_positions_de_section_en_double() {
        NodeContentImport bad = new NodeContentImport(new NodeId(UUID.randomUUID()), List.of(
                new NodeContentImport.LessonImport("intro", "Intro", 0, List.of(
                        new NodeContentImport.SectionImport("CONCEPT", 0, null, "a", null, null, null, null),
                        new NodeContentImport.SectionImport("CODE", 0, null, "b", "java", null, null, null)))));
        expectInvalid(bad);
    }

    private void expectInvalid(NodeContentImport bad) {
        StepVerifier.create(service.ingest(bad))
                .expectErrorMatches(e -> e instanceof BusinessRuleException b
                        && b.code().equals("INVALID_CONTENT"))
                .verify();
    }
}
