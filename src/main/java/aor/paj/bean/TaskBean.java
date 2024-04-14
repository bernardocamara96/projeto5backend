package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.StatisticsDto;
import aor.paj.dto.TaskDto;
import aor.paj.dto.User;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.taskStatusManager;
import aor.paj.service.status.userRoleManager;
import aor.paj.service.validator.TaskValidator;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskBean is an application-scoped bean that manages task operations, including reading from and writing to a JSON file,
 * 'allTasks.json'. It supports creating, retrieving, updating, and deleting tasks, as well as retrieving all tasks for a
 * specific user. The bean sorts tasks based on priority, start date, and end date for user-specific queries. It utilizes
 * Jsonb for JSON processing, ensuring tasks are persistently stored and managed efficiently. This bean plays a crucial
 * role in task management within the application, providing a centralized point for task data manipulation and retrieval.
 */

@Stateless
public class TaskBean{
    @EJB
    TaskDao taskDao;
    @EJB
    UserBean userBean;
    @EJB
    CategoryDao categoryDao;
    @Inject
    TaskValidator taskValidator;
    @EJB
    UserDao userDao;

    private TaskEntity convertTaskDtotoTaskEntity(TaskDto taskDto){
        TaskEntity taskEntity=new TaskEntity();
        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setStartDate(taskDto.getStartDate());
        taskEntity.setEndDate(taskDto.getEndDate());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setPriority(taskDto.getPriority());

        return taskEntity;
    }

    public TaskDto convertTaskEntitytoTaskDto(TaskEntity taskEntity){
        TaskDto taskDto=new TaskDto();
        taskDto.setDescription(taskEntity.getDescription());
        taskDto.setId(taskEntity.getId());
        taskDto.setStatus(taskEntity.getStatus());
        taskDto.setTitle(taskEntity.getTitle());
        taskDto.setCategory_type(taskEntity.getCategory().getType());
        taskDto.setPriority(taskEntity.getPriority());
        taskDto.setUsername_author(taskEntity.getUser().getUsername());
        taskDto.setStartDate(taskEntity.getStartDate());
        taskDto.setEndDate(taskEntity.getEndDate());
        taskDto.setDeleted(taskEntity.isDeleted());
        return taskDto;
    }
    public boolean addTask(String token,String type,TaskDto taskDto) {
        if(token==null || type==null || taskDto==null) return false;
        UserEntity userEntity=userBean.getUserByToken(token);
        TaskEntity taskEntity=convertTaskDtotoTaskEntity(taskDto);
        CategoryEntity categoryEntity=categoryDao.findCategoryByType(type);
        if(categoryEntity!=null) {
            taskEntity.setUser(userEntity);
            taskEntity.setCategory(categoryEntity);
            taskEntity.setTitle(taskDto.getTitle());
            taskEntity.setDescription(taskDto.getDescription());
            taskEntity.setStatus(100);
            taskEntity.setDeleted(false);
            taskEntity.setCreationDate(LocalDateTime.now());
            if(taskDto.getStartDate()!=null) {
                taskEntity.setStartDate(taskDto.getStartDate());
            }
            if(taskDto.getEndDate()!=null) {
                taskEntity.setEndDate(taskDto.getEndDate());
            }
            taskDao.persist(taskEntity);
            return true;
        }
        else return false;
    }

    public TaskEntity getTaskById(int task_id){
        return taskDao.findTaskById(task_id);
    }

    public boolean taskIdValidator(int task_id){
        if(taskDao.findTaskById(task_id)==null) return false;
        else return true;
    }


    public boolean taskDeletePermission(String token){
        UserEntity userEntity=userDao.findUserByToken(token);
        if(userEntity!=null) {
            if (userEntity.getRole().equals( userRoleManager.PRODUCT_OWNER)) {
                return true;
            }
            else return false;
        }
        else return false;
    }

