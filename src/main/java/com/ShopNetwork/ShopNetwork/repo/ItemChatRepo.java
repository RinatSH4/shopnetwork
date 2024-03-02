package com.ShopNetwork.ShopNetwork.repo;

import com.ShopNetwork.ShopNetwork.models.ItemChat;
import com.ShopNetwork.ShopNetwork.models.Review;
import org.springframework.data.repository.CrudRepository;

public interface ItemChatRepo extends CrudRepository<ItemChat, Long> {
    Iterable<ItemChat> findByItemId(long itemId);
    ItemChat findById(long messageId);
}
