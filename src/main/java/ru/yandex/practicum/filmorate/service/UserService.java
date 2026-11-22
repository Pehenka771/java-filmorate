package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User add(User newUser) {
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

        return userStorage.add(newUser);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Не указан id при обновлении пользователя");
            throw new ValidationException("Не указан id");
        }
        checkUserExists(user.getId());
        validate(user, true);

        User oldUser = userStorage.getById(user.getId());
        if (user.getName() != null && !user.getName().isBlank()) {
            oldUser.setName(user.getName());
            log.info("У пользователя с id={} успешно обновлено имя на {}", oldUser.getId(), oldUser.getName());
        }
        if (user.getLogin() != null) {
            checkLoginUnique(user.getLogin(), oldUser.getId());
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
        return userStorage.update(oldUser);
    }

    public void delete(Integer userId) {
        checkUserExists(userId);
        userStorage.delete(userId);
    }

    public User getById(Integer userId) {
        checkUserExists(userId);
        return userStorage.getById(userId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public void addFriend(Integer userId, Integer friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        userStorage.addFriend(userId, friendId);
    }

    public Set<Integer> getFriendsList(Integer userId) {
        checkUserExists(userId);
        return userStorage.getFriendsList(userId);
    }

    public Set<Integer> getCommonFriendsList(Integer userId1, Integer userId2) {
        if (userId1 == null || userId2 == null) {
            throw new ValidationException("Не указаны id пользователя/пользователей");
        }
        checkUserExists(userId1);
        checkUserExists(userId2);
        return userStorage.getCommonFriendsList(userId1, userId2);
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

    private void checkUserExists(Integer userId) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private void checkEmailUnique(String email, Integer excludeUserId) {
        String normalized = email.toLowerCase();
        boolean isEmailTaken = userStorage.findAll().stream().anyMatch(existingUser ->
                existingUser != null
                        && existingUser.getEmail() != null
                        && !existingUser.getId().equals(excludeUserId)
                        && existingUser.getEmail().toLowerCase().equals(normalized));
        if (isEmailTaken) {
            log.warn("Обнаружен дубликат email={}", email);
            throw new ValidationException("Этот имейл уже используется");
        }
    }

    private void checkLoginUnique(String login, Integer id) {
        boolean isTaken = userStorage.findAll().stream().anyMatch(l -> l.getLogin() != null &&
                l.getLogin().equals(login) &&
                !l.getId().equals(id));
        if (isTaken) {
            log.warn("Обнаружен дубликат login={}", login);
            throw new ValidationException("Пользователь с таким логином уже существует");
        }
    }
}
