package universalis.map;

import org.junit.jupiter.api.Test;
import universalis.Universalis;
import universalis.map.factory.NationFactory;
import universalis.strategy.NoOpStrategy;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class NationTest {

    @Test
    public void addAndRemoveProvinces() {
        Nation n = new Nation("N1", new NoOpStrategy());
        Province p = new Province(2);
        assertEquals(0, n.getProvinceCount());
        n.captureProvince(p);
        assertEquals(1, n.getProvinceCount());
        n.removeProvince(p);
        assertEquals(0, n.getProvinceCount());
    }

    @Test
    public void takeTurnStrategySwitching() {
        Map.Builder builder = new Map.Builder(new java.util.Random(5));
        Map map = builder.create(3).build();

        Nation n = new Nation("Switch", new NoOpStrategy());
        Province center = map.getProvince(1,1);
        center.setOwner(n);
        n.captureProvince(center);

        List<Nation> nations = new ArrayList<>();
        nations.add(n);
        Universalis game = new Universalis(map, nations);

        n.takeTurn(game); // should not throw an exception
    }

    @Test
    public void createUniqueNamesUntilExhaustedThenThrows() {
        NationFactory.clearUsedNamesForTests();

        int capacity = 25 * 25;
        for (int i = 0; i < capacity; i++) {
            Nation n = NationFactory.createRandomNation();
            assertNotNull(n.getName());
            assertFalse(n.getName().isEmpty());
        }
        assertThrows(IllegalStateException.class, NationFactory::createRandomNation);
    }
}


