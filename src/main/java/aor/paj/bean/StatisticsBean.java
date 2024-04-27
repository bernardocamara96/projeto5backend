package aor.paj.bean;

import aor.paj.dao.UserDao;
import aor.paj.dto.StatisticsDto;
import aor.paj.service.status.taskStatusManager;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Stateless
public class StatisticsBean {
    @EJB
    TaskBean taskBean;
    @EJB
    UserBean userBean;
    @EJB
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
            statisticsDto.setAverageConclusionTime(taskBean.calculateTaskAverageConclusionTime());
            statisticsDto.setNumberOfUsersRegisterByHour(userBean.calculateUsersByHour());
            statisticsDto.setCumulativeTasksNumberByHour(taskBean.calculateConclusionsByDayAndHour());
            statisticsDto.setAppHoursArray(everyAppHour());

            return true;
        }catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e){
            return false;
        }
    }
    public String[] everyAppHour(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime appCreationDate = userDao.getRegisterDate("admin");
        LocalDateTime presentDate = LocalDateTime.now();

        appCreationDate = appCreationDate.toLocalDate().atStartOfDay();

        long totalDays = appCreationDate.until(presentDate, java.time.temporal.ChronoUnit.DAYS) + 1;

        String[] datesArray = new String[(int)totalDays];

        for (int i = 0; i < totalDays; i++) {
            datesArray[i] = appCreationDate.plusDays(i).format(formatter);
        }
        return datesArray;

    }
}
