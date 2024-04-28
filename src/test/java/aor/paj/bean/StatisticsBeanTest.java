package aor.paj.bean;


import aor.paj.dao.UserDao;
import aor.paj.dto.StatisticsDto;
import aor.paj.service.status.taskStatusManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsBeanTest {
    @InjectMocks
    private StatisticsBean statisticsBean;

    @Mock
    private TaskBean taskBean;
    @Mock
    private UserBean userBean;

    @Mock
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetStatistics_Failure() {
        // Mock data for StatisticsDto
        StatisticsDto statisticsDto = new StatisticsDto();

        // Mock behavior of TaskBean and UserBean methods to throw exceptions
        when(taskBean.averageTasksByUserAndSetConfirmedUsers(statisticsDto)).thenThrow(new NullPointerException());
        when(userBean.countNotConfirmedUsers()).thenThrow(new NullPointerException());
        when(taskBean.tasksNumberByStatus(any(Integer.class))).thenThrow(new NullPointerException());
        when(taskBean.calculateTaskAverageConclusionTime()).thenThrow(new NumberFormatException());

        // Call the method under test
        boolean result = statisticsBean.setStatistics(statisticsDto);

        // Verify that the method returns false
        assertFalse(result);
    }

}