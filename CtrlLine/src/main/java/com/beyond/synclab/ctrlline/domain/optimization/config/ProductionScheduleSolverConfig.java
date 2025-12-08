package com.beyond.synclab.ctrlline.domain.optimization.config;

import com.beyond.synclab.ctrlline.domain.optimization.constraint.ProductionScheduleConstraintProvider;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionScheduleSolution;
import java.util.UUID;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductionScheduleSolverConfig {
    @Bean
    public SolverConfig solverConfig() {
        return new SolverConfig()

            // ------------ Domain Model 설정 ------------
            .withSolutionClass(ProductionScheduleSolution.class)
            .withEntityClasses(ProductionPlanAssignment.class)

            // ------------ 제약조건 Provider 등록 ------------
            .withConstraintProviderClass(ProductionScheduleConstraintProvider.class)
            .withTerminationConfig(
                new TerminationConfig()
                    .withSecondsSpentLimit(2L)
                    .withUnimprovedSecondsSpentLimit(1L)
            );
    }

    @Bean
    public SolverManager<ProductionScheduleSolution, UUID> solverManager(SolverConfig solverConfig) {
        return SolverManager.create(solverConfig);
    }
}
