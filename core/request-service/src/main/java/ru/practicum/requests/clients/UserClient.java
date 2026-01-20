package ru.practicum.requests.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.UserApi;

@FeignClient(name = "user-service")
public interface UserClient extends UserApi {
}
