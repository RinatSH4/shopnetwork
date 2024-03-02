package com.ShopNetwork.ShopNetwork.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /* имя, фамилия, ник, номер телефона
                почта, пароль */
    private String name, surname, username, phone_number, email, password, photo;

    /* дата рождения, дата регистрации пользователя
     1990-11-29 - такой формат*/
    private Date date_of_birth;

    private Date date_of_registration;

    //проверка, подтвержденный ли пользователь
    private boolean enabled;
    private boolean active = false; //онлайн или оффлайн изначально оффлайн

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Item> items = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Orders> order = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Review> review = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Rating> ratings = new HashSet<>();


    @ManyToMany
    @JoinTable(
            name = "friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @ManyToMany(mappedBy = "friends")
    private Set<User> friendOf = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "followers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<User> following = new HashSet<>();

    private double balance;

    public User(String name, String surname, String username, String phone_number, String email, String password, Date date_of_birth, boolean enabled, Set<Role> roles) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.phone_number = phone_number;
        this.email = email;
        this.password = password;
        this.date_of_birth = date_of_birth;
        this.date_of_registration = new Date();
        this.enabled = true;
        this.roles = roles;
        this.photo = "https://upload.shejihz.com/wp-content/uploads/2020/09/dab44b50e70916cfdea7739e81c9c324.jpg";
    }

    public User(String name, String surname, String username, String phone_number, String email, String password, Date date_of_birth, boolean enabled, Set<Role> roles, Set<Item> items) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.phone_number = phone_number;
        this.email = email;
        this.password = password;
        this.date_of_birth = date_of_birth;
        this.date_of_registration = new Date();
        this.enabled = true;
        this.roles = roles;
        this.items = items;
        this.photo = "https://upload.shejihz.com/wp-content/uploads/2020/09/dab44b50e70916cfdea7739e81c9c324.jpg";
    }

    public User() {
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }
}
