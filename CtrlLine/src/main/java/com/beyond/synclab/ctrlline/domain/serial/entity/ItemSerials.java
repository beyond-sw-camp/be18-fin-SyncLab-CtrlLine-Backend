package com.beyond.synclab.ctrlline.domain.serial.entity;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "item_serial")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class ItemSerials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_serial_id")
    private Long id;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", insertable = false, updatable = false)
    private Lots lot;

    @Column(name = "serial_file_path", length = 512)
    private String serialFilePath;

    public static ItemSerials create(Long lotId, String path) {
        return ItemSerials.builder()
                .lotId(lotId)
                .serialFilePath(path)
                .build();
    }

    public void updateSerialFilePath(String serialFilePath) {
        this.serialFilePath = serialFilePath;
    }
}
