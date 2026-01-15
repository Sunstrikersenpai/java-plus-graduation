package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.practicum.event.client.CategoryClient;
import ru.practicum.event.client.RequestClient;
import ru.practicum.event.client.UserClient;
import ru.practicum.interaction.JacksonConfig;
import ru.practicum.interaction.exception.ApiExceptionHandler;

@SpringBootApplication
@Import({ApiExceptionHandler.class, JacksonConfig.class})
@EnableFeignClients(clients = {UserClient.class, RequestClient.class, CategoryClient.class})
@ComponentScan("ru.practicum")
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class,args);
    }
}
