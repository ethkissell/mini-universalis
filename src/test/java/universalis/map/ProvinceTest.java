package universalis.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import universalis.map.factory.ProvinceFactory;

import static org.junit.jupiter.api.Assertions.*;

public class ProvinceTest {

    @Test
    public void constructionAndBasicOps() {
        Province p = new Province(3);
        assertEquals(3, p.getDevelopment());

        p.changeDevelopment(4);
        assertEquals(7, p.getDevelopment());

        p.changeDevelopment(100);
        assertEquals(25, p.getDevelopment());

        assertNotNull(p.toString());
    }

    @RepeatedTest(5)
    public void createProvinceRandomDevInRange() {
        Province p = ProvinceFactory.createProvince();
        assertNotNull(p);
        int d = p.getDevelopment();
        assertTrue(d >= 0 && d <= 3, "initial development should be between 0 and 3");
    }
}


