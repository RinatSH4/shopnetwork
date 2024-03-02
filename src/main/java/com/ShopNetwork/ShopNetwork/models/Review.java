package com.ShopNetwork.ShopNetwork.models;
//отзывы

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length=1000)
    private String text;
    private String username;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @JoinColumn(name = "item_id")
    @OneToOne(fetch = FetchType.EAGER)
    private Item item;


    public Review(String text, String username, User user, Item item) {
        this.text = text;
        this.username = username;
        this.item = item;
        this.user = user;
    }

    public Review(String text, String username) {
        this.text = text;
        this.username = username;
    }

    public Review() {}

}
