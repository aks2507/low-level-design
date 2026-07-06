import java.util.ArrayList;
import java.util.List;

public class Reservation {
    private final String confirmationId;
    private final List<String> seatIds;
    private final Show show;

    public Reservation(String confirmationId, List<String> seatIds, Show show) {
        this.confirmationId = confirmationId;
        this.seatIds = seatIds;
        this.show = show;
    }

    public String getConfirmationId() {
        return confirmationId;
    }

    public Show getShow() {
        return show;
    }

    public List<String> getSeatIds() {
        return new ArrayList<>(seatIds);
    }
}
