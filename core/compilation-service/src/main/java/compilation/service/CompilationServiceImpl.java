package compilation.service;

import compilation.Compilation;
import compilation.CompilationMapper;
import compilation.CompilationRepository;
import compilation.EventClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.dto.compilations.AdminNewCompilationParamDto;
import ru.practicum.interaction.dto.compilations.AdminUpdateCompilationParamDto;
import ru.practicum.interaction.dto.compilations.CompilationDto;
import ru.practicum.interaction.dto.compilations.PublicCompilationRequestParamsDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.exception.EntityNotExistsException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;


    /**
     * Добавление новой подборки
     *
     * @param adminNewCompilationParamDto Параметры новой подборки
     * @return Новая подборка CompilationDto
     */
    @Override
    public CompilationDto addCompilation(AdminNewCompilationParamDto adminNewCompilationParamDto) {

        // Проверка на null
        if (adminNewCompilationParamDto == null) {
            throw new IllegalArgumentException("Отсутствуют данные для новой подборки.");
        }

        // Проверки названия
        String title = adminNewCompilationParamDto.getTitle();
        if (title.isBlank()) {
            throw new IllegalArgumentException("В подборке отсутствует название.");
        }


        Set<EventShortDto> events = new HashSet<>();
        if (adminNewCompilationParamDto.getEvents() != null && !adminNewCompilationParamDto.getEvents().isEmpty()) {
            events = eventClient.findAllByIdIn(adminNewCompilationParamDto.getEvents());
        }
        Compilation compilation = CompilationMapper.toEntity(adminNewCompilationParamDto, events);
        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toDto(saved, events);
    }

    /**
     * Удаление подборки по ID
     *
     * @param compId ID удаляемой подборки
     */
    @Override
    public void deleteCompilation(Long compId) {

        // Проверка на существование подборки
        validateCompilationExists(compId);
        compilationRepository.deleteById(compId);
    }

    /**
     * Обновление информации подборки с заданной ID на переданную информацию
     *
     * @param compId                         ID изменяемой подборки
     * @param adminUpdateCompilationParamDto Изменяемая информация
     * @return Измененная подборка CompilationDto
     */
    @Override
    public CompilationDto updateCompilation(Long compId, AdminUpdateCompilationParamDto adminUpdateCompilationParamDto) {
        // Проверка на существование подборки
        Compilation exitingCompilation = validateCompilationExists(compId);

        // Обновление полей подборки
        exitingCompilation.updateDetails(adminUpdateCompilationParamDto.getTitle(), adminUpdateCompilationParamDto.getPinned());

        // Обновляем список событий (если передан в запросе)
        Set<EventShortDto> events = new HashSet<>();
        if (adminUpdateCompilationParamDto.getEvents() != null) {
            events = eventClient.findAllByIdIn(adminUpdateCompilationParamDto.getEvents());
            exitingCompilation.replaceEvents(events);
        }

        compilationRepository.save(exitingCompilation);
        return CompilationMapper.toDto(exitingCompilation, events);

    }

    /**
     * Получение подборки по ID = complId
     *
     * @param complId ID подборки
     * @return CompilationDto
     */
    @Override
    public CompilationDto getCompilationById(long complId) {
        Compilation compilation = validateCompilationExists(complId);
        Set<EventShortDto> events = eventClient.findAllByIdIn(new ArrayList<>(compilation.getEvents()));
        return CompilationMapper.toDto(compilation, events);
    }

    /**
     * Получение подборок событий. Параметры звпроса: PublicCompilationRequestParamsDto
     *
     * @param params Входные параметры запроса PublicCompilationRequestParamsDto
     * @return List<CompilationDto>
     */
    @Override
    public List<CompilationDto> getCompilationsList(PublicCompilationRequestParamsDto params) {

        Pageable pageRequest = params.toPageable();

        List<Compilation> compilations;
        if (params.getPinned() != null) {
            compilations = compilationRepository.findAllByPinned(params.getPinned(), pageRequest);
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }

        Set<Long> eventIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .collect(Collectors.toSet());

        Map<Long, EventShortDto> eventMap = eventClient.findAllByIdIn(new ArrayList<>(eventIds)).stream()
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        return compilations.stream()
                .map(compilation -> CompilationMapper.toDto(compilation, eventMap))
                .collect(Collectors.toList());
    }


    /**
     * Проверка переданного в поиск ID подборки
     *
     * @param compId ID подборки
     * @return Если существует, возвращается Compilation
     */
    public Compilation validateCompilationExists(Long compId) {
        log.warn("validateCompilationExists(Long {})", compId);
        // Проверка на null ID
        if (compId == null) {
            throw new EntityNotExistsException("ID подборки не может быть null");
        }

        // Проверка существования подборки
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotExistsException("Подборка с ID " + compId + " не найден"));
    }

}
