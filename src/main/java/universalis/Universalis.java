package universalis;

import universalis.map.Map;
import universalis.map.Nation;
import universalis.map.factory.NationFactory;
import universalis.map.Province;
import universalis.strategy.DefensiveStrategy;
import universalis.strategy.NoOpStrategy;
import universalis.strategy.OffensiveStrategy;

import java.util.*;

public class Universalis {
    public static final int[][] DIRECTIONS = {{1,0},{-1,0},{0,1},{0,-1}};
    private static final int MAX_TURNS = 250;
    private static final int INCREASE_DEVELOPMENT_VALUE = 1;
    private static final int MAX_NAME_LENGTH = 10;
    private static final int DEVELOPMENT_PROVINCE_FACTOR = 2;

    private final Map map;
    private final List<Nation> nations = new ArrayList<>();
    private final Random rng;

    public Universalis(Map map, List<Nation> nations) {
        this(map, nations, new Random());
    }

    public Universalis(Map map, List<Nation> nations, Random rng) {
        if (map == null) throw new IllegalArgumentException("map required");
        this.map = map;
        if (nations != null) this.nations.addAll(nations);
        this.rng = rng == null ? new Random() : rng;
    }

    public Map getMap() { return map; }
    public List<Nation> getNations() { return Collections.unmodifiableList(nations); }

    // run a fixed number of turns
    public void runTurns(int turns) {
        for (int turn = 1; turn <= turns && nations.size() > 1; turn++) {
            for (Nation nation : new ArrayList<>(nations)) nation.takeTurn(this);
            nations.removeIf(nation -> nation.getProvinceCount() == 0);
            distributeDevelopmentPoints();
        }
    }

    // play until only one nation remains or stalemate detected
    public void playToCompletion() {
        int turn = 0;
        int idleTurns = 0;
        int lastOwned = totalOwnedProvinces();
        while (nations.size() > 1) {

            // print map state probably not necessary when UI is implemented
            System.out.println(this);

            turn++;
            for (Nation nation : new ArrayList<>(nations)) nation.takeTurn(this);
            nations.removeIf(nation -> nation.getProvinceCount() == 0);
            distributeDevelopmentPoints();

            int currentOwnedProvinces = totalOwnedProvinces();
            if (currentOwnedProvinces == lastOwned) idleTurns++; else idleTurns = 0;
            lastOwned = currentOwnedProvinces;
            if (MAX_TURNS > 0 && idleTurns >= MAX_TURNS) {
                System.out.println("Stalemate detected after " + idleTurns + " idle turns. Aborting.");
                break;
            }
        }

        System.out.println("Took " + turn + " turns.");
        String winner = nations.isEmpty() ? "None" : nations.getFirst().getName();
        System.out.println("Finished. Winner: " + winner);
    }

    private int totalOwnedProvinces() {
        int sum = 0;
        for (Nation nation : new ArrayList<>(nations)) sum += nation.getProvinceCount();
        return sum;
    }

    /**
     * nations get half of their total provinces in development to distribute randomly
     */
    private void distributeDevelopmentPoints() {
        for (Nation nation : new ArrayList<>(nations)) {
            int points = Math.max(0, nation.getProvinceCount() / DEVELOPMENT_PROVINCE_FACTOR);
            for (int i = 0; i < points; i++) {
                List<Province> list = nation.getProvinces();
                if (list.isEmpty()) break;
                Province province = list.get(rng.nextInt(list.size()));
                province.changeDevelopment(INCREASE_DEVELOPMENT_VALUE);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int width = map.getWidth();
        int height = map.getHeight();

        sb.append("\n=== Universalis Map snapshot ===\n");
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Province province = map.getProvince(col, row);
                String ownerName = province.getOwner() == null ? "." : province.getOwner().getName();
                String ownerShort = ownerName.length() > MAX_NAME_LENGTH ? ownerName.substring(0, MAX_NAME_LENGTH) : ownerName;
                sb.append(String.format("%2d:%-10s ", province.getDevelopment(), ownerShort));
            }
            sb.append("\n");
        }

        sb.append("\n=== Nations ===\n");
        for (Nation nation : nations) {
            int count = nation.getProvinceCount();
            int totalDev = nation.getTotalDevelopment();
            sb.append(String.format("%s - provinces=%d, totalDev=%d, army=%d\n", nation.getName(), count, totalDev, nation.getArmy()));
        }
        sb.append("===============================");
        return sb.toString();
    }

    /**
     * Helper to create a ready-to-run Universalis instance.
     */
    public static Universalis setupDefaultGame(int size, int numNations) {
        if (size <= 0) throw new IllegalArgumentException("invalid size");
        if (numNations <= 0) throw new IllegalArgumentException("numNations must be > 0");
        if (numNations > size * size) throw new IllegalArgumentException("too many nations for map size");

        List<Nation> nations = new ArrayList<>(numNations);
        Random rng = new Random();

        for (int i = 0; i < numNations; i++) {
            Nation nation = NationFactory.createRandomNation();
            int pick = rng.nextInt(3);
            switch (pick) {
                case 0: nation.setStrategy(new NoOpStrategy()); break;
                case 1: nation.setStrategy(new OffensiveStrategy(rng)); break;
                default: nation.setStrategy(new DefensiveStrategy()); break;
            }
            nations.add(nation);
        }

        Map.Builder builder = new Map.Builder(rng);
        Map map = builder.create(size).seedNations(nations).build();
        return new Universalis(map, nations, rng);
    }
}