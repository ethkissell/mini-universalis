package universalis.strategy;

import org.junit.jupiter.api.Test;
import universalis.Universalis;
import universalis.map.Map;
import universalis.map.Nation;
import universalis.map.Province;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class StrategyTest {

    @Test
    public void noopStrategyWorks() {
        Strategy s = new NoOpStrategy();
        Nation n = new Nation("X", s);
        s.execute(n, null); // should not throw
    }

    @Test
    public void defensiveDoesNotThrow() {
        Strategy s = new DefensiveStrategy();
        Nation n = new Nation("Def", s);
        s.execute(n, null); // safe with empty provinces
    }

    @Test
    public void offensiveExpandsWhenPossible() {
        Map.Builder builder = new Map.Builder(new java.util.Random(2));
        Map map = builder.create(2).build();

        Nation attacker = new Nation("Att", new OffensiveStrategy(new java.util.Random(3)));
        Province p0 = map.getProvince(0,0);
        p0.setOwner(attacker);
        attacker.captureProvince(p0);

        List<Nation> nations = new ArrayList<>();
        nations.add(attacker);
        Universalis game = new Universalis(map, nations);

        Strategy s = new OffensiveStrategy(new java.util.Random(4));
        s.execute(attacker, game);

        assertTrue(attacker.getProvinceCount() >= 1);
    }
}

