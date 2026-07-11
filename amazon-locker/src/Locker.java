import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Locker {
    private List<Compartment> compartments;
    private Map<String, AccessToken> accessTokensById;
    private Random random;


    public Locker(List<Compartment> compartments) {
        this.compartments = compartments;
        this.random = new Random();
        this.accessTokensById = new HashMap<>();
    }

    public String depositPackage(Size size) {
        Compartment compartment = getAvailableCompartment(size);
        if (compartment == null) {
            throw new RuntimeException("No new compartment available for size = " + size);
        }

        compartment.open();
        compartment.markOccupied();
        AccessToken accessToken = generateAccessToken(compartment);
        accessTokensById.put(accessToken.getAccessTokenId(), accessToken);

        return accessToken.getAccessTokenId();
    }

    public void pickup(String tokenId) {
        if (tokenId == null || tokenId.isEmpty()) {
            throw new RuntimeException("Invalid token Code");
        }

        AccessToken accessToken = accessTokensById.get(tokenId);
        if (accessToken == null) {
            throw new RuntimeException("Invalid token Code");
        }

        if (accessToken.isExpired()) {
            throw new RuntimeException("Access token expired");
        }

        clearDeposit(accessToken);
    }

    public void openExpiredCompartments() {
        for (AccessToken accessToken : accessTokensById.values()) {
            if (accessToken.isExpired()) {
                Compartment compartment = accessToken.getCompartment();
                compartment.open();
            }
        }
    }

    private Compartment getAvailableCompartment(Size size) {
        for (Compartment comp : compartments) {
            if (comp.getSize() == size && !comp.isOccupied()) {
                return comp;
            }
        }

        return null;
    }

    private AccessToken generateAccessToken(Compartment compartment) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiry = Instant.now().plus(7, ChronoUnit.DAYS);
        return new AccessToken(code, compartment, expiry);
    }

    private void clearDeposit(AccessToken accessToken) {
        Compartment compartment = accessToken.getCompartment();
        compartment.markFree();
        accessTokensById.remove(accessToken.getAccessTokenId());
    }
}
