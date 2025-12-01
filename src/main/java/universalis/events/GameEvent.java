package universalis.events;

import universalis.Universalis;

public class GameEvent {
    public enum Type {
        TURN_COMPLETED,
        GAME_FINISHED
    }

    private final Type type;
    private final Universalis gameInstance;

    public GameEvent(Type type, Universalis gameInstance) {
        this.type = type;
        this.gameInstance = gameInstance;
    }

    public Type getType() {
        return type;
    }

    public Universalis getGameInstance() {
        return gameInstance;
    }
}
