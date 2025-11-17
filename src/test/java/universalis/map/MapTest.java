package universalis.map;

import org.junit.jupiter.api.Test;
import universalis.strategy.NoOpStrategy;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MapTest {

    @Test
    public void builderCreateAndSeed() {
        Map.Builder builder = new Map.Builder(new java.util.Random(42));
        Map map = builder.create(4).build();

        assertEquals(4, map.getWidth());
        assertEquals(4, map.getHeight());

        List<Nation> nations = new ArrayList<>();
        nations.add(new Nation("TestA", new NoOpStrategy()));
        nations.add(new Nation("TestB", new NoOpStrategy()));

        Map.Builder builder2 = new Map.Builder(new java.util.Random(42));
        Map map2 = builder2.create(6).seedNations(nations).build();

        assertTrue(nations.get(0).getProvinceCount() >= 1);
        assertTrue(nations.get(1).getProvinceCount() >= 1);

        for (int y = 0; y < map2.getHeight(); y++) {
            for (int x = 0; x < map2.getWidth(); x++) {
                assertNotNull(map2.getProvince(x, y));
            }
        }
    }
}

