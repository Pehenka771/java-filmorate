package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User add(@RequestBody User newUser) {
        validate(newUser, false);

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
            log.info("Поле имя пустое, установлено равным логину, login={}", newUser.getLogin());
        } else {
            log.info("Успешно добавлено имя для нового пользователя, name={}", newUser.getName());
        }

        String trimmedEmail = newUser.getEmail().trim();
        checkEmailUnique(trimmedEmail, null);
        newUser.setEmail(trimmedEmail);

        newUser.setId(getNextId());
        log.info("Получение id для нового пользователя, id={}", newUser.getId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id={} успешно добавлен", newUser.getId());
        return newUser;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (user.getId() == null) {
            log.warn("Не указан id при обновлении пользователя");
            throw new ValidationException("Не указан id");
        }
        User oldUser = users.get(user.getId());
        if (oldUser == null) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new ValidationException("Пользователь с id = " + user.getId() + " не найден");
        }

        validate(user, true);

        if (user.getName() != null && !user.getName().isBlank()) {
            oldUser.setName(user.getName());
            log.info("У пользователя с id={} успешно обновлено имя на {}", oldUser.getId(), oldUser.getName());
        }
        if (user.getLogin() != null) {
            oldUser.setLogin(user.getLogin());
            log.info("У пользователя с id={} успешно обновлен логин", oldUser.getId());
        }
        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
            log.info("У пользователя с id={} успешно обновлена дата рождения", oldUser.getId());
        }
        if (user.getEmail() != null) {
            String trimmedEmail = user.getEmail().trim();
            checkEmailUnique(trimmedEmail, oldUser.getId());
            oldUser.setEmail(trimmedEmail);
            log.info("У пользователя с id={} успешно обновлен имейл", oldUser.getId());
        }
        return oldUser;
    }

    private Integer getNextId() {
        int currentMaxId = Math.toIntExact(users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0));
        return ++currentMaxId;
    }

    private void validate(User data, boolean isUpdate) {
        String email = data.getEmail();
        if (email == null) {
            if (!isUpdate) {
                log.warn("При добавлении пользователя не указан имейл");
                throw new ValidationException("Имейл не введен");
            }
        } else if (email.isBlank()) {
            log.warn("Указан пустой имейл");
            throw new ValidationException("Электронная почта не может быть пустой");
        } else if (!email.contains("@")) {
            log.warn("Имейл не содержит символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        String login = data.getLogin();
        if (login == null) {
            if (!isUpdate) {
                log.warn("При добавлении пользователя не указан логин");
                throw new ValidationException("Логин не введен");
            }
        } else if (login.isBlank() || login.contains(" ")) {
            log.warn("Логин пустой или содержит пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        LocalDate birthday = data.getBirthday();
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            log.warn("Дата рождения указана в будущем, birthday={}", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void checkEmailUnique(String email, Integer excludeUserId) {
        String normalized = email.toLowerCase();
        boolean isEmailTaken = users.values().stream().anyMatch(existingUser ->
                existingUser != null
                        && existingUser.getEmail() != null
                        && !existingUser.getId().equals(excludeUserId)
                        && existingUser.getEmail().toLowerCase().equals(normalized));
        if (isEmailTaken) {
            log.warn("Обнаружен дубликат email={}", email);
            throw new ValidationException("Этот имейл уже используется");
        }
    }
}