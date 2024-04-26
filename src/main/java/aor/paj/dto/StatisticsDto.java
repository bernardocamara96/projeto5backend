package aor.paj.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.xml.bind.annotation.XmlElement;
import util.LocalDateTimeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class StatisticsDto {
    @XmlElement
    private int confirmedUsers;
    @XmlElement
    private int notConfirmedUsers;
    @XmlElement
    private double averageTasksNumberByUser;
    @XmlElement
    private int[] tasksNumberByState;

    @XmlElement
    private double averageConclusionTime;
    @XmlElement
    private ArrayList<Integer> numberOfUsersRegisterByHour;
    @XmlElement
    private ArrayList<Integer> cumulativeTasksNumberByHour;
    @XmlElement
    private String[] appHoursArray;



    public StatisticsDto() {

    }

    public int getConfirmedUsers() {
        return confirmedUsers;
    }

    public void setConfirmedUsers(int confirmedUsers) {
        this.confirmedUsers = confirmedUsers;
    }

    public int getNotConfirmedUsers() {
        return notConfirmedUsers;
    }

    public void setNotConfirmedUsers(int notConfirmedUsers) {
        this.notConfirmedUsers = notConfirmedUsers;
    }

    public double getAverageTasksNumberByUser() {
        return averageTasksNumberByUser;
    }

    public void setAverageTasksNumberByUser(double averageTasksNumberByUser) {
        this.averageTasksNumberByUser = averageTasksNumberByUser;
    }

    public int[] getTasksNumberByState() {
        return tasksNumberByState;
    }

    public void setTasksNumberByState(int[] tasksNumberByState) {
        this.tasksNumberByState = tasksNumberByState;
    }



    public double getAverageConclusionTime() {
        return averageConclusionTime;
    }

    public void setAverageConclusionTime(double averageConclusionTime) {
        this.averageConclusionTime = averageConclusionTime;
    }


    public ArrayList<Integer> getNumberOfUsersRegisterByHour() {
        return numberOfUsersRegisterByHour;
    }

    public void setNumberOfUsersRegisterByHour(ArrayList<Integer> numberOfUsersRegisterByHour) {
        this.numberOfUsersRegisterByHour = numberOfUsersRegisterByHour;
    }

    public ArrayList<Integer> getCumulativeTasksNumberByHour() {
        return cumulativeTasksNumberByHour;
    }

    public void setCumulativeTasksNumberByHour(ArrayList<Integer> cumulativeTasksNumberByHour) {
        this.cumulativeTasksNumberByHour = cumulativeTasksNumberByHour;
    }

    public String[] getAppHoursArray() {
        return appHoursArray;
    }

    public void setAppHoursArray(String[] appHoursArray) {
        this.appHoursArray = appHoursArray;
    }

    @Override
    public String toString() {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        String[] appHoursStrings = new String[appHoursArray.length];

        for (int i = 0; i < appHoursArray.length; i++) {
            appHoursStrings[i] = gson.toJson(appHoursArray[i]);
        }
        return "{" +
                "\"confirmedUsers\": " + confirmedUsers + "," +
                "\"notConfirmedUsers\": " + notConfirmedUsers + "," +
                "\"averageTasksNumberByUser\": " + averageTasksNumberByUser + "," +
                "\"tasksNumberByState\": " + Arrays.toString(tasksNumberByState) + "," +
                "\"averageConclusionTime\": " + averageConclusionTime + "," +
                "\"numberOfUsersRegisterByHour\": " + numberOfUsersRegisterByHour + "," +
                "\"cumulativeTasksNumberByHour\": " + cumulativeTasksNumberByHour + "," +
                "\"appHoursArray\": " + Arrays.toString(appHoursStrings) +
                "}";
    }


}
