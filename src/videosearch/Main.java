package videosearch;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Controller controller = new Controller();
        primaryStage.setTitle("Video Search");

        primaryStage.setScene(new Scene(controller, 750, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
