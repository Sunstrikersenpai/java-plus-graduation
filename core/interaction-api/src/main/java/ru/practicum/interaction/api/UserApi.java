package ru.practicum.interaction.api;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.List;
import java.util.Set;

public interface UserApi {
    @GetMapping("/admin/users/{userId}")
    UserShortDto getUserById(@Positive @PathVariable("userId") Long userId);

    @GetMapping("/admin/users/idIn")
    Set<UserShortDto> findByIdIn(@RequestParam(name = "ids") List<Long> ids);
}
