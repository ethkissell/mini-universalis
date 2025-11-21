package universalis.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import universalis.Constants;
import universalis.Universalis;
import universalis.strategy.*;
import universalis.strategy.Strategy;

public class Nation {
    private final String name;
    private Strategy strategy;
    private final List<Province> provinces = new ArrayList<>();
    private static final int X = 0;
    private static final int Y = 1;
    private static final int TARGET_INDEX = 1;

    private static final int MINIMUM_ARMY_SIZE = Constants.MINIMUM_ARMY_SIZE;
    private static final int ARMY_GROWTH_MODIFIER = Constants.ARMY_GROWTH_MODIFIER;
    private static final int ARMY_CAP_MODIFIER = Constants.ARMY_CAP_MODIFIER;
    private static final int ARMY_LOSS_FACTOR = Constants.ARMY_LOSS_FACTOR;
    private static final int CAPTURE_DEVELOPMENT_PENALTY = Constants.CAPTURE_DEVELOPMENT_PENALTY;

    private int army;

    public Nation(String name, Strategy strategy) {
        this.name = name;
        this.strategy = strategy;
    }

    public String getName() { return name; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }

    public List<Province> getProvinces() { return provinces; }
    public int getProvinceCount() { return provinces.size(); }

    public int getArmy() { return army; }
    public void setArmy(int newArmy) { this.army = Math.max(MINIMUM_ARMY_SIZE, newArmy); }

    public void captureProvince(Province province) {
        if (province != null && !provinces.contains(province)) {
            province.changeDevelopment(CAPTURE_DEVELOPMENT_PENALTY);
            provinces.add(province);
        }
    }

    public void addProvinceToNationOnSetup(Province province) {
        if (province != null && !provinces.contains(province)) {
            provinces.add(province);
        }
    }

    public void removeProvince(Province province) {
        provinces.remove(province);
    }

    public int getTotalDevelopment() {
        int sum = 0;
        for (Province province : provinces) sum += province.getDevelopment();
        return sum;
    }

    public int armyCap() {
        return Math.max(MINIMUM_ARMY_SIZE, ARMY_CAP_MODIFIER * getTotalDevelopment());
    }

    public void growArmy() {
        int growth = getTotalDevelopment();
        if (growth <= 0) return;
        int cap = armyCap();
        int newArmy = army + (growth * ARMY_GROWTH_MODIFIER);
        army = Math.min(newArmy, cap);
    }

    /**
     * Called each turn: strategy acts, then grow army.
     */
    public void takeTurn(Universalis game) {
        int totalDev = getTotalDevelopment();
        int cap = armyCap();
        boolean lowArmy = cap == 0 || ((double) army < 0.2 * cap);

        // low army or no development -> defensive strategy
        if (lowArmy || totalDev == 0) {
            setStrategy(new DefensiveStrategy());
        }

        // not low army -> expand into empty provinces
        List<int[]> emptyFrontier = collectEmptyFrontier(game);
        if (!emptyFrontier.isEmpty()) {
            setStrategy(new OffensiveStrategy());
        }

        // no empty frontier -> opportunistic check for weaker neighboring nations
        Nation weakerNeighbor = findAdjacentWeakerNation(game);
        if (weakerNeighbor != null) {
            setStrategy(new OffensiveStrategy());
        }

        if (strategy != null) strategy.execute(this, game);
        growArmy();
    }

