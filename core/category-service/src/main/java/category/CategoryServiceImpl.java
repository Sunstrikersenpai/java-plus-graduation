package category;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.category.NewCategoryDto;
import ru.practicum.interaction.dto.category.UpdateCategoryDto;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static category.CategoryMapper.toCategory;
import static category.CategoryMapper.toCategoryDto;


@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventClient eventClient;

    @Override
    public Collection<CategoryDto> getAllCategory(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return toCategoryDto(getCategory(catId));
    }

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        // для обработки в ErrorHandler - DuplicatedDataException.class, DataIntegrityViolationException.class
        // HttpStatus.CONFLICT)
        return toCategoryDto(categoryRepository.save(toCategory(newCategoryDto)));
    }

    @Override
    public CategoryDto updateCategory(Long catId, UpdateCategoryDto updateCategoryDto) {
        Category category = getCategory(catId);
        category.setName(updateCategoryDto.getName());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = getCategory(catId);
        if (!eventClient.getEventsByCategory(catId).isEmpty()) {
            throw new ConflictException("Category has existing events");
        }
        categoryRepository.delete(category);
    }

    @Override
    public Set<CategoryDto> findAllByIdIn(List<Long> ids) {
        return categoryRepository.findAllByIdIn(ids).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toSet());
    }

    // вспомогательный метод
    public Category getCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(
                // для обработки в ErrorHandler - NotFoundException - HttpStatus.NOT_FOUND
                () -> new NotFoundException(String.format("Категории с ID = %s не существует", catId)));
    }
}
