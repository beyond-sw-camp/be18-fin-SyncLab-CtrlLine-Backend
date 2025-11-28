package com.beyond.synclab.ctrlline.domain.lot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock
    private LotRepository lotRepository;

    private LotService lotService;

    @BeforeEach
    void setUp() {
        lotService = new LotService(lotRepository);
    }

    @Test
    void findByProductionPlanId_returnsEmptyWhenPlanIdNull() {
        assertThat(lotService.findByProductionPlanId(null)).isEmpty();
    }

    @Test
    void getByProductionPlanId_returnsEntity() {
        Lots lot = Lots.builder().id(1L).productionPlanId(20L).lotNo("L1").build();
        when(lotRepository.findByProductionPlanId(20L)).thenReturn(Optional.of(lot));

        Lots result = lotService.getByProductionPlanId(20L);

        assertThat(result).isEqualTo(lot);
    }

    @Test
    void getByProductionPlanId_throwsWhenNotFound() {
        when(lotRepository.findByProductionPlanId(30L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lotService.getByProductionPlanId(30L))
                .isInstanceOf(LotNotFoundException.class);
    }
}
