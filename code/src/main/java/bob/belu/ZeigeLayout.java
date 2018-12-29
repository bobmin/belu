package bob.belu;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ZeigeLayout extends Application {

	private static final int IMAGE_WIDTH = 640;

	private static final int IMAGE_HEIGHT = 480;

	private static final int TILE_SIZE = 16;

	private static final int WIDTH_FACTOR = 36;

	private static final int HEIGHT_FACTOR = 40;

	private static final int ZOOM = 2;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Group root = new Group();
		Scene scene = new Scene(root, WIDTH_FACTOR * TILE_SIZE, HEIGHT_FACTOR * TILE_SIZE, Color.BLACK);

		int image_width_times = (IMAGE_WIDTH / ZOOM) / TILE_SIZE;
		int image_height_times = (IMAGE_HEIGHT / ZOOM) / TILE_SIZE;

		System.out.println("image: " + image_width_times + "x" + image_height_times);

		int x = 0, y = 0;
		for (int idx = 0; idx < (WIDTH_FACTOR * HEIGHT_FACTOR); idx++) {
			x = (idx % WIDTH_FACTOR) * TILE_SIZE;
			if (idx > 0 && (idx % WIDTH_FACTOR) == 0) {
				y += TILE_SIZE;
			}
			Rectangle rect1 = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
			if (idx > (image_width_times * image_height_times)) {
				rect1.setFill(Color.TRANSPARENT);
			} else {
				rect1.setFill(Color.YELLOW);
			}

			rect1.setStroke(Color.RED);
			rect1.setStrokeWidth(1);
			root.getChildren().addAll(rect1);
		}


		primaryStage.setTitle("ZeigeLayout");
		primaryStage.setScene(scene);
		primaryStage.show();

	}



}
