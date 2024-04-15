package aor.paj.bean;

import aor.paj.dao.UserDao;
import aor.paj.dto.StatisticsDto;
import aor.paj.service.status.taskStatusManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Stateless
public class StatisticsBean {
    @Inject
    TaskBean taskBean;
    @Inject
    UserBean userBean;
    @Inject
    CategoryBean categoryBean;
    @Inject
    UserDao userDao;

    public boolean setStatistics(StatisticsDto statisticsDto){
        try {
            statisticsDto.setAverageTasksNumberByUser(taskBean.averageTasksByUserAndSetConfirmedUsers(statisticsDto));
            statisticsDto.setNotConfirmedUsers(userBean.countNotConfirmedUsers());
            int[] tasksNumberByStatus = new int[3];
            tasksNumberByStatus[0] = (taskBean.tasksNumberByStatus(taskStatusManager.TODO));
            tasksNumberByStatus[1] = (taskBean.tasksNumberByStatus(taskStatusManager.DOING));
            tasksNumberByStatus[2] = (taskBean.tasksNumberByStatus(taskStatusManager.DONE));
            statisticsDto.setTasksNumberByState(tasksNumberByStatus);
            statisticsDto.setCategoriesList(categoryBean.ordenedCategoriesList());
            statisticsDto.setAverageConclusionTime(taskBean.calculateTaskAverageConclusionTime());
            statisticsDto.setNumberOfUsersRegisterByHour(userBean.calculateUsersByHour());
            statisticsDto.setCumulativeTasksNumberByHour(taskBean.calculateConclusionsByDayAndHour());
            statisticsDto.setAppHoursArray(everyAppHour());

            return true;
        }catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e){
            return false;
        }
    }
    public LocalDateTime[] everyAppHour(){
        LocalDateTime appCreationDate = userDao.getRegisterDate("admin");// Replace this with the actual app creation date
        LocalDateTime presentDate = LocalDateTime.now(); // Current date and time

        appCreationDate = appCreationDate.plusHours(1).withMinute(0).withSecond(0).withNano(0);

        // Calculate the total number of hours between app creation date and present date
        long totalHours = appCreationDate.until(presentDate, java.time.temporal.ChronoUnit.HOURS);

        // Initialize an array to store hours
        LocalDateTime[] hoursArray = new LocalDateTime[(int)totalHours+1];

        // Populate the array with each hour from creation date to present date
        for (int i = 0; i <= totalHours; i++) {
            hoursArray[i] = appCreationDate.plusHours(i);
        }
        return hoursArray;

    }
}
