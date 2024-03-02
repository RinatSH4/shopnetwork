package com.ShopNetwork.ShopNetwork.repo;

import com.ShopNetwork.ShopNetwork.models.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


import com.ShopNetwork.ShopNetwork.models.Item;
import com.ShopNetwork.ShopNetwork.models.OrderStatus;
import com.ShopNetwork.ShopNetwork.models.Orders;
import com.ShopNetwork.ShopNetwork.models.User;

public interface OrdersRepository extends CrudRepository<Orders, Long> {
    List<Orders> findByUser(User user);
    List<Orders> findBySellerId(long sellerId);
    List<Orders> findByItemId(long itemId);
    @Query("SELECT o FROM Orders o WHERE o.user = :user AND o.item = :item AND :status MEMBER OF o.orderStatusSet")
    Orders findOrderBuy(@Param("user") User user, @Param("item") Item item, @Param("status") OrderStatus status);
}