    /**
     * Collect a list of empty frontier coordinates adjacent to ANY province owned by this nation.
     */
    private List<int[]> collectEmptyFrontier(Universalis game) {
        Map map = game.getMap();
        List<int[]> frontier = new ArrayList<>();
        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                Province province = map.getProvince(col, row);
                if (province.getOwner() == this) {
                    for (int[] directions : Universalis.DIRECTIONS) {
                        int neighborX = col + directions[X];
                        int neighborY = row + directions[Y];
                        if (map.checkBounds(neighborX, neighborY)) continue;
                        Province neighbor = map.getProvince(neighborX, neighborY);
                        if (neighbor.getOwner() == null) {
                            boolean duplicate = false;
                            for (int[] check : frontier) if (check[X] == neighborX && check[Y] == neighborY) { duplicate = true; break; }
                            if (!duplicate) frontier.add(new int[]{neighborX, neighborY});
                        }
                    }
                }
            }
        }
        return frontier;
    }

    /**
     * Find any adjacent enemy nation whose army is less than this nation's army.
     * Returns the first found weaker neighbor or null if none.
     */
    private Nation findAdjacentWeakerNation(Universalis game) {
        Map map = game.getMap();
        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                Province province = map.getProvince(col, row);
                if (province.getOwner() == this) {
                    for (int[] directions : Universalis.DIRECTIONS) {
                        int neighborX = col + directions[X];
                        int neighborY = row + directions[Y];
                        if (map.checkBounds(neighborX, neighborY)) continue;
                        Province neighbor = map.getProvince(neighborX, neighborY);
                        Nation defender = neighbor.getOwner();
                        if (defender != null && defender != this) {
                            if (this.army > defender.getArmy()) return defender;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Expand into empty neighboring province if available.
     * If none available, pick a capture candidate and resolve battle using nation armies:
     * - if attackerArmy > defenderArmy: attacker wins, transfer ownership, halve both armies
     * - if attackerArmy < defenderArmy: defender holds, both armies halved
     * - if tied: both armies halved, no transfer
     * Only one action is performed per invocation.
     */
    public void expandOrAttack(Universalis game, Random rng) {
        Map map = game.getMap();
        List<int[]> emptyFrontiers = new ArrayList<>();
        List<int[][]> captureCandidates = new ArrayList<>();

        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                Province province = map.getProvince(col, row);
                if (province.getOwner() == this) {
                    for (int[] directions : Universalis.DIRECTIONS) {
                        int neighborX = col + directions[X];
                        int neighborY = row + directions[Y];
                        if (map.checkBounds(neighborX, neighborY)) continue;
                        Province neighbor = map.getProvince(neighborX, neighborY);
                        if (neighbor.getOwner() == null) {
                            emptyFrontiers.add(new int[]{neighborX, neighborY});
                        } else if (neighbor.getOwner() != this) {
                            captureCandidates.add(new int[][]{{col, row}, {neighborX, neighborY}});
                        }
                    }
                }
            }
        }

        // Prefer expansion
        if (!emptyFrontiers.isEmpty()) {
            int[] pick = emptyFrontiers.get(rng.nextInt(emptyFrontiers.size()));
            Province neighborProvince = map.getProvince(pick[X], pick[Y]);
            neighborProvince.setOwner(this);
            captureProvince(neighborProvince);
            return;
        }

        // Attempt capture
        if (!captureCandidates.isEmpty()) {
            int idx = rng.nextInt(captureCandidates.size());
            int[][] chosen = captureCandidates.get(idx);
            int targetX = chosen[TARGET_INDEX][X];
            int targetY = chosen[TARGET_INDEX][Y];
            Province target = map.getProvince(targetX, targetY);
            Nation defender = target.getOwner();
            if (defender == null) {
                // fallback to claiming if owner null
                target.setOwner(this);
                captureProvince(target);
                return;
            }

            int attackerArmy = this.getArmy();
            int defenderArmy = defender.getArmy();

            if (attackerArmy > defenderArmy) { // attacker wins
                target.setOwner(this);
                captureProvince(target);
                defender.removeProvince(target);
                this.army = this.army / ARMY_LOSS_FACTOR;
                defender.setArmy(defender.getArmy() / ARMY_LOSS_FACTOR);
            } else if (attackerArmy < defenderArmy) { // defender holds
                this.army = this.army / ARMY_LOSS_FACTOR;
                defender.setArmy(defender.getArmy() / ARMY_LOSS_FACTOR);
            } else { // tie: both halved, no transfer
                this.army = this.army / ARMY_LOSS_FACTOR;
                defender.setArmy(defender.getArmy() / ARMY_LOSS_FACTOR);
            }
        }
    }
}