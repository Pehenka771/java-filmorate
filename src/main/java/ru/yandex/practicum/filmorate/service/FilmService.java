package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film add(Film newFilm) {
        validate(newFilm, false);
        return filmStorage.add(newFilm);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Не указан id при обновлении фильма");
            throw new ValidationException("Не указан id");
        }
        Film oldFilm = filmStorage.getById(film.getId());
        if (oldFilm == null) {
            log.warn("Фильм с указанным id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        validate(film, true);
        if (film.getName() != null && !film.getName().isBlank()) {
            boolean isNameTaken = filmStorage.findAll().stream().anyMatch(existingFilm ->
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
        return filmStorage.update(oldFilm);
    }

    public void delete(Integer id) {
        checkFilmId(id);
        filmStorage.delete(id);
    }

    public Film getById(Integer id) {
        checkFilmId(id);
        return filmStorage.getById(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        if (filmStorage.getById(filmId).getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк выбранному фильму");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        if (!filmStorage.getById(filmId).getLikes().contains(userId)) {
            throw new ValidationException("Пользователь не ставил лайк фильму");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count == null) {
            throw new ValidationException("Количество фильмов в топе не может быть пустым");
        }
        if (count <= 0) {
            throw new ValidationException("Количество фильмов в топе не может быть нулевым или отрицательным");
        }
        return filmStorage.getMostPopularFilms(count);
    }

    private void checkFilmId(Integer filmId) {
        if (filmId == null) {
            throw new ValidationException("Указан пустой id");
        }
        if (filmStorage.getById(filmId) == null) {
            throw new NotFoundException("Фильма с указанным id не существует");
        }
    }

    private void checkUserId(Integer userId) {
        if (userId == null) {
            throw new ValidationException("Указан пустой id пользователя");
        }
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователя с указанным id не существует");
        }
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
}
