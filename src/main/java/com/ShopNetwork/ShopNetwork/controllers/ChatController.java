package com.ShopNetwork.ShopNetwork.controllers;

import com.ShopNetwork.ShopNetwork.RestApiFiles.services.ChatService;
import com.ShopNetwork.ShopNetwork.models.*;
import com.ShopNetwork.ShopNetwork.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Controller
@SessionAttributes({"Cart", "verifedItems"})
public class ChatController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ItemChatRepo itemChatRepo;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;



    @GetMapping("/user/{id}/chat")
    public String chat(@PathVariable(value = "id") Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User im = userRepository.findByUsername(userDetails.getUsername());
        User user = userRepository.findById(id).orElseGet(User::new);
        List<ChatMessage> chat = messageRepository.findBySenderAndRecipientOrSenderAndRecipient(im, user, user, im);
        model.addAttribute("messages", chat);
        model.addAttribute("user", user);
        model.addAttribute("im", im);
        long chatId = im.getId() * user.getId() * (im.getId() + user.getId());
        model.addAttribute("chatId", chatId);
        return "/chat";
    }

    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/messages")
    public SendMessage send(@RequestBody SendMessage message,
                            @DestinationVariable Long chatId,
                            Principal principal) throws Exception {
        User im = userRepository.findByUsername(principal.getName());
        User user = userRepository.findByUsername(message.getTo());
        if (im.getUsername().equals(message.getFrom()))
            chatService.sendMessage(im, user, message.getText());

        return new SendMessage(chatId, im.getUsername(), im.getPhoto(), user.getUsername(), message.getText(), String.valueOf(LocalDateTime.now()));
        }


    @MessageMapping("/item/{chatId}/chat")
    @SendTo("/topic/messages")
    public SendMessage sendMessageToItem(@RequestBody SendMessage message,
                                         @DestinationVariable Long chatId,
                                        Principal principal) {
        //chatId - в данном случае это itemId
        User user = userRepository.findByUsername(principal.getName());
        Item item = itemRepository.findById(chatId).orElseGet(Item::new);
        //создаем новое сообщение
        ItemChat itemChatAdd = new ItemChat();
        itemChatAdd.setUser(user);
        itemChatAdd.setContent(message.getText());
        itemChatAdd.setUsername(user.getUsername());
        itemChatAdd.setItem(item);
        itemChatAdd.setTime(LocalDateTime.now());
        itemChatRepo.save(itemChatAdd);
        return new SendMessage(message.getChatId(), user.getUsername(), user.getPhoto(), message.getText(), String.valueOf(LocalDateTime.now()));
    }

    @GetMapping("/item/{id}/chat")
    public String chatItem(@PathVariable(value = "id") Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User im = userRepository.findByUsername(userDetails.getUsername());
        Item item = itemRepository.findById(id).orElseGet(Item::new);
        Iterable<ItemChat> chat = itemChatRepo.findByItemId(id);
        model.addAttribute("messages", chat);
        model.addAttribute("im", im);
        model.addAttribute("item", item);
        return "/itemchat";
    }

    //удаляем сообщение
    @GetMapping("/item/{itemId}/delete-message/{id}")
    public String deleteMessage(@PathVariable(value = "itemId") long itemId,
                                @PathVariable(value = "id") long id,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MODERATOR)) {
            ItemChat message = itemChatRepo.findById(id);
            itemChatRepo.delete(message);
        }
        return "redirect:/item/" + itemId +"/chat";
    }
}
