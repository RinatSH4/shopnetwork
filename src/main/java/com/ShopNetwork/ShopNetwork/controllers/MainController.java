package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.*;
import com.ShopNetwork.ShopNetwork.repo.ItemRepository;
import com.ShopNetwork.ShopNetwork.repo.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class MainController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    //главная страница, тут просто передаем на нее все товары что есть
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        Iterable<Item> items = itemRepository.findByVerifed(true);

        List<Item> itemList = new ArrayList<>();
        items.forEach(itemList::add);

        // Переворачиваем список
        Collections.reverse(itemList);

        model.addAttribute("items", itemList);
        return "index";
    }
}
