package category;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.interaction.JacksonConfig;
import ru.practicum.interaction.exception.ApiExceptionHandler;

@SpringBootApplication
@Import({ApiExceptionHandler.class, JacksonConfig.class})
@EnableFeignClients(clients = {EventClient.class})
public class CategoryServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApp.class,args);
    }
}
