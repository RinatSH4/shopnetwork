package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.*;

import com.ShopNetwork.ShopNetwork.repo.ItemRepository;
import com.ShopNetwork.ShopNetwork.repo.OrdersRepository;
import com.ShopNetwork.ShopNetwork.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


//тут создаем сессию корзины, ее мы не храним в базе, она хранится в авторизованной сессии

@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class CartController {

    @ModelAttribute("Cart")
    public Cart createShoppingCart() {
        return new Cart();
    }

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;


    //добавление товаров в корзину через сессию
    @RequestMapping("/item/{id}/buy")
    public String addToCart(@PathVariable(value = "id") long id, @ModelAttribute("Cart") Cart cart) {
        Item item = itemRepository.findById(id).orElseGet(Item::new); //находим товар по его id, что указан в ссылке
        cart.setItems(item); //и передаем этот товар в сессию
        return "redirect:/ShoppingCart"; //автопереход на корзину
    }


    //Корзина
    @GetMapping("/ShoppingCart")
    public String cart(@AuthenticationPrincipal UserDetails userDetails, Model model, @ModelAttribute("Cart") Cart cart) throws NoSuchAlgorithmException {
        Iterable<Item> items = cart.getItems();
        User user = userRepository.findByUsername(userDetails.getUsername());
        float total = 0.0f;
        for (Item item : items) {
            total += item.getPrice();
        }
        model.addAttribute("ShoppingCart", items);
        model.addAttribute("summ", total);
        model.addAttribute("user", user);
        return "ShoppingCart";
    }

    @GetMapping("/ShoppingCart/deleteall") //если посмотреть по ссылкам в html файлах
    public String deleteCart(@ModelAttribute("Cart") Cart cart) {
        cart.clearAll(); //очищаем корзину
        return "redirect:/ShoppingCart"; //перемещаемся в корзину
    }

    @GetMapping("/ShoppingCart/delete/{id}") //удаляем товар из корзины
    public String dellItemCart(@ModelAttribute("Cart") Cart cart,
                               @PathVariable(value = "id") long id) {
        Item item = itemRepository.findById(id).orElseGet(Item::new);
        int cartId = cart.getItemIndex(item);
        cart.deleteItem(cartId); //тут мы находим товар item по id что в ссылке и удаляем его через репозиторий
        //все репозитории находятся в папках repo
        //репозиторий нужен нам для управления базой данных в spring boot, обычно репозитории создаются в виде интерфейсов
        //он может находить запись по любому названия поля, по значению и тд
        return "redirect:/ShoppingCart";
    }

    //оформляем заказ
    @PostMapping("/ShoppingCart")
    public String buy(@RequestParam String address, @ModelAttribute("Cart") Cart cart, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()); //находим модель самих себя
        for (Item i : cart.getItems()) { //перебираем все товары в нашей корзине
            //добавляем каждый товар в таблицу Orders(оформленные заказы), передаем такие параметры как:
            //товар, мы сами, продавец, адрес и статус доставки PENDING (это перечисление ENUM, такое же как роли пользователей)
            Orders order = new Orders(i, user, i.getUser(), address, Collections.singleton(OrderStatus.NOPAID));
            ordersRepository.save(order); //сохраняем каждый заказ в таблице Orders
        }
        cart.clearAll(); //после того как все оформили очищаем корзину
        return "redirect:/index"; //и перемещаемся автоматом на главную страницу
    }

    //оплата из под страницы user (пока только тут меняется таблица заказов
    //устанавливается сумма)
    @GetMapping("/item/buy/{id}") //тут такая же оплата, если на странице пользователя есть не оплаченый товар
    public String buyUserPage(@PathVariable(value = "id") long id, @AuthenticationPrincipal UserDetails userDetails) {
        Orders order = ordersRepository.findById(id).orElseGet(Orders::new);
        User user = userRepository.findByUsername(userDetails.getUsername());
        if(order.getUser().equals(user)) {
            order.setPay(true);
            Set<OrderStatus> status = new HashSet<>();
            status.add(OrderStatus.PAID); //добавлеем новый статус
            order.setOrderStatusSet(status);

            double itemPrice = order.getItem().getPrice(); //получаем цену для оплаты
            if (user.getBalance() >= itemPrice) {
                user.setBalance(user.getBalance() - itemPrice); //списываем деньги у пользователя за товар
                order.setSumm(itemPrice); //добавляем сумму к оплаченному товару
            }

            ordersRepository.save(order); //и сохраняем его
        }
        return "redirect:/user";
    }

    //перевод в md5 для нереализованной оплаты
    public static String md5(String key) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(StandardCharsets.UTF_8.encode(key));
        return String.format("%032x", new BigInteger(1, md5.digest()));
    }
}
