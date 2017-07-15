package sudoku.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;


public class Board implements Initializable {

	Sudoku sudoku = new Sudoku();

	private Matrix puzzle;
	private Matrix ans;
	private Map<String, String> ansMap;

	private boolean stop;
	private long startTime;
	private long curTime;
	private Thread thread;

	@FXML private CheckMenuItem noteItem;
	@FXML private CheckMenuItem showCandidates;
	@FXML private CheckMenuItem showSelectedRC;
	@FXML private CheckMenuItem showSelectedBlock;

	@FXML private ImageView noteIcon;
	@FXML private Text times;
	@FXML private Text timer;
	@FXML private ChoiceBox<String> level;
	@FXML private List<SCell> cells;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		addCellFocusedProperty();
		showNoteIcon();
		times.setText("5");
		initTimer();
		puzzle = sudoku.init(getLevel()).get(0);
		ans = sudoku.getAns();
		ansMap = ansParse(this.ans);
		setCellNums(puzzle);
	}

	private void addCellFocusedProperty() {
		level.setFocusTraversable(false);
		for (SCell cell : cells) {
			cell.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if (newValue.booleanValue()) {
						showSelectedCells(cell);
					}
				}
			});
		}
	}

	private int getLevel() {
		if (level.getValue().equals("Easy")) return 1;
		else if (level.getValue().equals("Normal")) return 2;
		else if (level.getValue().equals("Difficult")) return 3;
		else return 4;
	}

	private void setCellNums(Matrix puzzle) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				cells.get(i * 9 + j).setInitStatus();
				if (puzzle.get(i, j) != 0) {
					cells.get(i * 9 + j).setText(Integer.toString(puzzle.get(i, j)));
				} else {
					cells.get(i * 9 + j).setText("");
				}
			}
		}
	}

	private void initTimer() {
		stop = false;
		startTime = System.currentTimeMillis();
		thread = new Thread() {
			public void run() {
				while (!stop) {
					curTime = System.currentTimeMillis();
					int interval = (int) ((curTime - startTime) / 1000);
					int hour = interval / 3600;
					int minute = (interval / 60) % 60;
					int second = interval % 60;
					timer.setText(String.format("%02d:%02d:%02d", hour, minute, second));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * 将二维数组类型的数独ANS转为Map类型。
	 */
	private Map<String, String> ansParse(Matrix ans) {
		Map<String, String> ansMap = new HashMap<String, String>();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				ansMap.put("cell" + i + j, String.valueOf(ans.get(i, j)));
			}
		}
		return ansMap;
	}

	private boolean checkSucess() {
		for (SCell cell : cells) {
			if (cell.getText().isEmpty() || cell.status.equals(CStatus.NOTE)) {
				return false;
			}
		}
		return true;
	}

	@FXML private void keyListener(KeyEvent e) {
		String input = e.getText();
		SCell cell = ((SCell) e.getSource());
		if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
			if (cell.status.equals(CStatus.INIT) && !cell.getText().isEmpty()) {
				highLight(cell);
			}
		} else if (!noteItem.isSelected()) {
			setInputText(cell, input);
		} else {
			cell.setNoteText(input);
		}
	}

	@FXML private void mouseListener(MouseEvent e) {
		SCell cell = (SCell) e.getSource();
		if (cell.status.equals(CStatus.NOTE) || showSelectedRC.isSelected() || showSelectedBlock.isSelected()) return;
		highLight(cell);
	}

	private void setInputText(SCell cell, String input) {
		if (!(cell.getText().isEmpty() || cell.status.equals(CStatus.NOTE) || cell.status.equals(CStatus.ERROR))) return;
		if (!input.isEmpty() && "123456789".contains(input)) {
			if (ansMap.get(cell.getId()).equals(input)) {
				cell.setInitStatus();;
				cell.setText(input);
				if (checkSucess()) {
					stop = true;
					new SAlert(AlertType.INFORMATION, "Sucessful", "Sucessful!\nTime: " + timer.getText()).show();
				}
			} else {
				cell.setErrorStatus();
				int leftTimes = Integer.parseInt(times.getText()) - 1;
				if (leftTimes < 0) {
					new SAlert(AlertType.ERROR, "Fail", "Fail!\nTime: " + timer.getText()).show();
					times.setFill(Color.RED);
				}
				times.setText(String.valueOf(leftTimes));
			}
		} else if (input.equals("0")) {
			cell.setText("");
		}
	}

	private void highLight(SCell cell) {
		// cell空或ERROR或已经高亮，取消所有高亮
		if (cell.getText().isEmpty() || cell.status.equals(CStatus.ERROR) || cell.status.equals(CStatus.NOTE) || cell.isHighLight) {
			for (SCell cell2 : cells) {
				if (cell2.status.equals(CStatus.INIT)) {
					cell2.setInitStatus();
				}
			}
			// cell未高亮，则执行高亮
		} else {
			for (SCell cell2 : cells) {
				if (cell2.getText().equals(cell.getText()) && !cell2.status.equals(CStatus.NOTE)) {
					cell2.setHighLightStatus();
				} else if (cell2.status.equals(CStatus.INIT)) {
					cell2.setInitStatus();
				} else if (cell2.status.equals(CStatus.ERROR)) {
					cell2.setEffect(null);
					cell2.isHighLight = false;
					cell2.setErrorStatus();
				} else if (cell2.status.equals(CStatus.NOTE)) {
					cell2.setEffect(null);
					cell2.isHighLight = false;
					cell2.setNoteStatus(getFontSize(cell2.getText()));
				}
			}
		}
	}

	@FXML private void newGame() {
		noteItem.setSelected(false);
		showCandidates.setSelected(false);
		showNoteIcon();
		times.setText("5");
		times.setFill(Color.BLACK);
		puzzle = sudoku.init(getLevel()).get(0);
		ans = sudoku.getAns();
		ansMap = ansParse(this.ans);
		setCellNums(puzzle);
		initTimer();
	}

	@FXML private void solve() {
		int num = 0;
		for (SCell cell: cells) {
			if (cell.getText().isEmpty()) num = 0;
			else num = Integer.parseInt(cell.getText());
			puzzle.set(Character.getNumericValue(cell.getId().charAt(4)), Character.getNumericValue(cell.getId().charAt(5)), num);
			ans = sudoku.solve(puzzle).get(0);
			setCellNums(ans);
			stop = true;
		}
	}

	@FXML private void openFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open puzzle");
		File file = fileChooser.showOpenDialog(Main.getPrimaryStage());
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"), new ExtensionFilter("All Files", "*.*"));
		if (file != null) {
			String[][] inputPuzzle = new String[9][9];
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line;
				int lineNum = 0;
				while ((line = reader.readLine()) != null) {
					inputPuzzle[lineNum] = line.trim().split("\\s+");
					lineNum++;
				}
				reader.close();
				for (int i = 0; i < 9; i++) {
					for (int j = 0; j < 9; j++) {
						puzzle.set(i, j, Integer.parseInt(inputPuzzle[i][j]));
					}
				}
			} catch (FileNotFoundException e) {
				new SAlert(AlertType.ERROR, "Error", "File does not exist!").show();
				return;
			} catch (IOException e) {
				new SAlert(AlertType.ERROR, "Error", "File read error!").show();
				return;
			} catch (Exception e) {
				new SAlert(AlertType.ERROR, "Error", "Format error!\nSudoku file must be 9 rows/columns, and separated by space. The unfilled cells should be 0.").show();
				return;
			}
			ans = sudoku.solve(puzzle).get(0);
			setCellNums(puzzle);
			initTimer();
		}
	}

	@FXML private void saveFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save puzzle");
		File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"), new ExtensionFilter("All Files", "*.*"));
		BufferedWriter writer;
		if (file != null) {
			try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
				String num;
				for (int i = 0; i < cells.size(); i++) {
					num = cells.get(i).getText();
					if (num.isEmpty() || num.equals("X")) writer.write("0 ");
					else writer.write(num + " ");
					if ((i + 1) % 9 == 0) writer.newLine();
				}
				writer.close();
			} catch (IOException e) {
				new SAlert(AlertType.ERROR, "Error", "File write error!").show();
			}
		}
	}

	@FXML private void exit() {
		stop = true;
		Platform.exit();
		System.exit(0);
	}

	@FXML private void showCandidates() {
		if (showCandidates.isSelected()) {
			String known;
			List<String> candidates = new ArrayList<String>();
			for (SCell cell : cells) {
				candidates.clear();
				known = "";
				if (cell.getText().isEmpty() || cell.status.equals(CStatus.ERROR)) {
					int row = Character.getNumericValue(cell.getId().charAt(4));
					int column = Character.getNumericValue(cell.getId().charAt(5));
					int block = sudoku.getBlock(row, column);
					for (SCell cell2 : cells) {
						String cell2Text = cell2.getText();
						int row2 = Character.getNumericValue(cell2.getId().charAt(4));
						int column2 = Character.getNumericValue(cell2.getId().charAt(5));
						int block2 = sudoku.getBlock(row2, column2);
						if (cell2Text.length() == 1 && !cell.status.equals(CStatus.ERROR) && !known.contains(cell2Text)) {
							if (row2 == row || column2 == column || block2 == block) {
								known += cell2Text;
							}
						}
					}
					for (int i = 1; i < 10; i++) {
						if (!known.contains(String.valueOf(i))) {
							candidates.add(String.valueOf(i));
						}
					}
					Collections.sort(candidates);
					String cellText = String.join("", candidates);
					cell.setNoteStatus(getFontSize(cellText));
					cell.wrapTextProperty().setValue(true);
					cell.setText(cellText);
				}
			}
		} else {
			for (SCell cell: cells) {
				if (cell.status.equals(CStatus.NOTE)) {
					cell.setText("");
				}
			}
		}
	}

	private void showSelectedCells(SCell focusCells) {
		if (!showSelectedRC.isSelected() && !showSelectedBlock.isSelected()) return;
		List<SCell> showCells = new ArrayList<SCell>();
		for (SCell cell : cells) {
			if (showSelectedRC.isSelected() && (cell.getId().charAt(4) == focusCells.getId().charAt(4) || cell.getId().charAt(5) == focusCells.getId().charAt(5))) {
				showCells.add(cell);
			}
			if (showSelectedBlock.isSelected() && sudoku.getBlock(Character.getNumericValue(cell.getId().charAt(4)), Character.getNumericValue(cell.getId().charAt(5))) == sudoku.getBlock(Character.getNumericValue(focusCells.getId().charAt(4)), Character.getNumericValue(focusCells.getId().charAt(5)))) {
				showCells.add(cell);
			}
		}
		if (showSelectedRC.isSelected() || showSelectedBlock.isSelected()) {
			for (SCell cell : cells) {
				if (cell.equals(focusCells)) {
					cell.setEffect(null);
					cell.isHighLight = false;
				} else if (showCells.contains(cell)) {
					cell.setHighLightStatus();
				} else {
					cell.isHighLight = false;
					if (cell.status.equals(CStatus.INIT) && !cell.isHighLight) {
						cell.setInitStatus();
					} else if (cell.status.equals(CStatus.NOTE)) {
						cell.setNoteStatus(getFontSize(cell.getText()));
						cell.setEffect(null);
					} else if (cell.status.equals(CStatus.ERROR)) {
						cell.setErrorStatus();
						cell.setEffect(null);
					}
				}
			}
		}
	}

	private int getFontSize(String cellText) {
		if (cellText.length() <= 2) return 24 - cellText.length() * 4;
		else return 12;
	}

	@FXML private void hideAllHighLightCells() {
		for (SCell cell : cells) {
			cell.isHighLight = false;
			if (cell.status.equals(CStatus.INIT) && !cell.isHighLight) {
				cell.setInitStatus();
			} else if (cell.status.equals(CStatus.NOTE)) {
				cell.setNoteStatus(getFontSize(cell.getText()));
				cell.setEffect(null);
			} else if (cell.status.equals(CStatus.ERROR)) {
				cell.setErrorStatus();
				cell.setEffect(null);
			}
		}
	}

	@FXML private void showNoteIcon() {
		if (noteItem.isSelected()) {
			noteIcon.setVisible(true);
		} else {
			noteIcon.setVisible(false);
		}
	}

	@FXML private void showHelp() {
		new SAlert(AlertType.INFORMATION, "Help", "Operation：\n - Move: direction key (↑↓←→) or mouse.\n - Input: numeric key.\n\nFunction:\n - Open: open a local sudoku file.\n - Save: save as sudoku file.\n - Solve: show the answer of sudoku.\n - Highlight: highlight cells.\n - Note mode: input but not verify.\n - Show candidates: show all candidates.\n - Show selected row/column/block.").show();
	}

	@FXML private void showAbout() {
		new SAlert(AlertType.INFORMATION, "About", "Sudoku v0.1\n2017-07-14").show();
	}

}
