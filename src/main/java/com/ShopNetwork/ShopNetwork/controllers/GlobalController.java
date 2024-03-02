package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.Cart;
import com.ShopNetwork.ShopNetwork.models.Item;
import com.ShopNetwork.ShopNetwork.models.User;
import com.ShopNetwork.ShopNetwork.repo.ItemRepository;
import com.ShopNetwork.ShopNetwork.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

@ControllerAdvice
@SessionAttributes({"Cart", "verifedItems", "userBalance"})
public class GlobalController {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void globalAttributes(Model model, @ModelAttribute("Cart") Cart cart) {
        Iterable<Item> items = cart.getItems();
        int cartCount = 0;
        for (Item item : items) {
            cartCount += 1;
        }
        model.addAttribute("cartCount", cartCount);
    }

    @ModelAttribute
    public void verifedItems(Model model, @ModelAttribute("verifedItems") Cart cart) {
        Iterable<Item> items = itemRepository.findByVerifed(false);
        int verifedItems = 0;
        for (Item item : items) {
            verifedItems += 1;
        }
        model.addAttribute("vefifedItemsFalse", verifedItems);
    }
}
