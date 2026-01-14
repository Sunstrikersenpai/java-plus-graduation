package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.UserApi;

@FeignClient(name="user-service")
public interface UserClient extends UserApi {
}
