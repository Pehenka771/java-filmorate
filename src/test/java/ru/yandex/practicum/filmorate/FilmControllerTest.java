package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

	private FilmController filmController;

	@BeforeEach
	void setUp() {
		filmController = new FilmController();
	}

	@Test
	void addValidFilm() {
		Film film = validFilm();

		Film added = filmController.add(film);

		assertNotNull(added.getId());
		assertEquals("Film", added.getName());
		assertEquals("Description", added.getDescription());
		assertEquals(LocalDate.of(2000, 1, 1), added.getReleaseDate());
		assertEquals(100, added.getDuration());
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
		assertThrows(ValidationException.class, () -> filmController.update(film));
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

	private Film validFilm() {
		Film film = new Film();
		film.setName("Film");
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(100);
		return film;
	}
}