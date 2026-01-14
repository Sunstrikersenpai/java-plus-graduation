package ru.practicum.requests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.interaction.JacksonConfig;
import ru.practicum.interaction.exception.ApiExceptionHandler;
import ru.practicum.requests.clients.EventClient;
import ru.practicum.requests.clients.UserClient;

@SpringBootApplication
@Import({ApiExceptionHandler.class, JacksonConfig.class})
@EnableFeignClients(clients = {UserClient.class, EventClient.class})
public class RequestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class,args);
    }
}
