package com.ShopNetwork.ShopNetwork.models;

public enum OrderStatus {

    /*
    статусы заказа:

    Pending - В ожидании
    Processing - Обработка
    Shipped - Отправлен
    Delivered - Доставлен
    Cancelled - Отменён
    Refunded - Возвращено
    On Hold - Ожидание
    Completed - Завершён
    */


    NOPAID("Не оплачен"),
    PAID("Оплачен"),
    PENDING("Принят"),
    SHIPPED("Отправлен"),
    DELIVERED("Получен");
    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
