package com.ShopNetwork.ShopNetwork;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;



public class EmailService {

    public void SendEmailJWT (String recipientEmail, String text) {
        final String username = "shop.network@bk.ru";
        final String password = "KPGBNpS2uipwAFym8Npv";

        // Настройки для подключения к серверу SMTP на mail.ru
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.mail.ru");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // Создание объекта сессии для аутентификации
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Создание объекта письма
            Message message = new MimeMessage(session);

            // Установка отправителя
            message.setFrom(new InternetAddress(username));

            // Установка получателя
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

            // Установка темы письма
            message.setSubject("Вы авторизовались");

            // Установка содержимого письма
            message.setText(text);

            // Отправка письма
            Transport.send(message);

            System.out.println("Письмо успешно отправлено!");

        } catch (MessagingException e) {
            System.err.println("Ошибка при отправке письма: " + e.getMessage());
        }
    }
}
