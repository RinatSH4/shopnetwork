package com.ShopNetwork.ShopNetwork.models;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String address;

    @ElementCollection(targetClass = OrderStatus.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "orders_status", joinColumns = @JoinColumn(name = "orders_id"))
    @Enumerated(EnumType.STRING)
    private Set<OrderStatus> orderStatusSet;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @JoinColumn(name = "seller_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User seller;

    @JoinColumn(name = "item_id")
    @OneToOne(fetch = FetchType.EAGER)
    private Item item;

    private boolean isPay;
    private double summ;

    public Orders(Item item, User user, User seller, String address, Set<OrderStatus> orderStatusSet) {
        this.item = item;
        this.user = user;
        this.seller = seller;
        this.address = address;
        this.orderStatusSet = orderStatusSet;
        this.isPay = false;
    }

    public Orders() {
        summ = 0;
    }
}
