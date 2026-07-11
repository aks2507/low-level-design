import java.time.Instant;

public class AccessToken {
    private String accessTokenId;
    private Compartment compartment;
    private Instant expiry;

    public AccessToken(String accessTokenId, Compartment compartment, Instant expiry) {
        this.accessTokenId = accessTokenId;
        this.compartment = compartment;
        this.expiry = expiry;
    }


    public String getAccessTokenId() {
        return accessTokenId;
    }

    public Compartment getCompartment() {
        return compartment;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiry);
    }
}
