package com.ShopNetwork.ShopNetwork.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Getter
@Setter
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @JoinColumn(name = "item_id")
    @OneToOne(fetch = FetchType.EAGER)
    private Item item;

    private int rating;

    public Rating(User user, Item item, int rating) {
        this.user = user;
        this.item = item;
        this.rating = rating;
    }

    public Rating() {
    }
}
