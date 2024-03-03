package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.*;

import com.ShopNetwork.ShopNetwork.repo.*;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ResourceLoader;

@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class ItemController {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private ItemChatRepo itemChatRepo;


    //страница добавления нового товара
    @GetMapping("/item/add")
    public String add() {
        return "add-item";
    }


    @PostMapping("/item/add")
    public String newItem(@RequestParam String title,
                          @RequestParam String info,
                          @RequestParam String image,
                          @RequestParam float price,
                          @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Item item = new Item(title, info, image, price, user);
        itemRepository.save(item);
        return "redirect:/item/" + item.getId();
    }


    @GetMapping("/item/{id}")
    public String show(@PathVariable(value = "id") long id, Model model, @AuthenticationPrincipal UserDetails userDetails) throws ChangeSetPersister.NotFoundException {

        Item item = itemRepository.findById(id).orElseGet(Item::new);
        User user = userRepository.findByUsername(item.getUser().getUsername());

        System.out.println(item.getUser().getUsername());
        //тут передаем товар
        model.addAttribute("item", item);
        //тут продавца
        model.addAttribute("user", user);

        //ну а тут отзывы
        Iterable<Review> reviews = reviewRepository.findByItemId(id);
        model.addAttribute("reviews", reviews);

        //тут оценки
        try {
            model.addAttribute("rating", ratingRepository.getAverageRating(id));
        } catch (NoResultException e) {
            model.addAttribute("rating", e.toString());
        }

        if (userDetails != null) {
            User authUser = userRepository.findByUsername(userDetails.getUsername());

            //Проверка, куплен и доставлен ли товар, либо уже оценен, что бы отобразить или скрыть форму для рейтинга
            if (ordersRepository.findOrderBuy(authUser, item, OrderStatus.DELIVERED) != null && ratingRepository.findMyRating(authUser, item) == null)
                model.addAttribute("isHaveRating", true);
             else
                model.addAttribute("isHaveRating", false);


            //Проверка, куплен и доставлен ли товар, либо уже оставлен отзыв, что бы отобразить или скрыть форму для отзывов
            if (ordersRepository.findOrderBuy(authUser, item, OrderStatus.DELIVERED) != null && reviewRepository.findReview(authUser, item) == null)
                model.addAttribute("isHaveReview", true);
             else
                model.addAttribute("isHaveReview", false);
        }
        return "show-item";
    }

    //удаление товара
    @GetMapping("/item/{id}/delete")
    public String deleteItem(@PathVariable (value = "id") long id, @AuthenticationPrincipal UserDetails userDetails) {
        Item item = itemRepository.findById(id).orElse(new Item());
        User user = userRepository.findByUsername(userDetails.getUsername());
        Iterable<Orders> orders = ordersRepository.findByItemId(id);

        List<Orders> orderIsWork = new ArrayList<>();
        for (Orders o : orders) {
            if (o.isWork())
                orderIsWork.add(o);
        }

        //в конце проверим, в работе ли товар, а то может доставляется или еще что
        if ((item.getUser().getId() == user.getId() ||
                user.getRoles().contains(Role.ADMIN) ||
                user.getRoles().contains(Role.MODERATOR)) && orderIsWork.isEmpty()) {

                //раз уж удаляем товар, удаляем его во всех таблицах

                for (Orders order : orders)
                    ordersRepository.delete(order);

                Iterable<Review> reviews = reviewRepository.findByItemId(id);
                for (Review review : reviews)
                    reviewRepository.delete(review);

                Iterable<Rating> ratings = ratingRepository.findByItemId(id);
                for (Rating rating : ratings)
                    ratingRepository.delete(rating);

                Iterable<ItemChat> itemChats = itemChatRepo.findByItemId(id);
                for (ItemChat chat : itemChats)
                    itemChatRepo.delete(chat);
            itemRepository.delete(item);
        }
        return "redirect:/";
    }

    //страница редактирования товара
    @GetMapping("/item/{id}/update")
    public String update(@PathVariable(value = "id") long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Item item = itemRepository.findById(id).orElse(new Item());
        User user = userRepository.findByUsername(userDetails.getUsername());
        model.addAttribute("item", item);
        System.out.println(item.getUser().getUsername());
        //редактировать мы можем товар если мы админ, модератор либо мы сами продавец
        if ((item.getUser() == user) ||
                user.getRoles().contains(Role.ADMIN) ||
                user.getRoles().contains(Role.MODERATOR))
            return "item-update";
        else
            return "redirect:/";
    }

    //делаем товар активным
    @GetMapping("/item/{id}/setenabletrue")
    public String setenabletrue(@PathVariable(value = "id") long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Item item = itemRepository.findById(id).orElse(new Item());
        User user = userRepository.findByUsername(userDetails.getUsername());
        model.addAttribute("item", item);
        System.out.println(item.getUser().getUsername());
        //редактировать мы можем товар если мы сами продавец
        if (item.getUser() == user) {
            item.setEnabled(true);
            itemRepository.save(item);
            return "item-update";
        }
        else
            return "redirect:/";
    }

    //делаем товар НЕ активным
    @GetMapping("/item/{id}/setenablefalse")
    public String setenablefalse(@PathVariable(value = "id") long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Item item = itemRepository.findById(id).orElse(new Item());
        User user = userRepository.findByUsername(userDetails.getUsername());
        model.addAttribute("item", item);
        System.out.println(item.getUser().getUsername());
        //редактировать мы можем товар если мы сами продавец
        if (item.getUser() == user) {
            item.setEnabled(false);
            itemRepository.save(item);
            return "item-update";
        }
        else
            return "redirect:/";
    }



    //редактирование товара
    @PostMapping("/item/{id}/update")
    public String updateItem(@PathVariable(value = "id") long id,
                             @RequestParam String title,
                             @RequestParam String info,
                             @RequestParam String image,
                             @RequestParam float price,
                             @AuthenticationPrincipal UserDetails userDetails) {
        Item item = itemRepository.findById(id).orElse(new Item());
        User admin = userRepository.findByUsername(userDetails.getUsername());
        //редактировать мы можем товар если мы сами продавец
        if (item.getUser().getUsername().equals(userDetails.getUsername())) {
            item.editItem(title, info, image, price);
            item.setVerifed(false);
            itemRepository.save(item);
            System.out.println(item.isEnabled());
            return "redirect:/item/" + id;
        }
        return "redirect:/item/" + id;
    }

    //поиск товара
    @GetMapping("/search-item")
    //как реализовано, нужно смотреть в файле ItemRepository
    public String searchItem(@RequestParam(name = "search") String search, Model model) {
        Iterable<Item> items = itemRepository.findItemByText(search, true);
        model.addAttribute("items", items);
        return "search";
    }
}