    public boolean editTask(int id, TaskDto taskDto){
        if(taskDto == null || id < 0) return false;
        if(taskValidator.validateTask(taskDto) && categoryDao.findCategoryByType(taskDto.getCategory_type())!=null){
            TaskEntity taskEntity=taskDao.findTaskById(id);
            if(taskEntity==null) return false;
            taskEntity.setCategory(categoryDao.findCategoryByType(taskDto.getCategory_type()));
            taskEntity.setTitle(taskDto.getTitle());
            taskEntity.setDescription(taskDto.getDescription());
            taskEntity.setPriority(taskDto.getPriority());
            if(taskDto.getEndDate()!=null){
                taskEntity.setEndDate(taskDto.getEndDate());
            }
            if(taskDto.getStartDate()!=null){
                taskEntity.setStartDate(taskDto.getStartDate());
            }
            taskDao.merge(taskEntity);
            return true;
        }
        return false;
    }

    public ArrayList<TaskEntity> getAllTasks(){
        return taskDao.getAllTasks();
    }

    public ArrayList<TaskEntity> getAllTasksByUsername(String username){
        UserEntity user=userDao.findUserByUsername(username);

        return taskDao.getTasksByUser(user);
    }

    public ArrayList<TaskEntity> getAllTasksByCategory(String category_type){
        return taskDao.getTasksByCategory(categoryDao.findCategoryByType(category_type));
    }

    public boolean deleteTaskPermanently(int id){
        if(taskDao.findTaskById(id)==null) return false;
        taskDao.deleteTask(id);
        return true;
    }

    public boolean deleteTemporarily(int id){
        TaskEntity taskEntity=taskDao.findTaskById(id);
        if(taskEntity==null) return false;
        if(!taskEntity.isDeleted() ){
            taskEntity.setDeleted(true);
            taskDao.merge(taskEntity);
            return true;
        }
        else return false;
    }

    public ArrayList<TaskEntity> getAllTasksByDeleted(boolean deleted){
        return taskDao.getTasksByDeleted(deleted);
    }

    public boolean recycleTask(int id){
        TaskEntity taskEntity=taskDao.findTaskById(id);
        if (taskEntity.isDeleted()) {
            taskEntity.setDeleted(false);
            taskDao.merge(taskEntity);
            return true;
        }
        else return false;

    }

    public void deleteAllTasksByUser(UserEntity user){
        ArrayList<TaskEntity> tasksEntities=getAllTasksByUsername(user.getUsername());
        for (TaskEntity task:tasksEntities){
            task.setDeleted(true);
            taskDao.merge(task);
        }
    }

    public boolean validateStatus(int status){
        if(status==100 || status==200 || status==300){
            return true;
        }
        else return false;
    }
    public boolean changeStatus(int status, int id){

           TaskEntity taskEntity=taskDao.findTaskById(id);
           if(taskEntity.getStatus()!=status) {
               if(status==taskStatusManager.DONE){
                   taskEntity.setLastDoneDate(LocalDateTime.now());
               }
               taskEntity.setStatus(status);
               taskDao.merge(taskEntity);
               return true;
           }
        else return false;
    }
    public ArrayList<TaskDto> getTaskByUsernameAndCategory(String category_type, String username){
        CategoryEntity category=categoryDao.findCategoryByType(category_type);
        UserEntity user=userDao.findUserByUsername(username);
        ArrayList<TaskEntity> tasksEntities=taskDao.getTasksByCategoryAndUser(category,user);
        ArrayList<TaskDto> tasksDtos=new ArrayList<>();
        for(TaskEntity task:tasksEntities){
            if(!task.isDeleted()) {
                tasksDtos.add(convertTaskEntitytoTaskDto(task));
            }
        }
        return tasksDtos;
    }

