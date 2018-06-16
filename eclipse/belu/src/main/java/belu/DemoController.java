package belu;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class DemoController implements Initializable {

	@FXML
	private Label label;

	@FXML
	private TextField input;

	@FXML
	private void handleButtonAction(ActionEvent event) {
		System.out.println("You clicked me!");
		label.setText(input.getText());
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}
}