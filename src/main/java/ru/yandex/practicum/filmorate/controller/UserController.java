package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable Integer userId) {
        return userService.getById(userId);
    }

    @GetMapping("/{userId}/friends")
    public Set<Integer> getFriendsList(@PathVariable Integer userId) {
        return userService.getFriendsList(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Set<Integer> getCommonFriends(@PathVariable Integer userId, @PathVariable Integer otherId) {
        return userService.getCommonFriendsList(userId, otherId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Integer userId) {
        userService.delete(userId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer userId, @PathVariable Integer friendId) {
        userService.removeFriend(userId, friendId);
    }

    @PostMapping
    public User add(@RequestBody User newUser) {
        return userService.add(newUser);
    }

    @PostMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable Integer userId, @PathVariable Integer friendId) {
        userService.addFriend(userId, friendId);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        return userService.update(user);
    }
}