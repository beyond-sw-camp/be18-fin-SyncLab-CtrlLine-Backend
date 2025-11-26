package com.beyond.synclab.ctrlline.domain.serial.repository;

import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemSerialRepository extends JpaRepository<ItemSerials, Long> {

    Optional<ItemSerials> findByLotId(Long lotId);
}
