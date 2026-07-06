import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Show {
    private final String showId;
    private final String screenLabel;
    private final Theater theater;
    private final Movie movie;
    private final LocalDateTime startDateAndTime;
    private final List<Reservation> reservations;

    public Show(String showId, String screenLabel, Theater theater, Movie movie, LocalDateTime startDateAndTime) {
        this.showId = showId;
        this.screenLabel = screenLabel;
        this.theater = theater;
        this.movie = movie;
        this.startDateAndTime = startDateAndTime;
        this.reservations = new ArrayList<>();
    }

    public String getShowId() {
        return showId;
    }

    public Theater getTheater() {
        return theater;
    }

    public LocalDateTime getStartDateAndTime() {
        return startDateAndTime;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getScreenLabel() {
        return screenLabel;
    }

    public boolean isAvailable(String seatId) {
        for (Reservation reservation : reservations) {
            if (reservation.getSeatIds().contains(seatId)) {
                return false;
            }
        }

        return true;
    }

    public List<String> getAvailableSeats() {
        Set<String> bookedSeats = new HashSet<>();

        for (Reservation reservation : reservations) {
            bookedSeats.addAll(reservation.getSeatIds());
        }

        List<String> availableSeats = new ArrayList<>();
        for (char row = 'A'; row <= 'Z'; row++) {
            for (int num = 1; num <= 20; num++) {
                String seatId = "" + row + num;
                if (!bookedSeats.contains(seatId)) {
                    availableSeats.add(seatId);
                }
            }
        }

        return availableSeats;
    }

    public void book(Reservation reservation) {
        synchronized (this) {
            List<String> seatIds = reservation.getSeatIds();

            // This is already checked in main class but adding it just for sanity
            if (seatIds == null || seatIds.isEmpty()) {
                throw new IllegalArgumentException("At least one seat has to be selected");
            }

            for (String seatId : seatIds) {
                if (!isValidSeat(seatId)) {
                    throw new IllegalArgumentException("Invalid seat selected: " + seatId);
                } else if (!isAvailable(seatId)) {
                    throw new IllegalArgumentException("Seat: " + seatId + " is not available for booking!");
                }
            }

            reservations.add(reservation);
        }
    }

    public void cancel(Reservation reservation) {
        synchronized (this) {
            reservations.remove(reservation);
        }
    }

    private boolean isValidSeat(String seatId) {
        if (seatId == null || seatId.length() < 2) {
            return false;
        }

        char row = seatId.charAt(0);
        try {
            int num = Integer.parseInt(seatId.substring(1));
            return row >= 'A' && row <= 'Z' && num >= 1 && num <= 20;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
