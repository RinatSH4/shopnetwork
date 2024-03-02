package com.ShopNetwork.ShopNetwork.RestApiFiles.services;

import com.ShopNetwork.ShopNetwork.models.ChatMessage;
import com.ShopNetwork.ShopNetwork.models.User;
import com.ShopNetwork.ShopNetwork.repo.MessageRepository;
import com.ShopNetwork.ShopNetwork.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@AllArgsConstructor
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    public void sendMessage(User sender, User recipient, String content) {
        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setTime(LocalDateTime.now());
        messageRepository.save(message);
    }

    public ChatService(){}

}