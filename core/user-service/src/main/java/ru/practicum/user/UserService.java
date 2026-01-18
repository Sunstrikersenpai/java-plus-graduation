package ru.practicum.user;

import ru.practicum.interaction.dto.user.GetUserRequest;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserService {
    Collection<UserDto> getAllUsers(GetUserRequest getUserRequest);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    void activateUser(Long userId, ActiveUser approved);

    User getUser(Long id);

    Set<UserShortDto> findUsersByIdIn(List<Long> ids);
}
