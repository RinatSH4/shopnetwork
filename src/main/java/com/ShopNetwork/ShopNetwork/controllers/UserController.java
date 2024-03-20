
package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.models.*;
import com.ShopNetwork.ShopNetwork.repo.*;
import com.ShopNetwork.ShopNetwork.service.FriendServise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.*;

@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TransactionalRepository transactionalRepository;

    @Autowired
    private FriendServise friendServise;
    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/login")
    public String login(@RequestParam Map<String, String> params, Model model) {
        if (params.containsKey("error")) {
            model.addAttribute("error", "неверный логин или пароль");
        }
        return "login";
    }


    //админка
    @GetMapping("/adminpanel") //панель администратора
    public String admin(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername());
        Iterable<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        if (admin.getRoles().contains(Role.ADMIN)) //проверяем, если мы админ, то переходим в админку
            return "adminpanel";
        else
            return "redirect:/user"; //если не админ, то в личный кабинет
    }

    @GetMapping("/user/{id}/setuser") //функция назначения роли из списка пользователей, назначаем пользователем
    public String setUser(@PathVariable(value = "id") long id, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        if (admin.getRoles().contains(Role.ADMIN)) {
            Set<Role> status = new HashSet<>();
            status.add(Role.USER);
            user.setRoles(status);
            userRepository.save(user);
            return "redirect:/adminpanel";
        } else {
            return "redirect:/user";
        }
    }

    @GetMapping("/user/{id}/setadmin") //функция назначения роли из списка пользователей, назначаем админом
    public String setAdmin(@PathVariable(value = "id") long id, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        if (admin.getRoles().contains(Role.ADMIN)) {
            Set<Role> status = new HashSet<>();
            status.add(Role.ADMIN);
            user.setRoles(status);
            userRepository.save(user);
            return "redirect:/adminpanel";
        } else {
            return "redirect:/user";
        }
    }

    @GetMapping("/user/{id}/setmoder") //функция назначения роли из списка пользователей, назначаем модером
    public String setModer(@PathVariable(value = "id") long id, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        if (admin.getRoles().contains(Role.ADMIN)) {
            Set<Role> status = new HashSet<>();
            status.add(Role.MODERATOR);
            user.setRoles(status);
            userRepository.save(user);
            return "redirect:/adminpanel";
        } else {
            return "redirect:/user";
        }
    }

    @GetMapping("/user/{id}/ban") //функция назначения роли из списка пользователей, тут просто БАН
    public String ban(@PathVariable(value = "id") long id, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        if (admin.getRoles().contains(Role.ADMIN)) {
            Set<Role> status = new HashSet<>();
            status.add(Role.BANNED);
            user.setRoles(status);
            userRepository.save(user);
            return "redirect:/adminpanel";
        } else {
            return "redirect:/user";
        }
    }

    @GetMapping("/verifed-items")
    public String verifedItems(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername());
        if (admin.getRoles().contains(Role.ADMIN) || admin.getRoles().contains(Role.MODERATOR)) {
            Iterable<Item> items = itemRepository.findByVerifed(false);
            model.addAttribute("items", items);
            return "/verifed-items";
        } else
            return "redirect:/";
    }

    @GetMapping("/item/{id}/verifed")
    public String verifedItem(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable(value = "id") long id) {
        User admin = userRepository.findByUsername(userDetails.getUsername());
        if (admin.getRoles().contains(Role.ADMIN) || admin.getRoles().contains(Role.MODERATOR)) {
            Item item = itemRepository.findById(id).orElseGet(Item::new);
            if (!item.isVerifed()) {
                item.setVerifed(true);
                itemRepository.save(item);
            }
            return "redirect:/verifed-items";
        } else
            return "redirect:/";
    }

    //это наш личный кабинет, на страницу передаются данные из базы данных которые описаны ниже
    //все данные находим через репозиторий
    @GetMapping("/user")
    public String user(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        //инфа о пользователе
        User user = userRepository.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        //мои покупки
        Iterable<Orders> myOrders = ordersRepository.findByUser(user);
        model.addAttribute("myOrders", myOrders);
        //мои продажи
        Iterable<Orders> mySells = ordersRepository.findBySellerId(user.getId());
        model.addAttribute("mySells", mySells);

        //мой баланс
        model.addAttribute("userBalance", user.getBalance());

        //чаты со мной
        List<ChatMessage> uniqueChats = messageRepository.findChatsByUser(user);
        Set<User> usersInChats = new HashSet<>();
        for (ChatMessage chat : uniqueChats) {
            if (chat.getSender().getId() == user.getId()) {
                usersInChats.add(chat.getRecipient());
            } else {
                usersInChats.add(chat.getSender());
            }
        }
        model.addAttribute("chats", usersInChats);
        if(user.getUsername().contains(userDetails.getUsername()))
            return "user";
        else
            return "redirect:/login";
    }

    //пополнить свой баланс
    @PostMapping("/addmoney")
    public String addMoney(@AuthenticationPrincipal UserDetails userDetails, @RequestParam double summ) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        user.setBalance(user.getBalance() + summ);
        userRepository.save(user);
        return "redirect:/user";
    }

    @PostMapping("/user/{id}/sendMoney")
    public String sendMoney(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam double summ,
                            @PathVariable(value = "id") long id) {
        User sender = userRepository.findByUsername(userDetails.getUsername());
        User recipient = userRepository.findById(id).orElseGet(User::new);
        if (sender.getBalance() >= summ) {
            sender.setBalance(sender.getBalance() - summ);
            recipient.setBalance(recipient.getBalance() + summ);
            userRepository.save(sender);
            userRepository.save(recipient);
            Transactionals transactionals = new Transactionals(sender, recipient, summ);
            transactionalRepository.save(transactionals);
        }
        return "redirect:/user";
    }

    @GetMapping("/friends")
    public String friends(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        //инфа о пользователе
        User user = userRepository.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "/friends";
    }

    @GetMapping("/mychats")
    public String myChats(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        //чаты со мной
        List<ChatMessage> uniqueChats = messageRepository.findChatsByUser(user);
        Set<User> usersInChats = new HashSet<>();
        for (ChatMessage chat : uniqueChats) {
            if (chat.getSender().getId() == user.getId()) {
                usersInChats.add(chat.getRecipient());
            } else {
                usersInChats.add(chat.getSender());
            }
        }
        model.addAttribute("chats", usersInChats);
        if(user.getUsername().contains(userDetails.getUsername()))
            return "mychats";
        else
            return "redirect:/login";
    }

    //тут мы полученные из формы, расположенной в файле user.html передаем
    //через userRepository, меняем, обновляем и сохраняем
    @PostMapping("/user/update")
    public String updateUser(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute("userForm") User userForm) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user != null) {
            user.setName(userForm.getName());
            user.setSurname(userForm.getSurname());
            user.setEmail(userForm.getEmail());
            userRepository.save(user);
        } else {
            System.out.println("Пользователь не найден!");
        }

        return "redirect:/user";
    }

    //работа с фото пользователя

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/static/photos";
    @PostMapping("/upload") public String uploadImage(Model model, @RequestParam("image") MultipartFile file,
                                                      @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        StringBuilder fileNames = new StringBuilder();
        Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());
        fileNames.append(file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        User user = userRepository.findByUsername(userDetails.getUsername());
        user.setPhoto("/photos/" + file.getOriginalFilename());
        userRepository.save(user);
        return "redirect:/user";
    }

    @GetMapping("/photos")
    public String photos() {
        return "redirect:/index";
    }



    //переход на страницу другого пользователя
    @GetMapping ("/user/{userId}")
    public String showUser(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable(value = "userId") long userID, Model model) {
        User user = userRepository.findById(userID).orElseGet(User::new); //тут находим модель по id пользователя, что бы отправить все его данные из таблицы
        User im = userRepository.findByUsername(userDetails.getUsername()); //а тут находим сами себя через авторизованный userDetails
        model.addAttribute("user", user); //ну и передаем
        model.addAttribute("im", im);
        if(user.getId() == im.getId()) //если мы перешли на свой id, то нас перебрасывает в личный кабинет
            return "redirect:/user";
        else
            return "userid";
    }

    //отправка запроса в друзья
    @GetMapping("/addfriend/{friendId}")
    public String sendFriendRequest(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "friendId") Long friendId) {
        //тут находим самих себя
        User user = userRepository.findByUsername(userDetails.getUsername());
        //находим друга-товарища по его id, который указан в ссылке через
        //@PathVariable(value = "friendId") Long friendId
        User friend = userRepository.findById(friendId).orElseGet(User::new);
        //и через класс friendService выполняем функцию добавления в друзья
        //соответсвенно там тоже база данных
        friendServise.sendFriendRequest(user, friend);
        return "redirect:/user/"+friendId; //ну и переходим на страницу пользователя, страбатывает сразу та функция, что выше
    }

    //принять друга
    @GetMapping("/user/{followerId}/addfriend")
    public String addFriendRequest(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "followerId") Long followerId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        User friend = userRepository.findById(followerId).orElseGet(User::new);
        friendServise.acceptFriendRequest(friend, user); //та же ситуация что и выше, так же через frienService мы принимаем друга
        //класс этот в папке Service
        return "redirect:/user";
    }


    @GetMapping("/registration") //получаем страницу регистрации
    public String reg(@RequestParam(name = "error", defaultValue = "", required = false) String error,
                      Model model) {
        //тут отображается чисто форма, но если придут ссылки на то, что поля не заполнены
        //либо логин или email заняты, то отобразится ошибка
        if (error.equals("username"))
            model.addAttribute("error", "логин занят");
        else if (error.equals("email"))
            model.addAttribute("error", "email занят");
        else if (error.equals("isnull"))
            model.addAttribute("error", "заполните все поля");

        return "registration";
    }

    //добавление новго пользователя регистрации
    @PostMapping("/registration")
    public String addUser(@RequestParam String name,
                          @RequestParam String surname,
                          @RequestParam String username,
                          @RequestParam String phone_number,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam Date birthday) {

        if (userRepository.findByUsername(username) != null)
            return "redirect:/registration?error=username";
        else if (userRepository.findByEmail(email) != null)
            return "redirect:/registration?error=email"; //так же и с email
        else if (name.isEmpty() || surname.isEmpty() || username.isEmpty() || phone_number.isEmpty() ||
                email.isEmpty() || password.isEmpty())
            return "redirect:/registration?error=isnull";
        else {
            password = passwordEncoder.encode(password);
            User user = new User(name, surname, username, phone_number, email, password,
                    birthday, true, Collections.singleton(Role.USER));
            userRepository.save(user);
            return "redirect:/login";
        }
    }
}