package com.ShopNetwork.ShopNetwork.repo;

import com.ShopNetwork.ShopNetwork.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Iterable<Item> findByUserId(long userId);
    Iterable<Item> findByVerifed(boolean verified);
    @Query("SELECT i FROM Item i WHERE i.title LIKE %:searchText% AND i.verifed = :value")
    List<Item> findItemByText(@Param("searchText") String searchText, @Param("value") boolean verifed);
}
