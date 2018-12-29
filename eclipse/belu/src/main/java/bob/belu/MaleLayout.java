package bob.belu;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MaleLayout extends Application {

	private static final String TITLE = "MaleLayout";

	private static final int TILE_SIZE = 16;

	private static final int WIDTH_FACTOR = 36;

	private static final int WIDTH = WIDTH_FACTOR * TILE_SIZE;

	private static final int HEIGHT_FACTOR = 40;

	private static final int HEIGHT = HEIGHT_FACTOR * TILE_SIZE;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ToggleButton drowbtn = new ToggleButton("Messen");
		ToggleButton rubberbtn = new ToggleButton("???");
		ToggleButton linebtn = new ToggleButton("???");

		ToggleButton[] toolsArr = { drowbtn, rubberbtn, linebtn };

		ToggleGroup tools = new ToggleGroup();

		for (ToggleButton tool : toolsArr) {
			tool.setMinWidth(90);
			tool.setToggleGroup(tools);
			tool.setCursor(Cursor.HAND);
		}

		ColorPicker cpLine = new ColorPicker(Color.BLACK);
		ColorPicker cpFill = new ColorPicker(Color.TRANSPARENT);

		TextArea text = new TextArea();
		text.setPrefRowCount(1);

		Slider slider = new Slider(1, 50, 3);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);

		Label line_color = new Label("Line Color");
		Label fill_color = new Label("Fill Color");
		Label line_width = new Label("3.0");

		Button save = new Button("Save");
		Button open = new Button("Open");

		Button[] basicArr = { save, open };

		for (Button btn : basicArr) {
			btn.setMinWidth(90);
			btn.setCursor(Cursor.HAND);
			btn.setTextFill(Color.WHITE);
			btn.setStyle("-fx-background-color: #666;");
		}
		save.setStyle("-fx-background-color: #80334d;");
		open.setStyle("-fx-background-color: #80334d;");

		VBox btns = new VBox(10);
		btns.getChildren().addAll(drowbtn, rubberbtn, linebtn, text, line_color, cpLine, fill_color, cpFill, line_width,
				slider, open, save);
		btns.setPadding(new Insets(5));
		btns.setStyle("-fx-background-color: #999");
		btns.setPrefWidth(100);

		Canvas canvas = new Canvas(WIDTH_FACTOR * TILE_SIZE, HEIGHT_FACTOR * TILE_SIZE);
		GraphicsContext gc;
		gc = canvas.getGraphicsContext2D();
		gc.setLineWidth(1);

		maleLayout(gc);

		canvas.setOnMousePressed(e -> {
			if (drowbtn.isSelected()) {
				gc.setStroke(cpLine.getValue());
				gc.beginPath();
				gc.lineTo(e.getX(), e.getY());
			}
		});

		canvas.setOnMouseDragged(e -> {
			if (drowbtn.isSelected()) {
				gc.lineTo(e.getX(), e.getY());
				gc.stroke();
			}
		});

		canvas.setOnMouseReleased(e -> {
			if (drowbtn.isSelected()) {
				gc.lineTo(e.getX(), e.getY());
				gc.stroke();
				gc.closePath();
			}
		});

		canvas.setOnMouseMoved(e -> {
			primaryStage.setTitle(String.format("%s (%d,%d)", TITLE, (int) e.getX(), (int) e.getY()));
		});

		// Open
		open.setOnAction((e) -> {
			FileChooser openFile = new FileChooser();
			openFile.setTitle("Open File");
			openFile.setInitialDirectory(
					new File("C:\\Entwicklung\\Projekte\\belu\\eclipse\\belu\\src\\main\\resources"));
			File file = openFile.showOpenDialog(primaryStage);
			if (file != null) {
				try (Scanner scanner = new Scanner(file)) {
					// InputStream io = new FileInputStream(file);
					// Image img = new Image(io);
					// gc.drawImage(img, 0, 0);
					int fw = 0;
					int fh = 11;
					while (scanner.hasNext()) {
						String line = scanner.nextLine();
						if (!line.startsWith("#") && 0 < line.trim().length()) {
							String wh[] = line.split(":");
							int w = Integer.parseInt(wh[0]);
							int h = Integer.parseInt(wh[1]);

							if (w > h) {
								gc.setStroke(Farben.COLORS[fw]);
								Image hatch = createHatch(Farben.COLORS[fw]);
								ImagePattern pattern = new ImagePattern(hatch, 0, 0, 20, 20, false);
								gc.setFill(pattern);
								fw++;
							} else {
								gc.setStroke(Farben.COLORS[fh]);
								Image hatch = createHatch(Farben.COLORS[fh]);
								ImagePattern pattern = new ImagePattern(hatch, 0, 0, 20, 20, false);
								gc.setFill(pattern);
								fh--;
							}
							gc.fillRect(0, 0, w, h);
							System.out.println(line);
						}
					}
				} catch (IOException ex) {
					System.out.println("Error!");
				}
			}
		});

		// Save
		save.setOnAction((e) -> {
			FileChooser savefile = new FileChooser();
			savefile.setTitle("Save File");

			File file = savefile.showSaveDialog(primaryStage);
			if (file != null) {
				try {
					WritableImage writableImage = new WritableImage(1080, 790);
					canvas.snapshot(null, writableImage);
					RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
					ImageIO.write(renderedImage, "png", file);
				} catch (IOException ex) {
					System.out.println("Error!");
				}
			}

		});

		BorderPane pane = new BorderPane();
		pane.setRight(btns);
		pane.setCenter(canvas);

		Scene scene = new Scene(pane);

		primaryStage.setTitle(TITLE);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	private Image createHatch(final Color color) {
		Pane pane = new Pane();
		pane.setPrefSize(20, 20);
		Line fw = new Line(-5, -5, 25, 25);
		Line bw = new Line(-5, 25, 25, -5);
		fw.setStroke(color);
		bw.setStroke(color);
		fw.setStrokeWidth(5);
		bw.setStrokeWidth(5);
		pane.getChildren().addAll(fw, bw);
		new Scene(pane);
		return pane.snapshot(null, null);
	}

	private void maleLayout(GraphicsContext gc) {
		// Hintergrund
		gc.setStroke(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		// Gitter
		gc.setStroke(Color.BLACK);
		gc.setLineDashes(1d, 2d);
		for (double x = 0; x < WIDTH_FACTOR * TILE_SIZE; x += TILE_SIZE) {
			gc.strokeLine(x, 0, x, HEIGHT);
		}
		for (double y = 0; y < HEIGHT_FACTOR * TILE_SIZE; y += TILE_SIZE) {
			gc.strokeLine(0, y, WIDTH, y);
		}
	}

}
