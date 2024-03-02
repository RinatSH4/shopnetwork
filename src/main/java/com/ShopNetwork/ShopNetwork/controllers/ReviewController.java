package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.*;
import com.ShopNetwork.ShopNetwork.repo.ItemRepository;
import com.ShopNetwork.ShopNetwork.repo.MessageRepository;
import com.ShopNetwork.ShopNetwork.repo.OrdersRepository;
import com.ShopNetwork.ShopNetwork.repo.ReviewRepository;
import com.ShopNetwork.ShopNetwork.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

//отзывы
@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class ReviewController {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrdersRepository ordersRepository;

    @PostMapping("/item/{id}/review/add")
    public String reviewAdd(@PathVariable(value = "id") long itemId, @RequestParam String rew, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Item item = itemRepository.findById(itemId).orElseGet(Item::new);
        //Что бы оставить коментарий, проверим сначала получил ли пользователь заказ (OrderStatus.DELIVERED)
        //затем проверим, есть ли уже коментарий под этим товаром, если нет - то оставим.
        if(ordersRepository.findOrderBuy(user, item, OrderStatus.DELIVERED) != null && reviewRepository.findReview(user, item) == null) {
            Iterable<Review> reviews = reviewRepository.findByItemId(itemId);
            Review review = new Review(rew, userDetails.getUsername(), user, item);
            reviewRepository.save(review);
        }
        return "redirect:/item/" + itemId;
    }
}

