public class Compartment {
    private Size size;
    private boolean isOccupied;

    public Compartment(Size size) {
        this.size = size;
        this.isOccupied = false;
    }

    public void markFree() {
        isOccupied = false;
    }

    public void markOccupied() {
        isOccupied = true;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public Size getSize() {
        return size;
    }

    public void open() {
        System.out.println("Compartment has opened!");
    }
}
