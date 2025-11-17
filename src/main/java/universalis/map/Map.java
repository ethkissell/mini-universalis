package universalis.map;

import universalis.map.factory.ProvinceFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Map {
    private Province[][] grid;
    private Map() {}
    private static final int MAP_EDGE = 0;
    private static final int MIN_MAP_SIZE = 0;
    private static final int X = 0;
    private static final int Y = 1;

    public int getWidth() { return grid == null ? MIN_MAP_SIZE : grid[X].length; }
    public int getHeight() { return grid == null ? MIN_MAP_SIZE : grid.length; }

    public Province getProvince(int x, int y) {
        checkBounds(x, y);
        return grid[y][x];
    }

    public void setProvince(int x, int y, Province province) {
        checkBounds(x, y);
        grid[y][x] = province;
    }

    public boolean checkBounds(int x, int y) {
        if (grid == null) throw new IllegalStateException("Map not built yet");
        return x < MAP_EDGE || x >= getWidth() || y < MAP_EDGE || y >= getHeight();
    }

    public List<int[]> allCoordinates() {
        List<int[]> coords = new ArrayList<>(getWidth() * getHeight());
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                coords.add(new int[]{col, row});
            }
        }
        return coords;
    }

    public static class Builder {
        private final Random random;
        private final Map map = new Map();

        public Builder(Random random) { this.random = random; }

        public Builder create(int size) {
            if (size <= MIN_MAP_SIZE) throw new IllegalArgumentException("invalid dimensions");
            map.grid = new Province[size][size];
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    map.grid[row][col] = ProvinceFactory.createProvince();
                }
            }
            return this;
        }

        public Builder createNonSquare(int xSize, int ySize) {
            if (xSize <= MIN_MAP_SIZE || ySize <= MIN_MAP_SIZE) throw new IllegalArgumentException("invalid dimensions");
            map.grid = new Province[ySize][xSize];
            for (int row = 0; row < ySize; row++) {
                for (int col = 0; col < xSize; col++) {
                    map.grid[row][col] = ProvinceFactory.createProvince();
                }
            }
            return this;
        }

        /**
         * Place each nation on a random empty tile
         */
        public Builder seedNations(List<Nation> nations) {
            if (map.grid == null) throw new IllegalStateException("call create first");
            List<int[]> coords = map.allCoordinates();
            Collections.shuffle(coords, random);
            if (nations.size() > coords.size()) throw new IllegalArgumentException("too many nations");
            for (int i = 0; i < nations.size(); i++) {
                int[] position = coords.get(i);
                Province province = map.getProvince(position[X], position[Y]);
                province.setOwner(nations.get(i));
                nations.get(i).addProvinceToNationOnSetup(province);
            }
            // set initial armies after provinces assigned
            for (Nation nation : nations) {
                nation.setArmy(nation.getTotalDevelopment());
            }
            return this;
        }

        public Map build() {
            if (map.grid == null) throw new IllegalStateException("map not created");
            return map;
        }
    }
}