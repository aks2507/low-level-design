import java.time.LocalDateTime;
import java.util.*;

public class MovieBookingSystem {
    private final List<Theater> theaters;
    // Indices for O(1) lookup
    private final Map<String, Movie> moviesById;
    private final Map<String, List<Show>> showsByMovieId;
    private final Map<String, Show> showsById;
    private final Map<String, Reservation> reservationsById;

    public MovieBookingSystem(List<Theater> theaters) {
        this.theaters = theaters;
        this.moviesById = new HashMap<>();
        this.showsByMovieId = new HashMap<>();
        this.showsById = new HashMap<>();
        this.reservationsById = new HashMap<>();

        for (Theater theater : theaters) {
            for (Show show : theater.getShows()) {
                Movie movie = show.getMovie();
                moviesById.put(movie.getMovieId(), movie);
                showsById.put(show.getShowId(), show);
                showsByMovieId
                        .computeIfAbsent(movie.getMovieId(), k -> new ArrayList<>())
                        .add(show);
            }
        }
    }

    public List<Show> searchMovies(String movieName) {
        if (movieName == null || movieName.isEmpty()) {
            return new ArrayList<>();
        }
        List<Show> res = new ArrayList<>();
        String toSearch = movieName.toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        for (Movie movie : moviesById.values()) {
            if (movie.getTitle().toLowerCase().contains(toSearch)) {
                List<Show> movieShows = showsByMovieId.get(movie.getMovieId());
                if (movieShows != null) {
                    for (Show show : movieShows) {
                        if (show.getStartDateAndTime().isAfter(now)) {
                            res.add(show);
                        }
                    }
                }
            }
        }

        return res;
    }

    public List<Show> getShowsAtTheater(Theater theater) {
        if (theater == null) {
            return new ArrayList<>();
        }

        List<Show> showsAtTheater = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Show show : theater.getShows()) {
            if (show.getStartDateAndTime().isAfter(now)) {
                showsAtTheater.add(show);
            }
        }

        return showsAtTheater;
    }

    public Reservation bookSeats(String showId, List<String> seatIds) {
        if (showId == null || showId.isEmpty() || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid Booking request");
        }

        Show show = showsById.get(showId);
        if (show == null) {
            throw new NoSuchElementException("Show is not found: " + showId);
        }

        Reservation reservation = new Reservation(UUID.randomUUID().toString(), seatIds, show);

        show.book(reservation);

        return reservation;
    }

    public void cancelReservation(String confirmationId) {
        if (confirmationId == null || confirmationId.isEmpty()) {
            throw new IllegalArgumentException("No such confirmation ID: " + confirmationId);
        }

        Reservation reservation = reservationsById.get(confirmationId);

        if (reservation == null) {
            throw new NoSuchElementException("Reservation not found: " + confirmationId);
        }

        Show show = reservation.getShow();
        show.cancel(reservation);
        reservationsById.remove(confirmationId);
    }
}





