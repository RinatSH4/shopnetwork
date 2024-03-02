package com.ShopNetwork.ShopNetwork.models;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }
    public void setItems(Item item) {
        items.add(item);
    }

    public Cart() {
        this.items = new ArrayList<>();
    }

    public void clearAll() {
        items.clear();
    }

    public void deleteItem(int id) {
        items.remove(id+1);
    }

    public int getItemIndex(Item item) {
        return items.indexOf(item);
    }

}
