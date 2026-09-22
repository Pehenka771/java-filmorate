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
        String login = newUser.getLogin();
        String name = newUser.getName();
        LocalDate birthday = newUser.getBirthday();
        String email = newUser.getEmail();

        if (login == null) {
            log.warn("При добавлении пользователя не указан логин");
            throw new ValidationException("Логин не введен");
        } else if (login.isBlank() || login.contains(" ")) {
            log.warn("Попытка добавить пользователя с пустым логином или в логине есть пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (name == null || name.isBlank()) {
            newUser.setName(login);
            log.info("Поле имя оказалось пустым, имя установлено таким же как логин при добавлении пользователя, " +
                    "login={}", login);
        } else {
            newUser.setName(name);
            log.info("Успешно добавлено имя для нового пользователя, name={}", name);
        }
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            log.warn("При добавлении пользователя дата рождения указана в будущем, birthday={}", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        newUser.setBirthday(birthday);
        log.info("Успешно добавлена дата рождения для нового пользователя");
        if (email == null) {
            log.warn("При добавлении пользователя не указан имейл");
            throw new ValidationException("Имейл не введен");
        } else if (email.isBlank()) {
            log.warn("При добавлении пользователя указан пустой имейл");
            throw new ValidationException("Электронная почта не может быть пустой");
        } else if (!email.contains("@")) {
            log.warn("При добавлении пользователя имейл не содержит символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        String trimmedEmail = email.trim();
        boolean isEmailTaken = users.values().stream().anyMatch(existingUser ->
                existingUser != null && existingUser.getEmail() != null && existingUser.getEmail()
                        .equalsIgnoreCase(trimmedEmail));
        if (isEmailTaken) {
            log.warn("При добавлении имейла для нового пользователя обнаружен дубликат");
            throw new ValidationException("Этот имейл уже используется");
        }
        newUser.setEmail(trimmedEmail);
        log.info("Успешно добавлен имейл для нового пользователя, email={}", email);
        newUser.setId(getNextId());
        log.info("Получение id для нового пользователя, id={}", newUser.getId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id={} успешно добавлен", newUser.getId());
        return newUser;
    }

    private Integer getNextId() {
        int currentMaxId = Math.toIntExact(users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0));
        return ++currentMaxId;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        String login = user.getLogin();
        String name = user.getName();
        Integer id = user.getId();
        String email = user.getEmail();
        LocalDate birthday = user.getBirthday();

        if (id == null) {
            log.warn("Не указан id при обновлении пользователя");
            throw new ValidationException("Не указан id");
        }
        if (users.containsKey(id)) {
            User oldUser = users.get(id);
            if (name != null && !name.isBlank()) {
                oldUser.setName(name);
                log.info("У пользователя с id={} успешно обновлено имя на {}", id, name);
            }
            if (birthday != null && birthday.isBefore(LocalDate.now())) {
                oldUser.setBirthday(birthday);
            }
            if (email != null) {
                if (email.isBlank() || !email.contains("@")) {
                    log.warn("При обновлении имейла новый указан или пустым или без @, id={}", id);
                    throw new ValidationException("Некорректный формат email");
                }
                String trimmedEmail = email.trim();
                boolean isEmailTaken = users.values().stream().anyMatch(existingUser ->
                        !existingUser.getId().equals(oldUser.getId()) &&
                                existingUser.getEmail() != null &&
                                existingUser.getEmail().equalsIgnoreCase(trimmedEmail));
                if (isEmailTaken) {
                    log.warn("При обновлении имейла обнаружен дубликат, id={}", id);
                    throw new ValidationException("Этот имейл уже используется");
                }
                oldUser.setEmail(trimmedEmail);
                log.info("У пользователя с id={} успешно обновлен имейл", id);
            }
            if (login != null) {
                if (login.isBlank() || login.contains(" ")) {
                    log.warn("При обновлении логина указан пустой логин или логин с пробелами, id={}", id);
                    throw new ValidationException("Логин не может быть пустым и содержать пробелы");
                }
                oldUser.setLogin(login);
                log.info("У пользователя с id={} успешно обновлен логин", id);
            }
            return oldUser;
        }
        log.warn("Пользователь с id={} не найден", id);
        throw new ValidationException("Пользователь с id = " + id + " не найден");
    }
}