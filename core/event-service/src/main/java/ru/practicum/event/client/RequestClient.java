package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.RequestApi;

@FeignClient(name="request-service")
public interface RequestClient extends RequestApi {
}
