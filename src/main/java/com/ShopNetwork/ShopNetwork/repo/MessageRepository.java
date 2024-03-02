package com.ShopNetwork.ShopNetwork.repo;

import com.ShopNetwork.ShopNetwork.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.HashSet;
import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderAndRecipientOrSenderAndRecipient(User sender, User recipient, User recipient2, User sender2);
    @Query("SELECT DISTINCT m FROM ChatMessage m WHERE m.sender = :user OR m.recipient = :user")
    List<ChatMessage> findChatsByUser(@Param("user") User user);

    List<ChatMessage> findByChatId(long chatId);
}

