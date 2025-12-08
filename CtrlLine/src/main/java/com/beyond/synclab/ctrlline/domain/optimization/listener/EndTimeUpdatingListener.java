package com.beyond.synclab.ctrlline.domain.optimization.listener;

import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionScheduleSolution;
import java.time.LocalDateTime;
import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public class EndTimeUpdatingListener
    implements VariableListener<ProductionScheduleSolution, ProductionPlanAssignment> {

    @Override
    public void beforeEntityAdded(ScoreDirector<ProductionScheduleSolution> scoreDirector,
        ProductionPlanAssignment assignment) {
        // no-op
    }

    @Override
    public void afterEntityAdded(ScoreDirector<ProductionScheduleSolution> scoreDirector,
        ProductionPlanAssignment assignment) {
        updateEndTime(scoreDirector, assignment);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<ProductionScheduleSolution> scoreDirector,
        ProductionPlanAssignment assignment) {
        // no-op
    }

    @Override
    public void afterVariableChanged(ScoreDirector<ProductionScheduleSolution> scoreDirector,
        ProductionPlanAssignment assignment) {
        updateEndTime(scoreDirector, assignment);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<ProductionScheduleSolution> scoreDirector,
        ProductionPlanAssignment assignment) {
        // no-op
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<ProductionScheduleSolution> scoreDirector,
        ProductionPlanAssignment assignment) {
        // no-op
    }

    private void updateEndTime(ScoreDirector<ProductionScheduleSolution> scoreDirector,
        ProductionPlanAssignment assignment) {
        LocalDateTime start = assignment.getStartTime();
        if (start == null) {
            return;
        }

        LocalDateTime newEnd = start
            .plusMinutes(assignment.getDurationMinutes());

        scoreDirector.beforeVariableChanged(assignment, "endTime");
        assignment.setEndTime(newEnd);
        scoreDirector.afterVariableChanged(assignment, "endTime");
    }
}