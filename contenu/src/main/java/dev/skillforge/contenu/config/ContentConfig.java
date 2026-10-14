package dev.skillforge.contenu.config;

import dev.skillforge.contenu.application.GetLessonService;
import dev.skillforge.contenu.application.IngestNodeContentService;
import dev.skillforge.contenu.application.LessonAuthoringService;
import dev.skillforge.contenu.application.ListNodeLessonsService;
import dev.skillforge.contenu.application.SectionAuthoringService;
import dev.skillforge.contenu.domain.port.in.GetLessonUseCase;
import dev.skillforge.contenu.domain.port.in.IngestNodeContentUseCase;
import dev.skillforge.contenu.domain.port.in.LessonAuthoringUseCase;
import dev.skillforge.contenu.domain.port.in.ListNodeLessonsUseCase;
import dev.skillforge.contenu.domain.port.in.SectionAuthoringUseCase;
import dev.skillforge.contenu.domain.port.out.LessonRepository;
import dev.skillforge.contenu.domain.port.out.LessonWriteStore;
import dev.skillforge.contenu.domain.port.out.NodeContentImporter;
import dev.skillforge.contenu.domain.port.out.SectionRepository;
import dev.skillforge.contenu.domain.port.out.SectionWriteStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Câblage du contexte Contenu : services applicatifs purs exposés en beans. */
@Configuration
public class ContentConfig {

    @Bean
    ListNodeLessonsUseCase listNodeLessonsUseCase(LessonRepository lessons) {
        return new ListNodeLessonsService(lessons);
    }

    @Bean
    GetLessonUseCase getLessonUseCase(LessonRepository lessons, SectionRepository sections) {
        return new GetLessonService(lessons, sections);
    }

    @Bean
    IngestNodeContentUseCase ingestNodeContentUseCase(NodeContentImporter importer) {
        return new IngestNodeContentService(importer);
    }

    @Bean
    LessonAuthoringUseCase lessonAuthoringUseCase(LessonWriteStore store) {
        return new LessonAuthoringService(store);
    }

    @Bean
    SectionAuthoringUseCase sectionAuthoringUseCase(SectionWriteStore store) {
        return new SectionAuthoringService(store);
    }
}
