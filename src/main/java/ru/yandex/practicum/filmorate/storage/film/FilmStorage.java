package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();
    Film add(Film newFilm);
    Film update(Film film);
    void delete(Integer id);
    Film getById(Integer id);
    void addLike(Integer filmId, Integer userId);
    void removeLike(Integer filmId, Integer userId);
    List<Film> getMostPopularFilms(Integer count);
    void clear();
}
