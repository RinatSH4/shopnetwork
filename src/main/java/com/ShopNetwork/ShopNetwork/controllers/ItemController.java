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


    //добавление нового товара
    //тут аналогично как и с добавлением нового пользователя, нового коментария и т.д
    @PostMapping("/item/add")
    public String newItem(@RequestParam String title,
                          @RequestParam String info,
                          @RequestParam String image,
                          @RequestParam float price,
                          @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        //находим самих себя через userDetails
        //потому что мы же выкладываем товар, а не Василий-Гвоздодер
        User user = userRepository.findByUsername(userDetails.getUsername());
//
//        String uploadDir = "/images/"; //путь для сохранения файла
//
//        // Генерация уникального имени файла
//        String uniqueFileName = UUID.randomUUID() + "_" + itemImage.getOriginalFilename();
//
//        Path filePath = Paths.get(uploadDir, uniqueFileName);
//        try {
//            // Сохранение файла на сервере
//            Files.copy(itemImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            //String image = uploadDir + uniqueFileName; // относительный путь для доступа к изображениям

        //создаем новый товар на основе модели Item, передаем в него все, что ввели в форму
        //название, описание, фото, цену ну и конечно же нужно сказать кто продавец
        //именно МЫ! а не какой то там Григорий Погремух
            Item item = new Item(title, info, image, price, user);
            itemRepository.save(item); //как создали, обязательно сохранить в репозитории, только тогда произойдет сохранение в таблицу
            return "redirect:/item/" + item.getId();
            //ну как все успешно прошло - нас перебросит на страницу товара по id
        //как раз что описано ниже
    }

    //страница товара, у каждого товара, пользователя, отзыва есть уникальный идентефикатор ID
    //основа основ, база. Все на нем то и завязано, и товар мы будем искать по id
    //передаем все необходимое что у него есть

    @GetMapping("/item/{id}")
    public String show(@PathVariable(value = "id") long id, Model model, @AuthenticationPrincipal UserDetails userDetails) throws ChangeSetPersister.NotFoundException {
        //а именно находим его по id(он же в ссылке наверху {id} - и преобразуем его в long
        //@PathVariable(value = "id") long id
        Item item = itemRepository.findById(id).orElseGet(Item::new);
        //находим конечно же продавца: Товар->Получить пользователя->получить его id
        //и дальше по репозиторию находим модель того самого продавца
        User user = userRepository.findById(item.getUser().getId()).orElseGet(User::new);

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
        //выше поиск аналогичный как в верхних функциях
        //тут в условии написано, что удалить товар могу только если сам его выложил
        //ну и админ с модератором может его удалять
        //если что то не понравится
        if (item.getUser().getId() == user.getId() ||
                user.getRoles().contains(Role.ADMIN) ||
                user.getRoles().contains(Role.MODERATOR)) {

                //раз уж удаляем товар, удаляем его во всех таблицах
                Iterable<Orders> orders = ordersRepository.findByItemId(id);
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
        //редактировать мы можем товар если мы админ, модератор либо мы сами продавец
        if (item.getUser().getId() == user.getId() ||
                user.getRoles().contains(Role.ADMIN) ||
                user.getRoles().contains(Role.MODERATOR))
            return "item-update";
        else
            return "redirect:/";
    }

    //редактирование товара
    @PostMapping("/item/{id}/update")
    public String updateItem(@PathVariable(value = "id") long id,
                             @RequestParam String title,
                             @RequestParam String info,
                             @RequestParam String image,
                             @RequestParam float price, @AuthenticationPrincipal UserDetails userDetails) {
        Item item = itemRepository.findById(id).orElse(new Item());
        User admin = userRepository.findByUsername(userDetails.getUsername());
        //редактировать мы можем товар если мы админ, модератор либо мы сами продавец
        if (item.getUser().getUsername().equals(userDetails.getUsername()) || admin.getRoles().contains(Role.ADMIN)) {
            item.editItem(title, info, image, price);
            item.setVerifed(false);
            itemRepository.save(item);
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
