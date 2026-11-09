package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void addValidUser() {
        User user = validUser();

        User added = userController.add(user);

        assertNotNull(added.getId());
        assertEquals("user@mail.ru", added.getEmail());
        assertEquals("login", added.getLogin());
        assertEquals("Name", added.getName());
        assertEquals(LocalDate.of(1990, 1, 1), added.getBirthday());
    }

    @Test
    void addUserWithNullLoginShouldThrow() {
        User user = validUser();
        user.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addUserWithBlankLoginShouldThrow() {
        User user = validUser();
        user.setLogin("   ");
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addUserWithSpaceInLoginShouldThrow() {
        User user = validUser();
        user.setLogin("log in");
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addUserWithEmptyNameShouldSetLogin() {
        User user = validUser();
        user.setName("   ");
        User added = userController.add(user);
        assertEquals(user.getLogin(), added.getName());
    }

    @Test
    void addUserWithNullNameShouldSetLogin() {
        User user = validUser();
        user.setName(null);
        User added = userController.add(user);
        assertEquals(user.getLogin(), added.getName());
    }

    @Test
    void addUserWithFutureBirthdayShouldThrow() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addUserWithTodayBirthdayShouldBeOk() {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> userController.add(user));
    }

    @Test
    void addUserWithNullBirthdayShouldBeOk() {
        User user = validUser();
        user.setBirthday(null);
        assertDoesNotThrow(() -> userController.add(user));
    }

    @Test
    void addUserWithNullEmailShouldThrow() {
        User user = validUser();
        user.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addUserWithBlankEmailShouldThrow() {
        User user = validUser();
        user.setEmail("   ");
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addUserWithEmailWithoutAtShouldThrow() {
        User user = validUser();
        user.setEmail("testmail.ru");
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addUserWithDuplicateEmailShouldThrow() {
        userController.add(validUser());
        User user = validUser();
        user.setEmail("USER@MAIL.RU");
        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void addEmptyUserShouldThrow() {
        assertThrows(ValidationException.class, () -> userController.add(new User()));
    }

    @Test
    void updateUserWithNullIdShouldThrow() {
        assertThrows(ValidationException.class, () -> userController.update(new User()));
    }

    @Test
    void updateUserNotFoundShouldThrow() {
        User user = validUser();
        user.setId(999);
        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void updateUserWithInvalidEmailShouldThrow() {
        User user = userController.add(validUser());
        user.setEmail("invalid");
        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void updateUserWithDuplicateEmailShouldThrow() {
        User user1 = userController.add(validUser());

        User user2 = validUser();
        user2.setEmail("second@mail.ru");
        User savedUser2 = userController.add(user2);   // отдельная переменная

        savedUser2.setEmail(user1.getEmail().toUpperCase());

        assertThrows(ValidationException.class, () -> userController.update(savedUser2));
    }

    @Test
    void updateUserWithSpaceInLoginShouldThrow() {
        User user = userController.add(validUser());
        user.setLogin("bad login");
        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void updateUserValid() {
        User user = userController.add(validUser());
        user.setName("Updated Name");
        user.setLogin("updatedLogin");
        user.setEmail("updated@mail.ru");

        User updated = userController.update(user);

        assertEquals(user.getId(), updated.getId());
        assertEquals("Updated Name", updated.getName());
        assertEquals("updatedLogin", updated.getLogin());
        assertEquals("updated@mail.ru", updated.getEmail());
    }

    private User validUser() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}