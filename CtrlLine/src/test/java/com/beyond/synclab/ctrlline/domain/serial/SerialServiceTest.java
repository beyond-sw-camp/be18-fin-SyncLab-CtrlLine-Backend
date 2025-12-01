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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SerialServiceTest {

    @Mock
    private ItemSerialRepository itemSerialRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private SerialServiceImpl serialServiceImpl;

    // -------------------------------------------------------------
    // 성공 테스트
    // -------------------------------------------------------------
    @Test
    @DisplayName("시리얼 리스트 조회 성공")
    void getSerialList_success() throws Exception {

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

        // gzip 압축된 "[\"S001\",\"S002\"]"
        byte[] gzipBytes = TestGzipUtil.gzip("S001\nS002\n");

        ResponseInputStream<GetObjectResponse> mockStream =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        AbortableInputStream.create(new ByteArrayInputStream(gzipBytes))
                );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockStream);

        // when
        GetLotSerialListResponseDto dto = serialServiceImpl.getSerialListByLotId(lotId);

        // then
        assertThat(dto.getLotNo()).isEqualTo("2025/11/26-7");
        assertThat(dto.getSerialList()).containsExactly("S001", "S002");
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
