package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {
    Collection<User> findAll();
    User add(User newUser);
    User update(User user);
    void delete(Integer id);
    User getById(Integer id);
    void addFriend(Integer userId, Integer friendId);
    void removeFriend(Integer userId, Integer friendId);
    Set<Integer> getFriendsList(Integer userId);
    Set<Integer> getCommonFriendsList(Integer userId1, Integer userId2);
    void clear();
}
