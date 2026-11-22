package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController userController;
    @Autowired
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage.clear();
    }

    @Test
    void addValidUser() {
        User user = validUser();

        User added = userController.add(user);

        assertNotNull(added.getId());
        assertEquals(user.getEmail(), added.getEmail());
        assertEquals(user.getLogin(), added.getLogin());
        assertEquals(user.getName(), added.getName());
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
        User user1 = userController.add(validUser());
        User user2 = validUser();
        user2.setEmail(user1.getEmail());
        user2.setLogin("user2");
        assertThrows(ValidationException.class, () -> userController.add(user2));
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
        assertThrows(NotFoundException.class, () -> userController.update(user));
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

    @Test
    void getByIdExistingUser() {
        User user = userController.add(validUser());
        User found = userController.getById(user.getId());
        assertEquals(user.getId(), found.getId());
        assertEquals(user.getEmail(), found.getEmail());
        assertEquals(user.getLogin(), found.getLogin());
    }

    @Test
    void getByIdNonExistingUser() {
        assertThrows(NotFoundException.class, () -> userController.getById(999));
    }

    @Test
    void deleteExistingUser() {
        User user = userController.add(validUser());
        userController.delete(user.getId());
        assertThrows(NotFoundException.class, () -> userController.getById(user.getId()));
    }

    @Test
    void deleteNonExistingUser() {
        assertThrows(NotFoundException.class, () -> userController.delete(999));
    }

    @Test
    void addFriendAndGetList() {
        User user1 = userController.add(validUser());
        User user2 = userController.add(validUser());
        userController.addFriend(user1.getId(), user2.getId());
        Collection<User> friends = userController.getFriendsList(user1.getId());
        assertTrue(containsUser(friends, user2.getId()));
        assertEquals(1, friends.size());
    }

    @Test
    void removeFriend() {
        User user1 = userController.add(validUser());
        User user2 = userController.add(validUser());
        userController.addFriend(user1.getId(), user2.getId());
        userController.removeFriend(user1.getId(), user2.getId());
        Collection<User> friends = userController.getFriendsList(user1.getId());
        assertFalse(containsUser(friends, user2.getId()));
        assertEquals(0, friends.size());
    }

    @Test
    void addFriendToNonExistingUser() {
        User user = userController.add(validUser());
        assertThrows(NotFoundException.class, () -> userController.addFriend(user.getId(), 999));
        assertThrows(NotFoundException.class, () -> userController.addFriend(999, user.getId()));
    }

    @Test
    void addFriendToSelfShouldThrow() {
        User user = userController.add(validUser());
        assertThrows(ValidationException.class, () -> userController.addFriend(user.getId(), user.getId()));
    }

    @Test
    void getCommonFriends() {
        User user1 = userController.add(validUser());
        User user2 = userController.add(validUser());
        User user3 = userController.add(validUser());
        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());
        userController.addFriend(user2.getId(), user1.getId());
        userController.addFriend(user2.getId(), user3.getId());
        userController.addFriend(user3.getId(), user1.getId());
        userController.addFriend(user3.getId(), user2.getId());
        Collection<User> common = userController.getCommonFriends(user1.getId(), user2.getId());
        assertEquals(1, common.size());
        assertTrue(containsUser(common, user3.getId()));
    }

    private static int counter = 0;

    private User validUser() {
        User user = new User();
        String suffix = String.valueOf(counter++);
        user.setEmail("user" + suffix + "@mail.ru");
        user.setLogin("login" + suffix);
        user.setName("Name" + suffix);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private static boolean containsUser(Collection<User> users, Integer id) {
        return users.stream().anyMatch(u -> u.getId().equals(id));
    }
}