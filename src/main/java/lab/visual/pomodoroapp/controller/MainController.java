package lab.visual.pomodoroapp.controller;

import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import lab.visual.pomodoroapp.MainApp;
import lab.visual.pomodoroapp.model.Task;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;

public class MainController {

    @FXML private TextField estimatedPomodorosField;
    @FXML private Label timerLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button startBtn, pauseBtn, resetBtn;
    @FXML private ToggleButton themeToggle;
    @FXML private TextField taskField;
    @FXML private ListView<Task> taskList;
    @FXML private ToggleButton pomodoroBtn;
    @FXML private ToggleButton shortBreakBtn;
    @FXML private ToggleButton longBreakBtn;
    @FXML private Button settingsBtn;


    // Timer settings
    private int[] sessionDurations = {25 * 60, 5 * 60, 20 * 60}; // Pomodoro, ShortBreak, LongBreak in seconds
    private int sessionType = 0; // 0 - Pomodoro, 1 - Short, 2 - Long
    private int cycles = 0;
    private int timeLeft;
    private Timeline timeline;
    private boolean isRunning = false;
    private ObservableList<Task> tasks = FXCollections.observableArrayList();
    private String currentTheme = "light";
    private ToggleGroup modeToggleGroup;

    @FXML
    public void initialize() {
        resetSession();
        taskList.setItems(tasks);
        pauseBtn.setDisable(true);

        modeToggleGroup = new ToggleGroup();
        pomodoroBtn.setToggleGroup(modeToggleGroup);
        shortBreakBtn.setToggleGroup(modeToggleGroup);
        longBreakBtn.setToggleGroup(modeToggleGroup);
        pomodoroBtn.setSelected(true); // default selection
    }

