import constants.DiscColor;

public class Player {
    String name;
    DiscColor discColor;

    public Player(String name, DiscColor discColor) {
        this.name = name;
        this.discColor = discColor;
    }

    private DiscColor getDiscColor() {
        return this.discColor;
    }

    private String getName() {
        return this.name;
    }


}
