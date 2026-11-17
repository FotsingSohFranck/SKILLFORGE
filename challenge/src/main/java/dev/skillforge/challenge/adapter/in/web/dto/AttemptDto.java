package dev.skillforge.challenge.adapter.in.web.dto;

import dev.skillforge.challenge.domain.model.AttemptView;
import dev.skillforge.challenge.domain.model.ChallengeResult;
import dev.skillforge.challenge.domain.model.ChoiceView;
import dev.skillforge.challenge.domain.model.CompetencyScore;
import dev.skillforge.challenge.domain.model.NodeView;

import java.util.List;
import java.util.UUID;

/**
 * Vue d'une tentative renvoyée au client. En cours : {@code node} (sans aucune donnée de correction).
 * Terminée : {@code result}.
 */
public record AttemptDto(
        UUID attemptId,
        String status,
        String briefing,
        String objective,
        NodeDto node,
        String guidance,
        ResultDto result) {

    public static AttemptDto from(AttemptView view) {
        return new AttemptDto(
                view.attemptId(),
                view.status().name(),
                view.briefing(),
                view.objective(),
                view.node() == null ? null : NodeDto.from(view.node()),
                view.guidance() == null ? null : view.guidance().name(),
                view.result() == null ? null : ResultDto.from(view.result()));
    }

    public record NodeDto(
            String id,
            String title,
            String context,
            String event,
            String interactionType,
            List<String> competencies,
            List<ChoiceDto> choices) {

        static NodeDto from(NodeView node) {
            return new NodeDto(
                    node.id(),
                    node.title(),
                    node.context(),
                    node.event(),
                    node.interactionType() == null ? null : node.interactionType().name(),
                    node.competencies(),
                    node.choices().stream().map(ChoiceDto::from).toList());
        }
    }

    public record ChoiceDto(String id, String label) {
        static ChoiceDto from(ChoiceView choice) {
            return new ChoiceDto(choice.id(), choice.label());
        }
    }

    public record ResultDto(
            int globalScore,
            boolean passed,
            boolean reinforce,
            boolean eliminated,
            List<CompetencyScoreDto> competencies,
            List<String> triggeredCritical) {

        static ResultDto from(ChallengeResult result) {
            return new ResultDto(
                    result.globalScore(),
                    result.passed(),
                    result.reinforce(),
                    result.eliminated(),
                    result.competencyScores().stream().map(CompetencyScoreDto::from).toList(),
                    result.triggeredCritical());
        }
    }

    public record CompetencyScoreDto(String competency, int raw, int max, int percent) {
        static CompetencyScoreDto from(CompetencyScore score) {
            return new CompetencyScoreDto(score.competency(), score.raw(), score.max(), score.percent());
        }
    }
}
