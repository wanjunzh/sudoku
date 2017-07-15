package sudoku.java;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	private static Stage primaryStage;
	private static final double PRE_WIN_WIDTH = 600;
	private static final double PRE_WIN_HEIGHT = 600;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		Main.primaryStage = primaryStage;
		Parent root = FXMLLoader.load(getClass().getResource("/sudoku/resource/mainui.fxml"));
		Scene scene = new Scene(root, PRE_WIN_WIDTH, PRE_WIN_HEIGHT);
		primaryStage.setTitle("Sudoku");
		primaryStage.getIcons().add(new Image("/sudoku/imgs/logo.png"));
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}
}