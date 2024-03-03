package com.ShopNetwork.ShopNetwork.models;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String title, info, image;

    private float price;

    private boolean verifed = false;
    private boolean enabled;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private Set<Review> review = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private Set<Orders> order = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private Set<Rating> ratings = new HashSet<>();

    public Item(String title, String info, String image, float price, User user) {
        this.title = title;
        this.info = info;
        this.image = image;
        this.price = price;
        this.user = user;
        this.enabled = true;
    }

    public void editItem(String title, String info, String image, float price) {
        this.title = title;
        this.info = info;
        this.image = image;
        this.price = price;
    }

    public Item() {
        this.enabled = true;
    }
}
