package com.ShopNetwork.ShopNetwork.RestApiFiles.controllers;

import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.JwtAuthentication;
import com.ShopNetwork.ShopNetwork.RestApiFiles.services.AuthService;
import com.ShopNetwork.ShopNetwork.RestApiFiles.services.ChatService;
import com.ShopNetwork.ShopNetwork.models.*;
import com.ShopNetwork.ShopNetwork.repo.*;
import com.ShopNetwork.ShopNetwork.service.FriendServise;
import com.ShopNetwork.ShopNetwork.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.*;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestController {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;

    private final AuthService authService;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private FriendServise friendServise;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ItemChatRepo itemChatRepo;

    private UserService userService;

    //получение всех товаров
    @GetMapping("/items")
    public ResponseEntity<List<Map<String, String>>> getAllItems() {
        List<Map<String, String>> allItems = new ArrayList<>();
        Iterable<Item> items = itemRepository.findByVerifed(true);
        for (Item item : items) {
            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("title", item.getTitle());
            itemMap.put("info", item.getInfo());
            itemMap.put("image", item.getImage());
            itemMap.put("price", String.valueOf(item.getPrice()));
            itemMap.put("id", String.valueOf(item.getId()));
            itemMap.put("user_id", String.valueOf(item.getUser().getId()));
            itemMap.put("rating", String.valueOf(ratingRepository.getAverageRating(item.getId())));
            allItems.add(itemMap);
        }
        return ResponseEntity.ok(allItems);
    }


    //получение всех пользователей
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        List<Map<String, String>> allUsers = new ArrayList<>();
        Iterable<User> users = userRepository.findAll();
        for (User user : users) {
            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("name", user.getName());
            itemMap.put("surname",user.getSurname());
            itemMap.put("username", user.getUsername());
            itemMap.put("email", user.getEmail());
            itemMap.put("phone", user.getPhone_number());
            itemMap.put("id", String.valueOf(user.getId()));
            itemMap.put("registration_date:", String.valueOf(user.getDate_of_registration()));
            itemMap.put("birth_date:", String.valueOf(user.getDate_of_birth()));
            allUsers.add(itemMap);
        }
        return ResponseEntity.ok(allUsers);
    }


    //добавление пользователя

