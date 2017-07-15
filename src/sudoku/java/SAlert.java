package sudoku.java;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

class SAlert {

	private AlertType alertType;
	private String title;
	private String headerText;
	private String contentText;

	SAlert(AlertType alertType, String title, String contentText) {
		this.alertType = alertType;
		this.title = title;
		this.contentText = contentText;
	}

	void show() {
		Alert alert = new Alert(alertType);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/sudoku/imgs/logo.png"));
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait();
	}

}
