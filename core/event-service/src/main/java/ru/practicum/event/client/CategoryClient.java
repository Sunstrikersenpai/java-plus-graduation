package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.CategoryApi;

@FeignClient(name = "category-service")
public interface CategoryClient extends CategoryApi {
}
