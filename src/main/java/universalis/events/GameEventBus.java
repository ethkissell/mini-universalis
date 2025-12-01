package universalis.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameEventBus {
    private static GameEventBus instance;
    private final List<Consumer<GameEvent>> subscribers = new ArrayList<>();

    private GameEventBus() {
    }

    public static synchronized GameEventBus getInstance() {
        if (instance == null) {
            instance = new GameEventBus();
        }
        return instance;
    }

    public void subscribe(Consumer<GameEvent> subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(GameEvent event) {
        for (Consumer<GameEvent> subscriber : subscribers) {
            subscriber.accept(event);
        }
    }

    // Helper to reset for testing purposes if needed
    public static synchronized void reset() {
        instance = new GameEventBus();
    }
}
