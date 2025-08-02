package lab.visual.pomodoroapp.model;

public class Task {
    private String name;
    private int pomodorosCompleted;
    private int estimatedPomodoros;

    public Task(String name, int pomodorosNeeded) {
        this.name = name;
        this.pomodorosCompleted = 0;
        this.estimatedPomodoros = pomodorosNeeded;
    }

    public String getName() { return name; }
    public int getPomodorosCompleted() { return pomodorosCompleted; }
    public int getEstimatedPomodoros() { return estimatedPomodoros; }
    public void incrementPomodoros() { pomodorosCompleted++; }
    public void setEstimatedPomodoros(int estimatedPomodoros) {this.estimatedPomodoros = estimatedPomodoros;}
    @Override
    public String toString() { return name + " (" + pomodorosCompleted + ")"; }

    public void setName(String newName) {
        name = newName;
    }
}
