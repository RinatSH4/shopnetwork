package com.ShopNetwork.ShopNetwork.service;

import com.ShopNetwork.ShopNetwork.models.User;
import com.ShopNetwork.ShopNetwork.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FriendServise {

    @Autowired
    private UserRepository userRepository;

    // Метод для отправки запроса в друзья
    public void sendFriendRequest(User user, User friend) {
        friend.getFollowers().add(user);
        userRepository.save(friend);
    }

    // Метод для принятия запроса в друзья
    public void acceptFriendRequest(User friend, User user) {
        friend.getFriends().add(user);
        friend.getFriendOf().add(user);
        user.getFollowers().remove(friend);
        user.getFollowing().remove(friend);
        user.getFriends().add(friend);
        userRepository.save(friend);
        userRepository.save(user);
    }
}
