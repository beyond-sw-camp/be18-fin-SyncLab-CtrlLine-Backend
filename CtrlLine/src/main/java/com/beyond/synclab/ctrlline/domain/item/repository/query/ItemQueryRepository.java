package com.beyond.synclab.ctrlline.domain.item.repository.query;

import com.beyond.synclab.ctrlline.domain.item.dto.request.SearchItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemQueryRepository {
    Page<Items> searchItems(final SearchItemRequestDto keyword, final Pageable pageable);
}
