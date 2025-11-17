package universalis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import universalis.map.Map;
import universalis.map.Nation;
import universalis.map.Province;
import universalis.map.factory.NationFactory;
import universalis.strategy.DefensiveStrategy;
import universalis.strategy.NoOpStrategy;
import universalis.strategy.OffensiveStrategy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static universalis.map.factory.NationFactory.clearUsedNamesForTests;

public class UniversalisTest {

    @Test
    public void runTurnsProgresses() {
        Map.Builder builder = new Map.Builder(new java.util.Random(7));
        Map map = builder.create(3).build();

        Nation a = new Nation("A", new NoOpStrategy());
        Province p = map.getProvince(0,0);
        p.setOwner(a);
        a.captureProvince(p);

        List<Nation> nations = new ArrayList<>();
        nations.add(a);

        Universalis game = new Universalis(map, nations);

        game.runTurns(2);

        assertTrue(a.getProvinceCount() >= 1);
    }

    @Test
    public void setupDefaultGameReturnsConfiguredGame() {
        clearUsedNamesForTests();
        Universalis g = Universalis.setupDefaultGame(6, 3);
        assertNotNull(g);
        assertEquals(6, g.getMap().getWidth());
        assertEquals(6, g.getMap().getHeight());
        assertEquals(3, g.getNations().size());
    }

    @Test
    public void battleTransfersOwnershipAndHalvesArmies() {
        // Build a 2x1 map and set explicit province development values
        Map.Builder builder = new Map.Builder(new Random(123));
        Map map = builder.createNonSquare(2, 1).build();

        // replace provinces with deterministic development values
        Province p0 = new Province(5); // left, stronger development
        Province p1 = new Province(2); // right, weaker development
        map.setProvince(0, 0, p0);
        map.setProvince(1, 0, p1);

        // Create attacker and defender
        Nation attacker = new Nation("Att", new OffensiveStrategy(new Random(1)));
        Nation defender = new Nation("Def", new DefensiveStrategy());

        // Assign owners & bookkeeping
        p0.setOwner(attacker);
        attacker.captureProvince(p0);

        p1.setOwner(defender);
        defender.captureProvince(p1);

        // Initialize armies equal to total development (per spec)
        attacker.setArmy(attacker.getTotalDevelopment()); // 5
        defender.setArmy(defender.getTotalDevelopment()); // 2

        // Construct game (Universalis)
        List<Nation> nations = new ArrayList<>();
        nations.add(attacker);
        nations.add(defender);
        Universalis uni = new Universalis(map, nations, new Random(1));

        // Force the attacker to attempt a capture (call expandOrAttack directly)
        attacker.expandOrAttack(uni, new Random(2));

        // Since attackerArmy (5) > defenderArmy (2), the attacker should capture p1
        assertEquals(attacker, map.getProvince(1, 0).getOwner(), "Attacker should own the captured province");
        assertTrue(attacker.getProvinceCount() >= 2, "Attacker should have gained a province");
        assertEquals(0, defender.getProvinceCount(), "Defender should have lost its province");

        // Armies should be halved after battle
        assertEquals(5 / 2, attacker.getArmy(), "Attacker army should be halved after winning");
        assertEquals(1, defender.getArmy(), "Defender army should be halved after losing");
    }

    @Test
    public void playToCompletion_printsFullGameAndFinishes() {
        List<Nation> nations = new ArrayList<>();
        int mapSize = 4;
        int totalNations = 5;

        int i = 0;
        while (i < totalNations) {
            nations.add(NationFactory.createRandomNation());
            i++;
        }

        Random mapRng = new Random();
        Map.Builder builder = new Map.Builder(mapRng);
        Map map = builder.create(mapSize)
                .seedNations(nations)
                .build();

        Universalis uni = new Universalis(map, nations, new Random());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            uni.playToCompletion();
        } finally {
            System.out.flush();
            System.setOut(oldOut);
        }

        String output = outputStream.toString();
        System.out.println(output);

        assertTrue(output.contains("\n=== Universalis Map snapshot ===\n"),
                "Expected map snapshots printed during play.");
        assertTrue(output.contains("Finished. Winner:") || output.contains("Stalemate detected"),
                "Expected either a winner message or stalemate message in output.");
    }

    @Test
    @Disabled
    void compareWinsToStalemates() {
        final int numGames = 100;
        final int gameSize = 4;
        final int nationsPerGame = 5;

        int wins = 0;
        int stalemates = 0;

        for (int i = 0; i < numGames; i++) {
            NationFactory.clearUsedNamesForTests();

            List<Nation> nations = new ArrayList<>();
            for (int ii = 0; ii < nationsPerGame; ii++) {
                Nation n = NationFactory.createRandomNation();
                nations.add(n);
            }

            Map.Builder builder = new Map.Builder(new Random());
            Map map = builder
                    .create(gameSize)
                    .seedNations(nations)
                    .build();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Universalis universalis = new Universalis(map, nations, new Random());
            universalis.playToCompletion();
            String output = outputStream.toString();

            if (output.contains("Stalemate detected")) stalemates++;
            else wins++;
        }

        System.out.println("\n\n\n=== 100-game summary ===");
        System.out.println("Wins      " + wins);
        System.out.println("Stalemates" + stalemates);
        System.out.println("========================");

        assertEquals(numGames, wins + stalemates);
    }
}
