package com.beyond.synclab.ctrlline.domain.serial;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.serial.dto.response.GetLotSerialListResponseDto;
import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
import com.beyond.synclab.ctrlline.domain.serial.service.SerialServiceImpl;
import com.beyond.synclab.ctrlline.domain.serial.storage.SerialStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerialServiceTest {

    @Mock
    private ItemSerialRepository itemSerialRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private SerialStorageService storageService;

    @InjectMocks
    private SerialServiceImpl serialServiceImpl;

    // -------------------------------------------------------------
    // 성공 테스트
    // -------------------------------------------------------------
    @Test
    @DisplayName("시리얼 리스트 조회 성공")
    void getSerialList_success() {

        Long lotId = 1L;

        Lots lot = Lots.builder()
                .id(lotId)
                .lotNo("2025/11/26-7")
                .itemId(10L)
                .productionPlanId(100L)
                .build();

        ItemSerials serial = ItemSerials.builder()
                .id(99L)
                .lotId(lotId)
                .serialFilePath("s3://ctrlline-bucket/serials/test/serials.gz")
                .build();

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(itemSerialRepository.findByLotId(lotId)).thenReturn(Optional.of(serial));

        // torageService.read() mocking — gzip mocking 제거
        when(storageService.read("s3://ctrlline-bucket/serials/test/serials.gz"))
                .thenReturn(List.of("S001", "S002"));

        // when
        GetLotSerialListResponseDto dto = serialServiceImpl.getSerialListByLotId(lotId);

        // then
        assertThat(dto.getLotNo()).isEqualTo("2025/11/26-7");
        assertThat(dto.getSerialList()).containsExactly("S001", "S002");

        verify(storageService, times(1)).read("s3://ctrlline-bucket/serials/test/serials.gz"); // 추가 검증
    }

    // -------------------------------------------------------------
    // LOT 미존재
    // -------------------------------------------------------------
    @Test
    @DisplayName("LOT가 존재하지 않으면 LotNotFoundException 발생")
    void getSerialList_lot_not_found() {

        Long lotId = 999L;

        when(lotRepository.findById(lotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serialServiceImpl.getSerialListByLotId(lotId))
                .isInstanceOf(LotNotFoundException.class);
    }

    // -------------------------------------------------------------
    // ItemSerial 미존재
    // -------------------------------------------------------------
    @Test
    @DisplayName("시리얼 파일 정보가 없으면 INVALID_REQUEST 발생")
    void getSerialList_itemSerial_not_found() {

        Long lotId = 1L;

        Lots lot = Lots.builder()
                .id(lotId)
                .lotNo("LOT-1")
                .itemId(10L)
                .productionPlanId(100L)
                .build();

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(itemSerialRepository.findByLotId(lotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serialServiceImpl.getSerialListByLotId(lotId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(CommonErrorCode.INVALID_REQUEST.getMessage());
    }
}
