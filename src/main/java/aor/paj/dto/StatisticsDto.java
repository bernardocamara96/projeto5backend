package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;

import java.time.LocalDate;
import java.util.ArrayList;

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
    private ArrayList<CategoryStatsDto> categoriesList;
    @XmlElement
    private double averageConclusionTime;
    @XmlElement
    private int[] numberOfUsersRegisterByHour;
    @XmlElement
    private int[][] cumulativeTasksNumberByHour;

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

    public ArrayList<CategoryStatsDto> getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(ArrayList<CategoryStatsDto> categoriesList) {
        this.categoriesList = categoriesList;
    }

    public double getAverageConclusionTime() {
        return averageConclusionTime;
    }

    public void setAverageConclusionTime(double averageConclusionTime) {
        this.averageConclusionTime = averageConclusionTime;
    }


    public int[] getNumberOfUsersRegisterByHour() {
        return numberOfUsersRegisterByHour;
    }

    public void setNumberOfUsersRegisterByHour(int[] numberOfUsersRegisterByHour) {
        this.numberOfUsersRegisterByHour = numberOfUsersRegisterByHour;
    }

    public int[][] getCumulativeTasksNumberByHour() {
        return cumulativeTasksNumberByHour;
    }

    public void setCumulativeTasksNumberByHour(int[][] cumulativeTasksNumberByHour) {
        this.cumulativeTasksNumberByHour = cumulativeTasksNumberByHour;
    }
}
