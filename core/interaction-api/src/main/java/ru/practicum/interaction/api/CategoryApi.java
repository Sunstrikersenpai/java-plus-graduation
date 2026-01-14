package ru.practicum.interaction.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.category.CategoryDto;

import java.util.List;
import java.util.Set;

public interface CategoryApi {
    @GetMapping("/categories/{catId}")
    CategoryDto getById(@PathVariable @Positive @NotNull Long catId);

    @GetMapping("/admin/categories")
    Set<CategoryDto> findByIdIn(@RequestParam(name = "ids", required = false) List<Long> ids);
}
