package com.ShopNetwork.ShopNetwork.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter

public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long chatId;

    private String content;

    private LocalDateTime time;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    public ChatMessage(User sender, User recipient, String content) {
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.time = LocalDateTime.now();
    }

    @PrePersist
    public void generateChatIdIfAbsent() {
        if (this.chatId == 0) { // Проверяем, отсутствует ли chatId
            // Создаем уникальный chatId на основе sender_id и recipient_id
            this.chatId = Math.abs(sender.getId() + recipient.getId());
        }
    }
    public ChatMessage() {
    }
}