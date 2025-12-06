package universalis.map;

import java.util.Random;

public class Province {
    private static final Random random = new Random();

    private static final int MIN_DEV = 0;
    private static final int MAX_DEV = 25;
    private static final int MAX_STARTING_DEV = 3;

    private int development;
    private Nation owner;

    public Province() {
        this.development = MIN_DEV + random.nextInt(MAX_STARTING_DEV);
        this.owner = null;
    }

    public Province(int development) {
        this.development = development;
        this.owner = null;
    }

    public int getDevelopment() { return development; }
    public Nation getOwner() { return owner; }

    public void setOwner(Nation owner) { this.owner = owner; }

    public void changeDevelopment(int value) {
        if (this.development + value < MIN_DEV) { // if development dips below minimum reset
            this.development = MIN_DEV;
        } else {
            this.development = Math.min(MAX_DEV, this.development + value); // max or increase
        }
    }

    @Override
    public String toString() {
        String ownerName = owner == null ? "." : owner.getName();
        return String.format("%d:%s", development, ownerName);
    }
}
