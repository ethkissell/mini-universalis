package universalis.strategy;

import universalis.Universalis;
import universalis.map.Nation;
import universalis.map.Province;

import java.util.Random;
import universalis.Constants;

public class DefensiveStrategy implements Strategy {
    private final Random random = new Random();

    private static final double CHANCE_OF_INCREASING_DEVELOPMENT = Constants.CHANCE_OF_INCREASING_DEVELOPMENT;
    private static final int AMOUNT_TO_INCREASE_DEVELOPMENT = Constants.AMOUNT_TO_INCREASE_DEVELOPMENT;

    @Override
    public void execute(Nation self, Universalis game) {
        // defensive behavior: development increases
        for (Province province : self.getProvinces()) {
            if (random.nextDouble() < CHANCE_OF_INCREASING_DEVELOPMENT) province.changeDevelopment(AMOUNT_TO_INCREASE_DEVELOPMENT);
        }
    }

    @Override
    public String toString() { return "Defensive Strategy"; }
}
