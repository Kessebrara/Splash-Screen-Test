import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.event.EventHandler;

/**
 * Example of displaying a splash page for a standalone JavaFX application
 */
public class SplashTest extends Application 
{
    public static final String APPLICATION_ICON = "people.png";
    public static final String SPLASH_IMAGE = "uber_splash.png";
    
    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private static final int SPLASH_WIDTH = 250;
    private static final int SPLASH_HEIGHT = 375;

    public static void main(String[] args) throws Exception 
    {
        launch(args);
    }

    @Override
    public void init()
    {
        ImageView splash = new ImageView(new Image(SPLASH_IMAGE));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Will find friends for peanuts . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
            "-fx-padding: 5; " +
            "-fx-background-color: cornsilk; " +
            "-fx-border-width:5; " +
            "-fx-border-color: " +
            "linear-gradient(" +
            "to bottom, " +
            "chocolate, " +
            "derive(chocolate, 50%)" +
            ");"
        );
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(final Stage initStage) throws Exception
    {
        final Task<ObservableList<String>> loadingTask = new Task<ObservableList<String>>()
        {
                @Override
                protected ObservableList<String> call() throws InterruptedException {
                    ObservableList<String> foundFriends =
                    FXCollections.<String>observableArrayList();
                    ObservableList<String> availableFriends =
                        FXCollections.observableArrayList(
                        "Loading world",
                        "Loading world",
                        "Loading world",
                        "Loading world",
                        "Loading world",
                        "Loading world",
                        "Loading world",
                        "Loading world",
                        "Done loading!"
                        );

                    updateMessage("Loading processes . . .");
                    for (int i = 0; i < availableFriends.size(); i++) {
                        Thread.sleep(400);
                        updateProgress(i + 1, availableFriends.size());
                        String nextFriend = availableFriends.get(i);
                        foundFriends.add(nextFriend);
                        updateMessage("Assets loading! " + nextFriend);
                    }
                    Thread.sleep(400);
                    updateMessage("Launching game. Good luck!");

                    return foundFriends;
                }
            };

        showSplash(initStage, loadingTask,() -> showMainStage(loadingTask.valueProperty()));
        new Thread(loadingTask).start();
        
        initStage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent e)
            {
                System.exit(1);
            }
        });
    }

    private void showMainStage(
    ReadOnlyObjectProperty<ObservableList<String>> friends
    ) {
        mainStage = new Stage(StageStyle.DECORATED);
        mainStage.setTitle("My Friends");
        mainStage.getIcons().add(new Image(
                APPLICATION_ICON
            ));

        final ListView<String> peopleView = new ListView<>();
        peopleView.itemsProperty().bind(friends);

        mainStage.setScene(new Scene(peopleView));
        mainStage.show();
        
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent e)
            {
                System.exit(1);
            }
        });
    }

    private void showSplash(
    final Stage initStage,
    Task<?> task,
    InitCompletionHandler initCompletionHandler
    ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    loadProgress.progressProperty().unbind();
                    loadProgress.setProgress(1);
                    initStage.toFront();
                    FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                    fadeSplash.setFromValue(1.0);
                    fadeSplash.setToValue(0.0);
                    fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                    fadeSplash.play();

                    initCompletionHandler.complete();
                } // todo add code to gracefully handle other task states.
            });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public interface InitCompletionHandler {
        void complete();
    }
}