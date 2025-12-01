package universalis.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import universalis.Universalis;
import universalis.events.GameEvent;
import universalis.events.GameEventBus;
import universalis.map.Map;
import universalis.map.Nation;
import universalis.map.Province;

public class GameApplication extends Application {
    private static final int TILE_SIZE = 20;
    private Canvas canvas;
    private Universalis gameInstance;

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        canvas = new Canvas(800, 800);
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, 800, 800);
        primaryStage.setTitle("Mini Universalis (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();

        GameEventBus.getInstance().subscribe(this::onGameEvent);

        // Start the game in a separate thread
        new Thread(() -> {
            Universalis game = Universalis.setupDefaultGame(40, 10);
            game.setTurnDelay(50);
            game.playToCompletion();
        }).start();
    }

    private void onGameEvent(GameEvent event) {
        this.gameInstance = event.getGameInstance();
        if (event.getType() == GameEvent.Type.TURN_COMPLETED) {
            Platform.runLater(this::drawMap);
        } else if (event.getType() == GameEvent.Type.GAME_FINISHED) {
            Platform.runLater(() -> {
                drawMap();
                drawGameOver();
            });
        }
    }

    private void drawMap() {
        if (gameInstance == null)
            return;
        Map map = gameInstance.getMap();
        int width = map.getWidth();
        int height = map.getHeight();

        int mapPixelWidth = width * TILE_SIZE;
        int mapPixelHeight = height * TILE_SIZE;
        int legendWidth = 200;

        // Resize canvas if needed (map + legend)
        if (canvas.getWidth() != mapPixelWidth + legendWidth || canvas.getHeight() != Math.max(mapPixelHeight, 400)) {
            canvas.setWidth(mapPixelWidth + legendWidth);
            canvas.setHeight(Math.max(mapPixelHeight, 400));
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw Map
        gc.setFont(new javafx.scene.text.Font("Arial", 10));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Province province = map.getProvince(col, row);
                Nation owner = province.getOwner();

                if (owner != null) {
                    gc.setFill(getColorForNation(owner.getName()));
                } else {
                    gc.setFill(Color.LIGHTGRAY);
                }
                gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                gc.setStroke(Color.BLACK);
                gc.strokeRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (owner != null) {
                    gc.setFill(Color.BLACK); // Text color
                    String label = owner.getName().substring(0, 1);
                    gc.fillText(label, col * TILE_SIZE + TILE_SIZE / 2.0, row * TILE_SIZE + TILE_SIZE / 2.0);
                }
            }
        }

        // Draw Legend
        drawLegend(gc, mapPixelWidth, legendWidth);
    }

    private void drawLegend(GraphicsContext gc, int xOffset, int width) {
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(xOffset, 0, width, canvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setFont(new javafx.scene.text.Font("Arial", 14));
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.TOP);
        gc.fillText("Legend", xOffset + 10, 10);

        int y = 40;
        for (Nation nation : gameInstance.getNations()) {
            // Color box
            gc.setFill(getColorForNation(nation.getName()));
            gc.fillRect(xOffset + 10, y, 15, 15);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(xOffset + 10, y, 15, 15);

            // Name and stats
            gc.setFill(Color.BLACK);
            gc.setFont(new javafx.scene.text.Font("Arial", 12));
            String text = String.format("%s (%d)", nation.getName(), nation.getProvinceCount());
            gc.fillText(text, xOffset + 30, y);

            y += 25;
        }
    }

    private void drawGameOver() {
        if (gameInstance == null || canvas == null)
            return;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw semi-transparent overlay over the map part only, or whole canvas? Whole
        // canvas is fine.
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 30));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);

        String message = "Game Over";
        if (!gameInstance.getNations().isEmpty()) {
            Nation winner = gameInstance.getNations().get(0);
            message += "\nWinner: " + winner.getName();
            message += "\nProvinces: " + winner.getProvinceCount();
            message += "\nDevelopment: " + winner.getTotalDevelopment();
        } else {
            message += "\nNo Winner";
        }

        gc.fillText(message, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private Color getColorForNation(String name) {
        int hash = name.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);
        return Color.rgb(r, g, b);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
