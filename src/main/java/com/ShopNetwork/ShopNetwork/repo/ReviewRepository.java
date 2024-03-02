
package com.ShopNetwork.ShopNetwork.repo;

import com.ShopNetwork.ShopNetwork.models.Item;
import com.ShopNetwork.ShopNetwork.models.Rating;
import com.ShopNetwork.ShopNetwork.models.Review;
import com.ShopNetwork.ShopNetwork.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface ReviewRepository extends CrudRepository<Review, Long> {
    Iterable<Review> findByItemId(long itemId);
    @Query("Select r FROM Review r WHERE r.user = :user AND r.item = :item")
    Review findReview(@Param("user") User user, @Param("item") Item item); //тут проверка на наличие коментария
}
