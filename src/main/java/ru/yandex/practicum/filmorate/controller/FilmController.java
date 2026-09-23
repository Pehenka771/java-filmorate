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
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film add(@RequestBody Film newFilm) {
        validate(newFilm, false);
        newFilm.setId(getNextId());
        log.info("Получение нового id фильма при добавлении. Детали: id={}, name={}", newFilm.getId(), newFilm.getName());
        films.put(newFilm.getId(), newFilm);
        log.info("Успешно добавлен фильм: id={}, name={}", newFilm.getId(), newFilm.getName());
        return newFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {

        if (film.getId() == null) {
            log.warn("Не указан id при обновлении фильма");
            throw new ValidationException("Не указан id");
        }
        Film oldFilm = films.get(film.getId());
        if (oldFilm == null) {
            log.warn("Фильм с указанным id={} не найден", film.getId());
            throw new ValidationException("Фильм с id = " + film.getId() + " не найден");
        }
        validate(film, true);
        if (film.getName() != null && !film.getName().isBlank()) {
            boolean isNameTaken = films.values().stream().anyMatch(existingFilm ->
                    !existingFilm.getId().equals(oldFilm.getId()) &&
                            existingFilm.getName() != null &&
                            existingFilm.getName().equalsIgnoreCase(film.getName()));
            if (isNameTaken) {
                log.warn("Фильм с указанным именем при обновлении уже существует, name={}", film.getName());
                throw new ValidationException("Фильм с таким названием уже существует");
            }
            oldFilm.setName(film.getName());
            log.info("Название фильма успешно обновлено на {}", oldFilm.getName());
        }
        if (film.getDescription() != null) {
            oldFilm.setDescription(film.getDescription());
            log.info("Описание фильма обновлено на description={}", oldFilm.getDescription());
        }
        if (film.getReleaseDate() != null) {
            oldFilm.setReleaseDate(film.getReleaseDate());
            log.info("Дата релиза успешно обновлена на releaseDate={}", oldFilm.getReleaseDate());
        }
        if (film.getDuration() != null) {
            oldFilm.setDuration(film.getDuration());
            log.info("Продолжительность фильма успешно обновлена на {}", oldFilm.getDuration());
        }
        return oldFilm;
    }

    private void validate(Film data, boolean isUpdate) {
        String name = data.getName();
        if (name == null) {
            if (!isUpdate) {
                log.warn("Попытка добавить фильм с пустым названием");
                throw new ValidationException("Название не может быть пустым");
            }
        } else if (name.isBlank()) {
            log.warn("Попытка указать фильм с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }

        String description = data.getDescription();
        if (description == null) {
            if (!isUpdate) {
                log.warn("Попытка добавить фильм с пустым описанием. Детали: name={}", name);
                throw new ValidationException("Описание не может быть null");
            }
        } else if (description.length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Попытка указать описание длиннее {} символов. Детали: name={}",
                    MAX_DESCRIPTION_LENGTH, name);
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        LocalDate releaseDate = data.getReleaseDate();
        if (releaseDate == null) {
            if (!isUpdate) {
                log.warn("Попытка добавить фильм с пустой датой релиза. Детали: name={}", name);
                throw new ValidationException("Дата релиза — не может быть пустой");
            }
        } else if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
            log.warn("Дата релиза раньше 28 декабря 1895 года. Детали: name={}", name);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        Integer duration = data.getDuration();
        if (duration == null) {
            if (!isUpdate) {
                log.warn("Попытка добавить фильм с пустой продолжительностью. Детали: name={}", name);
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
        } else if (duration <= 0) {
            log.warn("Продолжительность должна быть положительным числом. Детали: name={}", name);
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private Integer getNextId() {
        int currentMaxId = Math.toIntExact(films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0));
        return ++currentMaxId;
    }
}
