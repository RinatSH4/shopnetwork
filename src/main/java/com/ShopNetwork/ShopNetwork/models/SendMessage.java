package com.ShopNetwork.ShopNetwork.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SendMessage {
    private Long chatId;
    private String from;
    private String image;
    private String to;
    private String text;
    private String time;

    public SendMessage(Long chatId, String from, String image, String text, String time) {
        this.chatId = chatId;
        this.from = from;
        this.image = image;
        this.text = text;
        this.time = time;
    }

    public SendMessage() {}
}