//    {
//        "name": "имя",
//            "surname": "фамилия",
//            "username": "nickname",
//            "phone_number": "7917999999",
//            "emai": "email@mail.ru",
//            "password": "password",
//            "date_of_birth": "1993-02-11"
//    }
    @PostMapping("/user/add")
    public ResponseEntity<Map<String, String>> addUser(@RequestBody User newuser) {

        String password = passwordEncoder.encode(newuser.getPassword());
        Map<String, String> responce = new HashMap<>();

        System.out.println(newuser.getEmail() + ":" + newuser.getUsername());
        if (userRepository.findByUsername(newuser.getUsername()) == null ||
        userRepository.findByEmail(newuser.getEmail()) == null) {
            User user = new User(
                    newuser.getName(),
                    newuser.getSurname(),
                    newuser.getUsername(),
                    newuser.getPhone_number(),
                    newuser.getEmail(),
                    password,
                    newuser.getDate_of_birth(),
                    true,
                    Collections.singleton(Role.USER));
            userRepository.save(user);

            responce.put("id:", String.valueOf(user.getId()));
            responce.put("login:", user.getUsername());
            responce.put("email:", user.getEmail());
            responce.put("message:", "Пользователь добавлен.");
        } else {
            responce.put("message:", "Пользователь с таким именем или email уже существует.");
        }
        return ResponseEntity.ok(responce);
    }

    //добавление нового товара
    @PostMapping("/item/add")
    public ResponseEntity<String> addItem(@RequestBody Item newitem) {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        User user = userRepository.findByUsername(authInfo.getUsername());
        Item item = new Item(newitem.getTitle(), newitem.getInfo(), newitem.getImage(), newitem.getPrice(), user);
        itemRepository.save(item);
        return ResponseEntity.ok("\"message\": \"Добавлено " + newitem.getTitle() + "\"");
    }


    //удаление товара юзером
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/item/{id}/delete")
    public ResponseEntity <Map<String, String>> deleteItem(@PathVariable (value = "id") long id) {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        User user = userRepository.findByUsername(authInfo.getUsername());
        Item item = itemRepository.findById(id).orElse(new Item());
        Map<String, String> responce = new HashMap<>();
        if(authInfo.getRoles().equals(Role.USER) || authInfo.getRoles().equals(Role.ADMIN)) {
            if (item.getUser().getId() == user.getId() || authInfo.getRoles().equals(Role.ADMIN)) {
                //раз уж удаляем товар, удаляем его во всех таблицах
                List<Orders> orders = ordersRepository.findByItemId(id);
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
                responce.put("item_ID:", String.valueOf(id));
                responce.put("message:", "удалено");
            } else {
                responce.put("message:", "Ошибка удаления");
            }
        } else {
            responce.put("message:", "Ошибка доступа");
        }
        return ResponseEntity.ok(responce);
    }


    //получение товара по id
    @GetMapping("/item/{id}")
    public ResponseEntity <Map<String, String>> getItemById(@PathVariable(value = "id") long id) throws ChangeSetPersister.NotFoundException {
        Item item = itemRepository.findById(id).orElse(new Item());
        User user = userRepository.findById(item.getUser().getId()).orElse(new User());
        Iterable<Review> reviews = reviewRepository.findByItemId(id);

        Map<String, String> responce = new HashMap<>();
        responce.put("id:", String.valueOf(item.getId()));
        responce.put("title:", item.getTitle());
        responce.put("image:", item.getImage());
        responce.put("info:", item.getInfo());
        responce.put("price:", String.valueOf(item.getPrice()));
        responce.put("seller:", user.getName());
        responce.put("rating:", String.valueOf(ratingRepository.getAverageRating(id)));
        return ResponseEntity.ok(responce);
    }

    //радактирование товара по id
    @PutMapping("/item/{id}/update")
    public ResponseEntity <Map<String, String>> editItem(@PathVariable(value = "id") long id,
                                                         @RequestBody Item edititem)
            throws ChangeSetPersister.NotFoundException {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        Item item = itemRepository.findById(id).orElse(new Item());
        User user = userRepository.findByUsername(authInfo.getUsername());
        Map<String, String> responce = new HashMap<>();

        if(authInfo.getRoles().contains(Role.USER) || authInfo.getRoles().contains(Role.ADMIN) || authInfo.getRoles().contains(Role.MODERATOR)) {
            if (item.getUser().getId() == user.getId()) {
                item.editItem(edititem.getTitle(), edititem.getInfo(), edititem.getImage(),
                        edititem.getPrice());
                itemRepository.save(item);
                responce.put("id:", String.valueOf(item.getId()));
                responce.put("new_title:", item.getTitle());
                responce.put("new_image:", item.getImage());
                responce.put("new_info:", item.getInfo());
                responce.put("new_price:", String.valueOf(item.getPrice()));
                responce.put("user_id:", String.valueOf(item.getUser().getId()));
            } else {
                responce.put("message:", "Ошибка редактирования товара");
            }
        }
        else {
            responce.put("message:", "Ошибка прав доступа");
        }
        return ResponseEntity.ok(responce);
    }


//Добавить отзыв
    @PostMapping("/item/{id}/review/add")
    public ResponseEntity<String> reviewAdd(@PathVariable(value = "id") long itemId, @RequestBody String rew) {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        User user = userRepository.findByUsername(authInfo.getUsername());
        Item item = itemRepository.findById(itemId).orElseGet(Item::new);
        if(authInfo.getRoles().contains(Role.USER) || authInfo.getRoles().contains(Role.ADMIN) || authInfo.getRoles().contains(Role.MODERATOR)) {
            //Что бы оставить коментарий, проверим сначала получил ли пользователь заказ (OrderStatus.DELIVERED)
            //затем проверим, есть ли уже коментарий под этим товаром, если нет - то оставим.
            if (ordersRepository.findOrderBuy(user, item, OrderStatus.DELIVERED) != null && reviewRepository.findReview(user, item) == null) {
                Iterable<Review> reviews = reviewRepository.findByItemId(itemId);
                Review review = new Review(rew, authInfo.getUsername(), user, item);
                reviewRepository.save(review);
                return ResponseEntity.ok("\"message\": \"добавлен отзыв\"");
            } else {
                return ResponseEntity.ok("\"message\": \"Вы уже оставили коментарий\"");
            }
        } else {
            return ResponseEntity.ok("\"message\": \"Ошибка прав доступа\"");
        }
    }