    public ArrayList<TaskDto> getTasksByUsernameAndStatus(String username,String status, boolean deleted){
        UserEntity user=userDao.findUserByUsername(username);
        if(user!=null) {
            int statusInt=Integer.parseInt(status);
            ArrayList<TaskEntity> taskEntities = taskDao.getTasksByStatusAndUser(statusInt, user, deleted);
            ArrayList<TaskDto> tasksDtos = new ArrayList<>();
            try {
                for (TaskEntity task : taskEntities) {
                    tasksDtos.add(convertTaskEntitytoTaskDto(task));
                }
                return tasksDtos;
            }
            catch(NullPointerException e){
                return tasksDtos;
            }
        }
        else return null;
    }

    public ArrayList<TaskDto> getTasksByUsernameAndDeleted(String username,boolean deleted){
        UserEntity user=userDao.findUserByUsername(username);
            ArrayList<TaskEntity> taskEntities = taskDao.getTasksByUserAndDeleted(user, deleted);
            ArrayList<TaskDto> tasksDtos = new ArrayList<>();
            try {
                for (TaskEntity task : taskEntities) {
                    tasksDtos.add(convertTaskEntitytoTaskDto(task));
                }
                return tasksDtos;
            }
            catch(NullPointerException e){
                return tasksDtos;
            }

    }

    /*Tests setters*/
    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    ////DASHBOARD////

    public double averageTasksByUserAndSetConfirmedUsers(StatisticsDto statisticsDto){
        List<UserEntity> users=userDao.findAllUsers();
        int tasksNumber=0;
        int confirmedUsersSize= userBean.countConfirmedUsers();
        statisticsDto.setConfirmedUsers(confirmedUsersSize);
        try {
            for (UserEntity user : users) {
                if(!user.getUsername().equals("deletedTasks")) {
                    tasksNumber += taskDao.countTaskByUser(user);
                }
            }
            return  Math.round((tasksNumber / (double)confirmedUsersSize)* 100.0) / 100.0;
        }catch (ArithmeticException e){
            return 0;
        }
    }

    public int tasksNumberByStatus(int status){
        try{
            return taskDao.countTasksByStatus(status);
        }catch(Exception e){
            return 0;
        }
    }

    public int tasksNumberByCategory(CategoryEntity category){
        try{
            return taskDao.countTaskByCategory(category);
        }catch (Exception e){
            return 0;
        }
    }

    public double calculateTaskAverageConclusionTime(){
        return Math.round(taskDao.calculateAverageConclusionTime() * 100.0) / 100.0;
    }

