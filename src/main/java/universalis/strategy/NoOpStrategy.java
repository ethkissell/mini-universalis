package universalis.strategy;

import universalis.Universalis;
import universalis.map.Nation;

public class NoOpStrategy implements Strategy {
    @Override
    public void execute(Nation self, Universalis game) {
        // do nothing; nation.takeTurn will grow the nation army after strategy executes
    }

    @Override
    public String toString() {
        return "NoOp Strategy";
    }
}