//оценить товар
    @PostMapping("/item/{id}/rating")
    public ResponseEntity<String> ratingAdd(@PathVariable(value = "id")long itemId, @RequestParam int ball) {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        User user = userRepository.findByUsername(authInfo.getUsername());
        Item item = itemRepository.findById(itemId).orElseGet(Item::new);
        if (authInfo.getRoles().contains(Role.USER) || authInfo.getRoles().contains(Role.ADMIN) || authInfo.getRoles().contains(Role.MODERATOR)) {
            //Что бы оставить коментарий, проверим сначала получил ли пользователь заказ (OrderStatus.DELIVERED)
            //затем проверим, есть ли уже коментарий под этим товаром, если нет - то оставим.
            if (ordersRepository.findOrderBuy(user, item, OrderStatus.DELIVERED) != null && ratingRepository.findMyRating(user, item) == null) {
                Rating rating = new Rating(user, item, ball);
                ratingRepository.save(rating);
                return ResponseEntity.ok("\"message\": \"добавлена оценка\"");
            } else {
                return ResponseEntity.ok("\"message\": \"Вы уже оценили этот товар\"");
            }
        } else {
            return ResponseEntity.ok("\"message\": \"Ошибка прав доступа\"");
        }
    }

    //отзывы к товару
    @GetMapping("/item/{id}/reviews")
    public ResponseEntity<List<Map<String, String>>> getReviews(@PathVariable(value = "id")long itemId) {
        List<Map<String, String>> allReviews = new ArrayList<>();
        Iterable<Review> reviews = reviewRepository.findByItemId(itemId);
        for (Review review : reviews) {
            Map<String, String> reviewMap = new HashMap<>();
            reviewMap.put("username", review.getUsername());
            reviewMap.put("text", review.getText());
            allReviews.add(reviewMap);
        }
        return ResponseEntity.ok(allReviews);
    }

    //Заказ, на фронте сделать корзину, где обозначен id товара, для оформления заказа всю корзину перебрать циклом
    @PostMapping("/ShoppingCart/buy/{id}")
    public ResponseEntity<String> buy(@RequestBody String address, @PathVariable(value = "id") long itemId) {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        User user = userRepository.findByUsername(authInfo.getUsername());
        Item item = itemRepository.findById(itemId).orElseGet(Item::new);

            Orders order = new Orders(item, user, item.getUser(), address, Collections.singleton(OrderStatus.PENDING));
            ordersRepository.save(order);
        return ResponseEntity.ok("\"message\": \"Товары перешли в заказ\"");
    }

    //----------------------тут запрос на статус заказа------------------
    @PostMapping("/order/{id}/status")
    public ResponseEntity<String> shipped(@PathVariable(value = "id") long id, @RequestBody OrderStatus orderStatus) {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        Orders order = ordersRepository.findById(id).orElseGet(Orders::new); //находим заказ
        Item seller = itemRepository.findById(id).orElseGet(Item::new); //находим товар в таблице Item, что бы узнать кто продавец
        if(seller.getUser().getUsername().equals(authInfo.getUsername())) {//Проверяем, мой ли товар, что бы я мог управлять статусом заказа
            Set<OrderStatus> status = new HashSet<>();
            status.add(orderStatus);
            order.setOrderStatusSet(status);
            ordersRepository.save(order);
            return ResponseEntity.ok("\"message\": \"Обновление статуса\"\n\"status\":" + orderStatus + "\"");
        } else {
            return ResponseEntity.ok("\"message\": \"Ошибка прав доступа\"");
        }
    }

    //переход на страницу пользователя
    @GetMapping ("/user/{userId}")
    public ResponseEntity<Map<String, String>> showUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "userId") long userID) {
        User user = userRepository.findById(userID).orElseGet(User::new);
        Map<String, String> responce = new HashMap<>();
        responce.put("username", user.getUsername());
        responce.put("name", user.getName());
        responce.put("surname", user.getSurname());
        responce.put("phone", user.getPhone_number());
        responce.put("email", user.getEmail());
        return ResponseEntity.ok(responce);
    }

    //получение списка друзей пользователя
    @GetMapping ("/user/{userId}/friends")
    public ResponseEntity<List<Map<String, String>>> showUserFriends(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "userId") long userID) {
        User user = userRepository.findById(userID).orElseGet(User::new);
        List<Map<String, String>> responce = new ArrayList<>();
        Iterable<User> friends = user.getFriends();
        for (User friend : friends) {
            Map<String, String> userFriends = new HashMap<>();
            userFriends.put("name", friend.getName());
            userFriends.put("surname", friend.getSurname());
            userFriends.put("username", friend.getUsername());
            responce.add(userFriends);
        }
        return ResponseEntity.ok(responce);
    }

    //получение списка подписчиков пользователя
    @GetMapping ("/user/{userId}/followers")
    public ResponseEntity<List<Map<String, String>>> showUserFollowers(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "userId") long userID) {
        User user = userRepository.findById(userID).orElseGet(User::new);
        List<Map<String, String>> responce = new ArrayList<>();
        Iterable<User> followers = user.getFollowers();
        for (User follower : followers) {
            Map<String, String> userFollower= new HashMap<>();
            userFollower.put("name", follower.getName());
            userFollower.put("surname", follower.getSurname());
            userFollower.put("username", follower.getUsername());
            responce.add(userFollower);
        }
        return ResponseEntity.ok(responce);
    }

    //отправка запроса в друзья
    @GetMapping("/addfriend/{friendId}")
    public ResponseEntity<String> sendFriendRequest(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "friendId") Long friendId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        User friend = userRepository.findById(friendId).orElseGet(User::new);
        friendServise.sendFriendRequest(user, friend);
        return ResponseEntity.ok("\"message\": \"Пользователю " + user.getUsername() + " отправлен запрос в друзья.\"");
    }

    //принять друга
    @GetMapping("user/{followerId}/addfriend")
    public ResponseEntity<String> addFriendRequest(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "followerId") Long followerId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        User friend = userRepository.findById(followerId).orElseGet(User::new);
        friendServise.acceptFriendRequest(friend, user);
        return ResponseEntity.ok("\"message\": \"Пользователь " + friend.getUsername() + "\"");
    }
    //-------------------чат------------------
    //открыть чат с пользователем
    @GetMapping("/user/{id}/chat")
    public ResponseEntity<List<Map<String, String>>> chat(@PathVariable(value = "id") long id, @AuthenticationPrincipal UserDetails userDetails) {
        User im = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        List<Map<String, String>> responce = new ArrayList<>();
        Iterable<ChatMessage> messages = messageRepository.findBySenderAndRecipientOrSenderAndRecipient(im, user, user, im);
        for (ChatMessage message : messages) {
            Map<String, String> newMessage= new HashMap<>();
            newMessage.put("username", message.getSender().getUsername());
            newMessage.put("content", message.getContent());
            newMessage.put("time", message.getTime().toString());
            responce.add(newMessage);
        }
        return ResponseEntity.ok(responce);
    }

    //отправка сообщения пользователю
    @PostMapping("/user/{id}/chat")
    public ResponseEntity<String> sendMessage(@RequestParam String message, @PathVariable(value = "id") long id,
                              @AuthenticationPrincipal UserDetails userDetails) {
        User im = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        chatService.sendMessage(im, user, message);
        return ResponseEntity.ok("\"message\": \"Сообщение доставлено.\"");
    }

    //отправка сообщения в общий чат по товару
    @PostMapping("/item/{id}/chat")
    public ResponseEntity<String> sendMessageToItem(@RequestParam String message, @PathVariable(value = "id") long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Item item = itemRepository.findById(id).orElseGet(Item::new);
        //создаем новое сообщение
        ItemChat itemChatAdd = new ItemChat();
        itemChatAdd.setUser(user);
        itemChatAdd.setContent(message);
        itemChatAdd.setUsername(user.getUsername());
        itemChatAdd.setItem(item);
        itemChatAdd.setTime(LocalDateTime.now());
        itemChatRepo.save(itemChatAdd);
        return ResponseEntity.ok("\"message\": \"Сообщение доставлено.\"");
    }

    //чат с товаром
    @GetMapping("/item/{id}/chat")
    public ResponseEntity<List<Map<String, String>>> chatItem(@PathVariable(value = "id") long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User im = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        List<Map<String, String>> responce = new ArrayList<>();
        Iterable<ItemChat> messages = itemChatRepo.findByItemId(id);
        for (ItemChat message : messages) {
            Map<String, String> newMessage= new HashMap<>();
            newMessage.put("username", message.getUser().getUsername());
            newMessage.put("content", message.getContent());
            newMessage.put("time", message.getTime().toString());
            responce.add(newMessage);
        }
        return ResponseEntity.ok(responce);
    }
}