    // Timer logic
    @FXML
    public void startTimer() {
        if (isRunning) return;
        isRunning = true;
        startBtn.setDisable(true);
        pauseBtn.setDisable(false);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            updateTimerDisplay();
            updateProgressBar();
            if (timeLeft <= 0) {
                timeline.stop();
                isRunning = false;
                playAlarm();
                showVisualAlert();
                if (sessionType == 0) { // 0 means Pomodoro session finished
                    incrementSelectedTaskPomodoro();
                }

                nextSession();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    public void pauseTimer() {
        if (timeline != null) timeline.pause();
        isRunning = false;
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
    }

    @FXML
    public void resetTimer() {
        if (timeline != null) timeline.stop();
        resetSession();
        isRunning = false;
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
    }

    private void resetSession() {
        timeLeft = sessionDurations[sessionType];
        updateTimerDisplay();
        updateProgressBar();
    }

    private void nextSession() {
        if (sessionType == 0) { // Pomodoro finished
            cycles++;
            if (cycles % 4 == 0){
                longBreakBtn.setSelected(true);
                sessionType = 2; // Long break
            }
            else{
                shortBreakBtn.setSelected(true);
                sessionType = 1; // Short break
            }

        } else {
            sessionType = 0;
            pomodoroBtn.setSelected(true);
        }
        resetSession();
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
    }

    private void updateTimerDisplay() {
        int min = timeLeft / 60;
        int sec = timeLeft % 60;
        timerLabel.setText(String.format("%02d:%02d", min, sec));
    }

    private void updateProgressBar() {
        double progress = 1.0 * (sessionDurations[sessionType] - timeLeft) / sessionDurations[sessionType];
        progressBar.setProgress(progress);
    }

    private void playAlarm() {
        try {
            String alarmFile = getClass().getResource("/sound/alarm.mp3").toString();
            AudioClip clip = new AudioClip(alarmFile);
            clip.play();
        } catch (Exception e) {
            // fallback: beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    // Session switchers
    @FXML
    public void switchToPomodoro(ActionEvent event) {
        if(sessionType == 0) {
            pomodoroBtn.setSelected(true);
            return;
        }
        if(isTimerRunningOrInProgress()) {
            boolean confirmed = showConfirmationDialog("Switching mode will reset your current progress. Continue?");
            if (!confirmed) {
                if(sessionType == 1) {
                    shortBreakBtn.setSelected(true);
                }else if(sessionType == 2) {
                    longBreakBtn.setSelected(true);
                }
                return;
            }
            sessionType = 0;
            resetSession();
        }

        sessionType = 0; resetSession();
    }
    @FXML
    public void switchToShortBreak(ActionEvent event) {
        if(sessionType == 1) {
            shortBreakBtn.setSelected(true);
            return;
        }
        if (isTimerRunningOrInProgress()) {
            boolean confirmed = showConfirmationDialog("Switching mode will reset your current progress. Continue?");
            if (!confirmed) {
                if(sessionType == 0) {
                    pomodoroBtn.setSelected(true);
                }else if(sessionType == 2) {
                    longBreakBtn.setSelected(true);
                }
                return;
            }
        }
        sessionType = 1;
        resetSession();
    }
    @FXML
    public void switchToLongBreak(ActionEvent event) {
        if(sessionType == 2) {
            longBreakBtn.setSelected(true);
            return;
        }
        if (isTimerRunningOrInProgress()) {
            boolean confirmed = showConfirmationDialog("Switching mode will reset your current progress. Continue?");
            if (!confirmed) {
                if(sessionType == 0) {
                    pomodoroBtn.setSelected(true);
                }else if(sessionType == 1) {
                    shortBreakBtn.setSelected(true);
                }
                return;
            }
        }
        sessionType = 2;
        resetSession();
    }

    // Task management
    @FXML
    public void addTask() {
        String name = taskField.getText().trim();
        String estPomodorosText = estimatedPomodorosField.getText().trim();

        int estPomodoros = 1;

        if (name.isEmpty()) {
            return;
        }

        try {
            estPomodoros = Integer.parseInt(estPomodorosText);
            if (estPomodoros < 1) estPomodoros = 1; // clamp negative numbers
        } catch (NumberFormatException e) {
            estPomodoros = 1; // Default to zero if invalid input
        }

        tasks.add(new Task(name, estPomodoros));

        taskField.clear();
        estimatedPomodorosField.clear();

        taskList.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName() + " [" + item.getPomodorosCompleted() + "/" + item.getEstimatedPomodoros() + "]");
                }
            }
        });
    }

    @FXML
    public void deleteTask() {
        Task selected = taskList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tasks.remove(selected);
        }
    }

    // Theme switching
    @FXML
    public void toggleTheme(ActionEvent event) {
        if (currentTheme.equals("light")) {
            currentTheme = "dark";
            themeToggle.setText("🌙");
        } else {
            currentTheme = "light";
            themeToggle.setText("☀️");
        }
        MainApp.setTheme(currentTheme);
    }

    private boolean isTimerRunningOrInProgress() {
        return isRunning || timeLeft < sessionDurations[sessionType];
    }

    private boolean showConfirmationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Mode Switch");
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait().filter(response -> response == yesButton).isPresent();
    }

    private void showVisualAlert() {
        DropShadow glow = new DropShadow();
        if(currentTheme.equals("light")) {
            glow.setColor(Color.RED);
        }else {
            glow.setColor(Color.WHITESMOKE);
        }

        glow.setRadius(20);
        glow.setSpread(0.7);

        timeline.pause();

        // Animate glow effect on timerLabel
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> timerLabel.setEffect(glow)),
                new KeyFrame(Duration.seconds(0.5), e -> timerLabel.setEffect(null)),
                new KeyFrame(Duration.seconds(1), e -> timerLabel.setEffect(glow)),
                new KeyFrame(Duration.seconds(1.5), e -> timerLabel.setEffect(null))
        );

        flash.play();
    }

    private void incrementSelectedTaskPomodoro() {
        Task selectedTask = taskList.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            selectedTask.incrementPomodoros();
            // Refresh the list view to update text, if needed
            taskList.refresh();
        }
    }

    @FXML
    public void updateTask() {
        Task selectedTask = taskList.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {

            return;
        }

        String newName = taskField.getText().trim();
        String estPomodorosText = estimatedPomodorosField.getText().trim();

        if (newName.isEmpty()) {
            newName = selectedTask.getName();
        }

        int newEstPomodoros = 0;
        try {
            newEstPomodoros = Integer.parseInt(estPomodorosText);
            if (newEstPomodoros < 1) newEstPomodoros = selectedTask.getEstimatedPomodoros();
        } catch (NumberFormatException e) {
            newEstPomodoros = selectedTask.getEstimatedPomodoros();  // Or handle invalid input better
        }

        // Update the task properties
        selectedTask.setName(newName);
        selectedTask.setEstimatedPomodoros(newEstPomodoros);

        // Refresh ListView
        taskList.refresh();

        taskField.clear();
        estimatedPomodorosField.clear();
    }

    @FXML
    public void openSettingsDialog() {
        // Create input fields prefilled with current durations (in minutes)
        TextField pomodoroField = new TextField(String.valueOf(sessionDurations[0] / 60));
        TextField shortBreakField = new TextField(String.valueOf(sessionDurations[1] / 60));
        TextField longBreakField = new TextField(String.valueOf(sessionDurations[2] / 60));

        // Layout for dialog inputs
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Pomodoro (minutes):"), 0, 0);
        grid.add(pomodoroField, 1, 0);
        grid.add(new Label("Short Break (minutes):"), 0, 1);
        grid.add(shortBreakField, 1, 1);
        grid.add(new Label("Long Break (minutes):"), 0, 2);
        grid.add(longBreakField, 1, 2);

        // Setup dialog window
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Pomodoro Timer Settings");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validate input before allowing OK button
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                int p = Integer.parseInt(pomodoroField.getText());
                int sb = Integer.parseInt(shortBreakField.getText());
                int lb = Integer.parseInt(longBreakField.getText());

                if (p <= 0 || sb <= 0 || lb <= 0) {
                    throw new NumberFormatException("Values must be positive.");
                }
            } catch (NumberFormatException e) {
                // Show error and consume event to prevent closing
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Invalid Input");
                alert.setContentText("Please enter valid positive integer values for all fields.");
                alert.showAndWait();
                event.consume();
            }
        });

        // Show dialog and wait for response
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Save new durations in seconds
                sessionDurations[0] = Integer.parseInt(pomodoroField.getText()) * 60;
                sessionDurations[1] = Integer.parseInt(shortBreakField.getText()) * 60;
                sessionDurations[2] = Integer.parseInt(longBreakField.getText()) * 60;

                // Reset current session to apply new durations
                resetSession();

                // Feedback
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setHeaderText(null);
                info.setContentText("Pomodoro timer settings updated.");
                info.showAndWait();
            }
        });
    }
}
