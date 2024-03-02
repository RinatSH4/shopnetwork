package com.ShopNetwork.ShopNetwork.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemChat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String content, username;

    private LocalDateTime time;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @JoinColumn(name = "item_id")
    @OneToOne(fetch = FetchType.EAGER)
    private Item item;

}
