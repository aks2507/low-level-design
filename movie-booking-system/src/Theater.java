import java.util.ArrayList;
import java.util.List;

public class Theater {
    private final String theaterId;
    private final String name;
    private final List<Show> shows;

    public Theater(String theaterId, String name) {
        this.theaterId = theaterId;
        this.name = name;
        this.shows = new ArrayList<>();
    }

    public String getTheaterId() {
        return theaterId;
    }

    public String getName() {
        return name;
    }

    public List<Show> getShows() {
        return shows;
    }

    public List<Show> getShowsForMovie(Movie movie) {
        List<Show> res = new ArrayList<>();
        for (Show show : shows) {
            if (show.getMovie().getMovieId().equals(movie.getMovieId())) {
                res.add(show);
            }
        }

        return res;
    }
}
