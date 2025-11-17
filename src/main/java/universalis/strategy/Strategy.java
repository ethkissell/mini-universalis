package universalis.strategy;

import universalis.Universalis;
import universalis.map.Nation;

public interface Strategy {
    void execute(Nation self, Universalis game);
}
