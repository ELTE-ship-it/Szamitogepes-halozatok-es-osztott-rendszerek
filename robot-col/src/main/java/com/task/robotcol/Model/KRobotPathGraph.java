package com.task.robotcol.Model;

import javafx.event.EventHandler;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;

public class KRobotPathGraph {
    private final int pathLength;
    private final ArrayList<RobotTask> tasks;
    private final ArrayList<Robot> robots;
    private ArrayList<RobotTask>[] robotTasks;

    private final RobotTaskManager robotTaskManager = new RobotTaskManager();
    private final RobotManager robotManager = new RobotManager();
    public KRobotPathGraph(int pathLength, ArrayList<RobotTask> tasks, ArrayList<Robot> robots) {
        this.pathLength = pathLength;
        this.tasks = tasks;
        this.robots = robots;
        robotTaskManager.sortRobotTaskByIndex(tasks);
        devideTasksForRobots();
    }

    public KRobotPathGraph(int pathLength, Map<Integer, Integer> tasksWithLength, ArrayList<Integer> robotIndexes) {
        //TODO: ellenőrizni, hogy a pathLength-hez megfelelnak a robot és task indexek, 0-tól indexelek
        this.pathLength = pathLength;
        this.robots = new ArrayList<>();
        this.tasks = new ArrayList<>();
        for(int i = 0; i< robotIndexes.size(); i++) {
            robots.add(new Robot(robotIndexes.get(i), i));
        }
        int taskNum = 0;
        for (Map.Entry<Integer, Integer> task : tasksWithLength.entrySet()) {
            tasks.add(new RobotTask(task.getKey(), taskNum, task.getValue()));
            taskNum++;
        }
        robotTaskManager.sortRobotTaskByIndex(tasks);
        devideTasksForRobots();
    }

    /*private void devideTasksForRobots() {
        //TODO: ez ugye meg nem jo, mert az elso robotnak odadja az osszes taskot
        robots.getFirst().setTasks(tasks);
        oneRobot(robots.getFirst());
    }*/
    private void writeAssignmentsToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Log initial task positions
            writer.write("Initial Task Positions:\n");
            for (RobotTask task : tasks) {
                writer.write("Task " + task.getTaskNum() + " is at index " + task.getIndex() + " with length " + task.getLength() + "\n");
            }
            writer.write("\nTask Assignments:\n");

            // Log task assignments
            for (int i = 0; i < robots.size(); i++) {
                Robot robot = robots.get(i);
                writer.write("Robot " + robot.getRobotNum() + " has been assigned the following tasks:\n");
                for (RobotTask assignedTask : robot.getTasks()) {
                    writer.write("- Task " + assignedTask.getTaskNum() + " at index " + assignedTask.getIndex() + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void devideTasksForRobots() {
        tasks.sort(Comparator.comparingInt(RobotTask::getIndex)); // Rendezés pozíció szerint
        ArrayList<RobotTask>[] robotTasks = new ArrayList[robots.size()];
        for (int i = 0; i < robots.size(); i++) {
            robotTasks[i] = new ArrayList<>();
        }

        // Intervallumok létrehozása: feladatokat egyenletesen osztjuk el
        int tasksPerRobot = (int) Math.ceil((double) tasks.size() / robots.size());

        for (int i = 0; i < tasks.size(); i++) {
            int robotIndex = i / tasksPerRobot;
            if (robotIndex < robots.size()) {
                robotTasks[robotIndex].add(tasks.get(i));
            }
        }

        // Feladatok kiosztása a robotokhoz
        for (int i = 0; i < robots.size(); i++) {
            robots.get(i).setTasks(robotTasks[i]);
        }
        writeAssignmentsToFile("task_assignments.txt");
    }
    //writeAssignmentsToFile("task_assignments.txt");


    public void makeAMove() {
        for(Robot robot : robots) {
            robot.makeAMove();
        }
    }

    private void oneRobot(Robot robot) {
        robot.setStartFromFirst(abs(robot.getStartIndex() - robot.getTasks().getLast().getIndex()) > abs(robot.getStartIndex() - robot.getTasks().getFirst().getIndex()));
    }

    public void setStepHandlers(EventHandler<RobotEventArgs> stepHandler) {
        for(Robot robot : robots) {
            robot.gameAdvanced = stepHandler;
        }
    }

    public int getPathLength() {
        return pathLength;
    }

    public Integer getRobotNum(int index) {
        Robot robot = robotManager.getRobotOnIndex(index, robots);
        return robot!=null ? Integer.valueOf(robot.getRobotNum()) : null;
    }

    public Integer getTaskNum(int index) {
        RobotTask task = robotTaskManager.getTaskOnIndex(index, tasks);
        return task!=null ? Integer.valueOf(task.getTaskNum()) : null;
    }

    public Integer getTaskLength(int index) {
        RobotTask task = robotTaskManager.getTaskOnIndex(index, tasks);
        return task!=null ? Integer.valueOf(task.getRemainingLength()) : null;
    }
}
