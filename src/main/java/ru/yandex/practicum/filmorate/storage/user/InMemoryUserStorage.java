package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User add(User newUser) {
        newUser.setId(idGenerator.getAndIncrement());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User update(User user) {
        User oldUser = users.get(user.getId());

        oldUser.setName(user.getName());
        oldUser.setLogin(user.getLogin());
        oldUser.setBirthday(user.getBirthday());
        oldUser.setEmail(user.getEmail());
        return oldUser;
    }

    @Override
    public void delete(Integer id) {
        users.remove(id);
    }

    @Override
    public User getById(Integer id) {
        return users.get(id);
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        Set<Integer> userFriends = friends.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }
    }

    @Override
    public Set<Integer> getFriendsList(Integer userId) {
        return friends.getOrDefault(userId, new HashSet<>());
    }

    @Override
    public Set<Integer> getCommonFriendsList(Integer userId1, Integer userId2) {
        Set<Integer> friends1 = friends.getOrDefault(userId1, new HashSet<>());
        Set<Integer> friends2 = friends.getOrDefault(userId2, new HashSet<>());
        Set<Integer> commonFriends = new HashSet<>(friends1);
        commonFriends.retainAll(friends2);
        return commonFriends;
    }

    @Override
    public void clear() {
        users.clear();
        idGenerator.set(1);
    }
}
