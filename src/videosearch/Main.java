package videosearch;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{


        final long startTime;
        final long endTime;

        startTime = System.currentTimeMillis();
        Controller controller = new Controller();
        endTime = System.currentTimeMillis();
        Debug.print("Total execution time: " + (endTime - startTime));

        primaryStage.setTitle("VideoSearch");

        primaryStage.setScene(new Scene(controller, 820, 600));
        primaryStage.setResizable(false);
        primaryStage.show();


    }


    public static void main(String[] args) {
        launch(args);
    }
}
