package universalis.strategy;

import universalis.Universalis;
import universalis.map.Nation;

import java.util.Random;

public class OffensiveStrategy implements Strategy {
    private static Random random = new Random();
    public OffensiveStrategy(Random random) { OffensiveStrategy.random = random; }
    public OffensiveStrategy() { random = new Random(); }

    @Override
    public void execute(Nation self, Universalis game) {
        self.expandOrAttack(game, random);
    }

    @Override
    public String toString() {
        return "Offensive Strategy";
    }
}