package com.ShopNetwork.ShopNetwork.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class Transactionals {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @JoinColumn(name = "sender_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User sender;

    @JoinColumn(name = "recipient_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User recipient;

    private double summ;
    private Date date;

    public Transactionals(User sender, User recipient, double summ) {
        this.sender = sender;
        this.recipient = recipient;
        this.summ = summ;
        this.date = new Date();
    }
}
