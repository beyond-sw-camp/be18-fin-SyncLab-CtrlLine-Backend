package com.beyond.synclab.ctrlline.domain.itemline.repository;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemLineRepository extends JpaRepository<ItemsLines, Long> {

    @Query("""
            SELECT il.item
            FROM ItemsLines il
            JOIN il.item i
            WHERE il.line = :line
              AND il.isActive = true
              AND i.isActive = true
              AND i.itemStatus = 'FINISHED_PRODUCT'
    """)
    List<Items> findActiveFinishedItemsByLine(@Param("line") Lines line);

    List<ItemsLines> findByLine(Lines line);

    Optional<ItemsLines> findByLineIdAndItemIdAndIsActiveTrue(Long lineId, Long itemId);
}
