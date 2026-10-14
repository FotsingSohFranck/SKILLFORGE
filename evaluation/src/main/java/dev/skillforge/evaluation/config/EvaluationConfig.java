package dev.skillforge.evaluation.config;

import dev.skillforge.evaluation.application.GetChallengeService;
import dev.skillforge.evaluation.application.GetQuizService;
import dev.skillforge.evaluation.application.IngestChallengeService;
import dev.skillforge.evaluation.application.IngestQuizService;
import dev.skillforge.evaluation.application.ListCertificationExamsService;
import dev.skillforge.evaluation.application.ListNodeChallengesService;
import dev.skillforge.evaluation.application.SubmitAttemptService;
import dev.skillforge.evaluation.application.SubmitChallengeService;
import dev.skillforge.evaluation.domain.port.in.GetChallengeUseCase;
import dev.skillforge.evaluation.domain.port.in.GetQuizUseCase;
import dev.skillforge.evaluation.domain.port.in.IngestChallengeUseCase;
import dev.skillforge.evaluation.domain.port.in.IngestQuizUseCase;
import dev.skillforge.evaluation.domain.port.in.ListCertificationExamsUseCase;
import dev.skillforge.evaluation.domain.port.in.ListNodeChallengesUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmitAttemptUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmitChallengeUseCase;
import dev.skillforge.evaluation.domain.port.out.AttemptStore;
import dev.skillforge.evaluation.domain.port.out.ChallengeImporter;
import dev.skillforge.evaluation.domain.port.out.ChallengeRepository;
import dev.skillforge.evaluation.domain.port.out.QuizImporter;
import dev.skillforge.evaluation.domain.port.out.QuizRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Câblage du contexte Évaluation : services applicatifs purs exposés en beans. */
@Configuration
public class EvaluationConfig {

    @Bean
    GetQuizUseCase getQuizUseCase(QuizRepository quizzes) {
        return new GetQuizService(quizzes);
    }

    @Bean
    SubmitAttemptUseCase submitAttemptUseCase(QuizRepository quizzes, AttemptStore attempts) {
        return new SubmitAttemptService(quizzes, attempts);
    }

    @Bean
    IngestQuizUseCase ingestQuizUseCase(QuizImporter importer) {
        return new IngestQuizService(importer);
    }

    @Bean
    GetChallengeUseCase getChallengeUseCase(ChallengeRepository challenges) {
        return new GetChallengeService(challenges);
    }

    @Bean
    SubmitChallengeUseCase submitChallengeUseCase(ChallengeRepository challenges) {
        return new SubmitChallengeService(challenges);
    }

    @Bean
    IngestChallengeUseCase ingestChallengeUseCase(ChallengeImporter importer) {
        return new IngestChallengeService(importer);
    }

    @Bean
    ListNodeChallengesUseCase listNodeChallengesUseCase(ChallengeRepository challenges) {
        return new ListNodeChallengesService(challenges);
    }

    @Bean
    ListCertificationExamsUseCase listCertificationExamsUseCase(
            dev.skillforge.evaluation.domain.port.out.ExamRepository exams) {
        return new ListCertificationExamsService(exams);
    }

    @Bean
    dev.skillforge.evaluation.domain.port.in.IngestExamUseCase ingestExamUseCase(
            dev.skillforge.evaluation.domain.port.out.ExamImporter importer) {
        return new dev.skillforge.evaluation.application.IngestExamService(importer);
    }

    @Bean
    dev.skillforge.evaluation.domain.port.in.GetExamUseCase getExamUseCase(
            dev.skillforge.evaluation.domain.port.out.ExamRepository exams) {
        return new dev.skillforge.evaluation.application.GetExamService(exams);
    }

    @Bean
    dev.skillforge.evaluation.domain.port.in.StartExamUseCase startExamUseCase(
            dev.skillforge.evaluation.domain.port.out.ExamRepository exams,
            dev.skillforge.evaluation.domain.port.out.ExamAttemptStore attempts) {
        return new dev.skillforge.evaluation.application.StartExamService(exams, attempts);
    }

    @Bean
    dev.skillforge.evaluation.domain.port.in.SubmitExamUseCase submitExamUseCase(
            dev.skillforge.evaluation.domain.port.out.ExamRepository exams,
            dev.skillforge.evaluation.domain.port.out.ExamAttemptStore attempts,
            dev.skillforge.evaluation.domain.port.out.EvaluationEventPublisher events) {
        return new dev.skillforge.evaluation.application.SubmitExamService(exams, attempts, events);
    }

    @Bean
    dev.skillforge.evaluation.domain.port.in.GetExamHistoryUseCase getExamHistoryUseCase(
            dev.skillforge.evaluation.domain.port.out.ExamAttemptStore attempts) {
        return new dev.skillforge.evaluation.application.GetExamHistoryService(attempts);
    }

    @Bean
    dev.skillforge.evaluation.domain.port.in.GetRevisionUseCase getRevisionUseCase(
            dev.skillforge.evaluation.domain.port.out.ExamAttemptStore attempts) {
        return new dev.skillforge.evaluation.application.GetRevisionService(attempts);
    }
}
