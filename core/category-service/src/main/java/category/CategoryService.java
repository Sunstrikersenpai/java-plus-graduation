package category;


import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.category.NewCategoryDto;
import ru.practicum.interaction.dto.category.UpdateCategoryDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CategoryService {
    Collection<CategoryDto> getAllCategory(Integer from, Integer size);

    CategoryDto getCategoryById(Long catId);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long catId, UpdateCategoryDto updateCategoryDto);

    void deleteCategory(Long catId);

    Category getCategory(Long catId);

    Set<CategoryDto> findAllByIdIn(List<Long> ids);
}
