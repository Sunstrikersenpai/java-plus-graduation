package compilation;


import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.compilations.AdminNewCompilationParamDto;
import ru.practicum.interaction.dto.compilations.CompilationDto;
import ru.practicum.interaction.dto.event.EventShortDto;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class CompilationMapper {
    public static Compilation toEntity(AdminNewCompilationParamDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .build();
    }

    public static CompilationDto toDto(Compilation entity, Map<Long,EventShortDto> eventDtoMap) {
        return CompilationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .pinned(entity.getPinned())
                .events(entity.getEvents().stream()
                        .map(eventDtoMap::get)
                        .toList())
                .build();
    }

    public static CompilationDto toDto(Compilation entity, Set<EventShortDto> eventDtoSet) {
        return CompilationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .pinned(entity.getPinned())
                .events(new ArrayList<>(eventDtoSet))
                .build();
    }

    public static Compilation toEntity(AdminNewCompilationParamDto dto, Set<EventShortDto> events) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(events.stream().map(EventShortDto::getId).collect(Collectors.toSet()))
                .build();
    }


}
