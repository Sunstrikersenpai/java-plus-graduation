package ru.practicum.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.practicum.interaction.JacksonConfig;
import ru.practicum.interaction.exception.ApiExceptionHandler;

@SpringBootApplication
@Import({ApiExceptionHandler.class, JacksonConfig.class})
public class UserServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApp.class, args);
    }
}