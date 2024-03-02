
package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.*;
import com.ShopNetwork.ShopNetwork.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

//управление статусом заказа

import com.ShopNetwork.ShopNetwork.models.OrderStatus;
import com.ShopNetwork.ShopNetwork.models.Orders;


//управление статусом заказа

@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class OrderController {

    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/order/pending/{id}") //заказ в процессе
    public String panding(@PathVariable(value = "id") long id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        //ищем заказ по {id}
        User user = userRepository.findByUsername(userDetails.getUsername());
        Orders order = ordersRepository.findById(id).orElse(new Orders());
        if(order.getSeller().equals(user)) {
            Set<OrderStatus> status = new HashSet<>();
            status.add(OrderStatus.PENDING); //добавлеем новый статус
            order.setOrderStatusSet(status);
            ordersRepository.save(order); //и сохраняем его
            return "redirect:/user";
        }
        return "redirect:/";
    }

    @GetMapping("/order/shipped/{id}") //заказ отправлен
    public String shipped(@PathVariable(value = "id") long id,
                          @AuthenticationPrincipal UserDetails userDetails) {
        //ищем заказ по {id}
        User user = userRepository.findByUsername(userDetails.getUsername());
        Orders order = ordersRepository.findById(id).orElse(new Orders());
        if(order.getSeller().equals(user)) {
            Set<OrderStatus> status = new HashSet<>();
            status.add(OrderStatus.SHIPPED); //добавлеем новый статус
            order.setOrderStatusSet(status);
            ordersRepository.save(order); //и сохраняем его
            return "redirect:/user";
        }
        return "redirect:/";
    }

    @GetMapping("/order/delivered/{id}") //заказ доставлен
    public String delivered(@PathVariable(value = "id") long id,
                            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        User admin = userRepository.findByUsername("Admin");
        Orders order = ordersRepository.findById(id).orElse(new Orders());
        if(order.getSeller().equals(user)) {
            Set<OrderStatus> status = new HashSet<>();
            status.add(OrderStatus.DELIVERED); //ставим доставлено
            order.setOrderStatusSet(status);
            //при доставкее обнуляем сумму и переводим ее пользователю
            double userBalance = (order.getSumm()/100) * 98;
            double adminBalance = (order.getSumm()/100) * 2; //берем 2 % с продажи

            user.setBalance(user.getBalance() + userBalance); //продавцу 98%
            admin.setBalance(admin.getBalance() + adminBalance); //админу 2%
            order.setSumm(0); // сумму обнуляем
            ordersRepository.save(order); //зануляем сумму
            userRepository.save(user); //обновляем пользователя
            userRepository.save(admin);
            //отправляем деньги продавцу
            return "redirect:/user";
        }
        return "redirect:/";
    }
}
