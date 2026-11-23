package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film add(Film newFilm) {
        newFilm.setId(idGenerator.getAndIncrement());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());
        return oldFilm;
    }

    @Override
    public void delete(Integer id) {
        films.remove(id);
    }

    @Override
    public Film getById(Integer id) {
        return films.get(id);
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        film.getLikes().remove(userId);
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        return films.values().stream().sorted(Comparator.<Film>comparingInt(f -> f.getLikes().size()).reversed())
                .limit(count).collect(Collectors.toList());
    }

    @Override
    public void clear() {
        films.clear();
        idGenerator.set(1);
    }
}
