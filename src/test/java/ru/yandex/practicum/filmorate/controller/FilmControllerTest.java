package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

	@Autowired
	private FilmController filmController;
	@Autowired
	private FilmStorage filmStorage;
	@Autowired
	private UserController userController;
	@Autowired
	private UserStorage userStorage;

	@BeforeEach
	void setUp() {
		filmStorage.clear();
		userStorage.clear();
		filmCounter = 0;
		userCounter = 0;
	}

	@Test
	void addValidFilm() {
		Film film = validFilm();
		Film added = filmController.add(film);

		assertNotNull(added.getId());
		assertEquals(film.getName(), added.getName());
		assertEquals(film.getDescription(), added.getDescription());
		assertEquals(film.getReleaseDate(), added.getReleaseDate());
		assertEquals(film.getDuration(), added.getDuration());
	}

	@Test
	void addFilmWithNullNameShouldThrow() {
		Film film = validFilm();
		film.setName(null);
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithBlankNameShouldThrow() {
		Film film = validFilm();
		film.setName("   ");
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithNullDescriptionShouldThrow() {
		Film film = validFilm();
		film.setDescription(null);
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithDescriptionLength200ShouldBeOk() {
		Film film = validFilm();
		film.setDescription("a".repeat(200));
		assertDoesNotThrow(() -> filmController.add(film));
	}

	@Test
	void addFilmWithDescriptionLength201ShouldThrow() {
		Film film = validFilm();
		film.setDescription("a".repeat(201));
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithNullReleaseDateShouldThrow() {
		Film film = validFilm();
		film.setReleaseDate(null);
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithReleaseDateBeforeMinShouldThrow() {
		Film film = validFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithMinReleaseDateShouldBeOk() {
		Film film = validFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		assertDoesNotThrow(() -> filmController.add(film));
	}

	@Test
	void addFilmWithZeroDurationShouldThrow() {
		Film film = validFilm();
		film.setDuration(0);
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithNegativeDurationShouldThrow() {
		Film film = validFilm();
		film.setDuration(-1);
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addFilmWithNullDurationShouldThrowValidationException() {
		Film film = validFilm();
		film.setDuration(null);
		assertThrows(ValidationException.class, () -> filmController.add(film));
	}

	@Test
	void addEmptyFilmShouldThrow() {
		assertThrows(ValidationException.class, () -> filmController.add(new Film()));
	}

	@Test
	void updateFilmWithNullIdShouldThrow() {
		assertThrows(ValidationException.class, () -> filmController.update(new Film()));
	}

	@Test
	void updateFilmNotFoundShouldThrow() {
		Film film = validFilm();
		film.setId(999);
		assertThrows(NotFoundException.class, () -> filmController.update(film));
	}

	@Test
	void updateFilmWithDescriptionLength201ShouldThrow() {
		Film film = filmController.add(validFilm());
		film.setDescription("a".repeat(201));
		assertThrows(ValidationException.class, () -> filmController.update(film));
	}

	@Test
	void updateFilmWithReleaseDateBeforeMinShouldThrow() {
		Film film = filmController.add(validFilm());
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		assertThrows(ValidationException.class, () -> filmController.update(film));
	}

	@Test
	void updateFilmWithZeroDurationShouldThrow() {
		Film film = filmController.add(validFilm());
		film.setDuration(0);
		assertThrows(ValidationException.class, () -> filmController.update(film));
	}

	@Test
	void updateFilmWithDuplicateNameShouldThrow() {
		Film film1 = filmController.add(validFilm());
		Film film2 = filmController.add(validFilm());
		film2.setName(film1.getName());
		assertThrows(ValidationException.class, () -> filmController.update(film2));
	}

	@Test
	void updateFilmValid() {
		Film film = filmController.add(validFilm());
		film.setName("Updated");
		film.setDescription("Updated description");
		film.setReleaseDate(LocalDate.of(2010, 5, 5));
		film.setDuration(150);

		Film updated = filmController.update(film);

		assertEquals(film.getId(), updated.getId());
		assertEquals("Updated", updated.getName());
		assertEquals("Updated description", updated.getDescription());
		assertEquals(LocalDate.of(2010, 5, 5), updated.getReleaseDate());
		assertEquals(150, updated.getDuration());
	}

	@Test
	void getByIdExistingFilm() {
		Film film = filmController.add(validFilm());
		Film found = filmController.getById(film.getId());
		assertEquals(film.getId(), found.getId());
		assertEquals(film.getName(), found.getName());
	}

	@Test
	void getByIdNonExistingFilm() {
		assertThrows(NotFoundException.class, () -> filmController.getById(999));
	}

	@Test
	void deleteExistingFilm() {
		Film film = filmController.add(validFilm());
		filmController.delete(film.getId());
		assertThrows(NotFoundException.class, () -> filmController.getById(film.getId()));
	}

	@Test
	void deleteNonExistingFilm() {
		assertThrows(NotFoundException.class, () -> filmController.delete(999));
	}

	@Test
	void addLikeSuccessfully() {
		Film film = filmController.add(validFilm());
		User user = userController.add(validUser());

		filmController.addLike(film.getId(), user.getId());

		assertDoesNotThrow(() -> filmController.removeLike(film.getId(), user.getId()));
	}

	@Test
	void addLikeTwiceShouldThrow() {
		Film film = filmController.add(validFilm());
		User user = userController.add(validUser());

		filmController.addLike(film.getId(), user.getId());
		assertThrows(ValidationException.class, () -> filmController.addLike(film.getId(), user.getId()));
	}

	@Test
	void removeLikeFromNonExistingLike() {
		Film film = filmController.add(validFilm());
		User user = userController.add(validUser());

		assertThrows(ValidationException.class, () -> filmController.removeLike(film.getId(), user.getId()));
	}

	@Test
	void addLikeToNonExistingFilm() {
		User user = userController.add(validUser());
		assertThrows(NotFoundException.class, () -> filmController.addLike(999, user.getId()));
	}

	@Test
	void addLikeWithNonExistingUser() {
		Film film = filmController.add(validFilm());
		assertThrows(NotFoundException.class, () -> filmController.addLike(film.getId(), 999));
	}

	@Test
	void getMostPopularFilmsReturnsSortedByLikes() {
		Film film1 = filmController.add(validFilm());
		film1.setName("Popular One");

		Film film2 = filmController.add(validFilm());
		film2.setName("Less Popular");

		User u1 = userController.add(validUser());
		User u2 = userController.add(validUser());
		User u3 = userController.add(validUser());

		// Ставим 2 лайка первому фильму
		filmController.addLike(film1.getId(), u1.getId());
		filmController.addLike(film1.getId(), u2.getId());

		// Ставим 1 лайк второму
		filmController.addLike(film2.getId(), u3.getId());

		List<Film> top = filmController.getMostPopularFilms(2);

		assertEquals(2, top.size());
		assertEquals("Popular One", top.get(0).getName()); // Больше лайков → выше в списке
		assertEquals("Less Popular", top.get(1).getName());
	}

	@Test
	void getMostPopularFilmsWithInvalidCountShouldThrow() {
		assertThrows(ValidationException.class, () -> filmController.getMostPopularFilms(null));
		assertThrows(ValidationException.class, () -> filmController.getMostPopularFilms(0));
		assertThrows(ValidationException.class, () -> filmController.getMostPopularFilms(-5));
	}

	private static int filmCounter = 0;
	private static int userCounter = 0;

	private Film validFilm() {
		String suffix = String.valueOf(filmCounter++);
		Film film = new Film();
		film.setName("Film" + suffix);
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(100);
		return film;
	}

	private User validUser() {
		String suffix = String.valueOf(userCounter++);
		User user = new User();
		user.setLogin("login" + suffix);
		user.setEmail("user" + suffix + "@mail.ru");
		user.setName("name" + suffix);
		user.setBirthday(LocalDate.of(1990, 1, 1));
		return user;
	}
}