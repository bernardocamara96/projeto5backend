package aor.paj.bean;

import aor.paj.dto.StatisticsDto;
import aor.paj.service.status.taskStatusManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.ArrayList;

@Stateless
public class StatisticsBean {
    @Inject
    TaskBean taskBean;
    @Inject
    UserBean userBean;
    @Inject
    CategoryBean categoryBean;

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
            return true;
        }catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e){
            return false;
        }
    }
}
