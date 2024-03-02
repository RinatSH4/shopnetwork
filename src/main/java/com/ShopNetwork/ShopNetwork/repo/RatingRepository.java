package com.ShopNetwork.ShopNetwork.repo;

import com.ShopNetwork.ShopNetwork.models.Item;
import com.ShopNetwork.ShopNetwork.models.Rating;
import com.ShopNetwork.ShopNetwork.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends CrudRepository<Rating, Long> {
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Rating r WHERE r.item.id = :itemId")
    Float getAverageRating(@Param("itemId") Long itemId);

    Iterable<Rating> findByItemId(long itemId);
    Rating findByUserId(long userId);
    @Query("SELECT r FROM Rating r WHERE r.user = :user AND r.item = :item") //тут проверка на наличие рейтинга")
    Object findMyRating(User user, Item item);
}
