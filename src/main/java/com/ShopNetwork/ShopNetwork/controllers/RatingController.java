package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.*;
import com.ShopNetwork.ShopNetwork.repo.*;
import com.ShopNetwork.ShopNetwork.service.FriendServise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Collections;

import com.ShopNetwork.ShopNetwork.models.Item;
import com.ShopNetwork.ShopNetwork.models.OrderStatus;
import com.ShopNetwork.ShopNetwork.models.Rating;
import com.ShopNetwork.ShopNetwork.models.User;

@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class RatingController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private OrdersRepository ordersRepository;

//тут оценки товаров
    @PostMapping("/item/{id}/rating")
    public String ratingAdd(@PathVariable(value = "id") long itemId, @RequestParam int ball, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Item item = itemRepository.findById(itemId).orElseGet(Item::new);
        //Что бы оставить коментарий, проверим сначала получил ли пользователь заказ (OrderStatus.DELIVERED)
        //затем проверим, есть ли уже коментарий под этим товаром, если нет - то оставим.
        if (ordersRepository.findOrderBuy(user, item, OrderStatus.DELIVERED) != null && ratingRepository.findMyRating(user, item) == null) {
            Rating rating = new Rating(user, item, ball);
            ratingRepository.save(rating);
        }
        return "redirect:/item/" + itemId;
    }
}
