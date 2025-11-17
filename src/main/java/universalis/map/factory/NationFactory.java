package universalis.map.factory;

import universalis.map.Nation;
import universalis.strategy.NoOpStrategy;

import java.util.*;

public class NationFactory {
    private static final List<String> BASE_NAMES = Arrays.asList(
            "France", "Japan", "India", "China", "Brazil", "Norway", "Sweden", "Spain", "Peru", "Mexico",
            "Canada", "Turkey", "Poland", "Greece", "Morocco", "Nepal", "Vietnam", "Thailand", "Australia",
            "England", "Portugal", "Italy", "Indonesia", "Scotland", "Antarctica"
    );

    private static final List<String> SUFFIXES = Arrays.asList(
            "County","Duchy","Kingdom","Empire","Province","Realm","Republic","Federation","Tribe","Dynasty",
            "Coalition","Union","Confederacy","Dominion","Territory","Colony","Collective","Clan","Regime","League",
            "Protectorate","Domain","March","Principality","Faction"
    );

    private static final Random random = new Random();
    private static final Set<String> usedNames = new HashSet<>();

    public static Nation createRandomNation() {
        if (usedNames.size() >= BASE_NAMES.size() * SUFFIXES.size())
            throw new IllegalStateException("All unique name combinations exhausted");

        String name;
        do {
            String base = BASE_NAMES.get(random.nextInt(BASE_NAMES.size()));
            String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
            name = base + " " + suffix;
        } while (usedNames.contains(name));
        usedNames.add(name);
        return new Nation(name, new NoOpStrategy());
    }

    // test helper
    public static void clearUsedNamesForTests() { usedNames.clear(); }
}