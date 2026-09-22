package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film add(@RequestBody Film newFilm) {
        String description = newFilm.getDescription();
        String name = newFilm.getName();
        LocalDate releaseDate = newFilm.getReleaseDate();

        if (name == null || name.isBlank()) {
            log.warn("Попытка добавить фильм с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }
        if (description == null) {
            log.warn("Попытка добавить фильм с пустым описанием. Детали: name={}", name);
            throw new ValidationException("Описание не может быть null");
        }
        if (description.length() > 200) {
            log.warn("Попытка добавить фильм с описанием боле 200 символов. Детали: name={}", name);
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (releaseDate == null) {
            log.warn("Попытка добавить фильм с пустой датой релиза. Детали: name={}", name);
            throw new ValidationException("Дата релиза — не может быть пустой");
        }
        if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
            log.warn("Попытка добавить фильм с датой релиза раньше 28 декабря 1895 года. Детали: name={}",
                    name);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (newFilm.getDuration() == null || newFilm.getDuration() <= 0) {
            log.warn("Попытка добавить фильм с отрицательной длительностью фильма. Детали: name={}", name);
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        newFilm.setId(getNextId());
        log.info("Получение нового id фильма при добавлении. Детали: id={}, name={}", newFilm.getId(), name);
        films.put(newFilm.getId(), newFilm);
        log.info("Успешно добавлен фильм: id={}, name={}", newFilm.getId(), name);
        return newFilm;
    }

    private Integer getNextId() {
        int currentMaxId = Math.toIntExact(films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0));
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        Integer id = film.getId();
        String description = film.getDescription();
        String name = film.getName();
        LocalDate releaseDate = film.getReleaseDate();
        Integer duration = film.getDuration();

        if (id == null) {
            log.warn("Не указан id при обновлении фильма");
            throw new ValidationException("Не указан id");
        }
        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(id);
            if (description != null) {
                if (description.length() > 200) {
                    log.warn("Попытка обновить описание длиной большей 200, текущая длина length={}", description.length());
                    throw new ValidationException("Описание не может быть длиннее 200 символов");
                }
                oldFilm.setDescription(description);
                log.info("Описание фильма обновлено на description={}", oldFilm.getDescription());
            }
            if (releaseDate != null) {
                if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
                    log.warn("Попытка изменить дату релиза на ранее чем 28 декабря 1895 года");
                    throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
                }
                oldFilm.setReleaseDate(releaseDate);
                log.info("Дата релиза успешно обновлена на releaseDate={}", oldFilm.getReleaseDate());
            }
            if (duration != null) {
                if (duration <= 0) {
                    log.warn("Указана отрицательная продолжительность при обновлении фильма");
                    throw new ValidationException("Продолжительность должна быть положительным числом");
                }
                oldFilm.setDuration(duration);
                log.info("Продолжительность фильма успешно обновлена на {}", oldFilm.getDuration());
            }
            if (name != null && !name.isBlank()) {
                boolean isNameTaken = films.values().stream().anyMatch(existingFilm ->
                        !existingFilm.getId().equals(oldFilm.getId()) &&
                                existingFilm.getName() != null &&
                                existingFilm.getName().equalsIgnoreCase(name));
                if (isNameTaken) {
                    log.warn("Фильм с указанным именем при обновлении уже существует, name={}", name);
                    throw new ValidationException("Фильм с таким названием уже существует");
                }
                oldFilm.setName(name);
                log.info("Название фильма успешно обновлено на {}", oldFilm.getName());
            }
            return oldFilm;
        }
        log.warn("Фильм с указанным id={} не найден", id);
        throw new ValidationException("Фильм с id = " + id + " не найден");
    }
}