    public int[][] calculateConclusionsByDayAndHour() {
        // Get conclusion times and app start time from DAO
        List<LocalDateTime> conclusionTimes = taskDao.getAllConclusionDates();
        LocalDateTime appStartTime = userDao.getRegisterDate("admin");

        // Calculate the total number of days since the app started
        long totalDays = Duration.between(appStartTime, LocalDateTime.now()).toDays();

        // Initialize the 2D array to hold counts for each day and hour since the app started
        int[][] cumulativeTasksByHour = new int[(int) (totalDays + 1)][24];

        int totalCumulativeTasks=0;

        for (LocalDateTime conclusionTime : conclusionTimes) {
            // Calculate the difference in days between conclusion time and app start time
            long daysSinceStart = Duration.between(appStartTime, conclusionTime).toDays();
            // Calculate the hour within the day
            int hour = conclusionTime.getHour();
            // Increment the count for the current day and hour
            if (daysSinceStart >= 0 && daysSinceStart <= totalDays) {
                cumulativeTasksByHour[(int) daysSinceStart][hour]++;
            }
        }

        // Adjust counts to make them cumulative
        for (int i = 0; i <= totalDays; i++) {
            for (int j = 0; j < 24; j++) {
                totalCumulativeTasks+= cumulativeTasksByHour[i][j];
                cumulativeTasksByHour[i][j] = totalCumulativeTasks;
            }
        }

        return cumulativeTasksByHour;
    }

//    public int getLastTaskIdCreated(){
//        int maxId = 0;
//        for(TaskDto t: tasks){
//            if(t.getId() > maxId) maxId = t.getId();
//        }
//        return maxId + 1;
//    }
//
//    public TaskDto getTask(int id) {
//        for (TaskDto a : tasks) {
//            if (a.getId() == id)
//                return a;
//        }
//        return null;
//    }
//
//    public ArrayList<TaskDto> getAllTasks() {
//        return tasks;
//    }
//
//    public ArrayList<TaskDto> getAllTasksByUser(String username) {
//        ArrayList<TaskDto> listOfUserTasks = new ArrayList<>();
//        for(TaskDto t : tasks){
//            if(t.getUsername_author().equals(username)) listOfUserTasks.add(t);
//        }
//        return orderTasksByPriorityStartAndEndDate(listOfUserTasks);
//    }
//    public boolean removeTask(int id) {
//        for (TaskDto a : tasks) {
//            if (a.getId() == id) {
//                tasks.remove(a);
//                writeIntoJsonFile();
//                return true;
//            }
//        }
//        return false;
//    }
//    public ArrayList<TaskDto> orderTasksByPriorityStartAndEndDate(ArrayList<TaskDto> tasks) {
//        TaskDto temp;
//        for (int i = 0; i < tasks.size(); i++) {
//            for (int j = i + 1; j < tasks.size(); j++) {
//                boolean toChange = false;
//
//                TaskDto taskI = tasks.get(i);
//                TaskDto taskJ = tasks.get(j);
//                if (taskI.getPriority() < taskJ.getPriority()) {
//                    toChange = true;
//                }
//                else if (taskI.getPriority() == taskJ.getPriority() &&
//                        ((taskI.getStartDate() == null && taskJ.getStartDate() != null) ||
//                                (taskI.getStartDate() != null && taskJ.getStartDate() != null &&
//                                        taskI.getStartDate().isAfter(taskJ.getStartDate())))) {
//                    toChange = true;
//                }
//                else if (taskI.getPriority() == taskJ.getPriority() &&
//                        ((taskI.getStartDate() == null && taskJ.getStartDate() == null) ||
//                                (taskI.getStartDate() != null && taskJ.getStartDate() != null &&
//                                        taskI.getStartDate().isEqual(taskJ.getStartDate()))) &&
//                        ((taskI.getEndDate() == null && taskJ.getEndDate() != null) ||
//                                (taskI.getEndDate() != null && taskJ.getEndDate() != null &&
//                                        taskI.getEndDate().isAfter(taskJ.getEndDate())))) {
//                    toChange = true;
//                }
//                if (toChange) {
//                    temp = tasks.get(i);
//                    tasks.set(i, tasks.get(j));
//                    tasks.set(j, temp);
//                }
//            }
//        }
//        return tasks;
//    }
//
//    public void updateTask(int id, TaskUpdate taskUpdate) {
//        for (TaskDto task : tasks) {
//            if (task.getId() == id) {
//                // Atualiza os campos da tarefa, exceto o ID
//                if (taskUpdate.getTitle() != null && !taskUpdate.getTitle().isEmpty()) {
//                    task.setTitle(taskUpdate.getTitle());
//                }
//                if (taskUpdate.getDescription() != null && !taskUpdate.getDescription().isEmpty()) {
//                    task.setDescription(taskUpdate.getDescription());
//                }
//                if (taskUpdate.getPriority() != null) { // Assume 0 como valor não válido
//                    task.setPriority(taskUpdate.getPriority());
//                }
//                if (taskUpdate.getStatus() != 0) { // Assume 0 como valor não válido
//                    task.setStatus(taskUpdate.getStatus());
//                }
//                if (taskUpdate.getStartDate() != null) {
//                    task.setStartDate(taskUpdate.getStartDate());
//                }
//                if (taskUpdate.getEndDate() != null) {
//                    task.setEndDate(taskUpdate.getEndDate());
//                }
//
//                writeIntoJsonFile();
//                return;
//            }
//        }
//    }
//
//    private void writeIntoJsonFile() {
//        Jsonb jsonb = JsonbBuilder.create(new
//                JsonbConfig().withFormatting(true));
//        try {
//            jsonb.toJson(tasks, new FileOutputStream(filename));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
}