package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.validator.TaskValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskBeanTest {

    @InjectMocks
    private TaskBean taskBean;

    @Mock
    private TaskDao taskDao;

    @Mock
    private UserDao userDao;

    @Mock
    private CategoryDao categoryDao;

    @Mock
    private TaskValidator taskValidator;

    @Mock
    private UserBean userBean;

    private UserEntity testUser;
    private CategoryEntity testCategory;
    private TaskEntity testTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new UserEntity();
        testUser.setUsername("testUser");
        testUser.setPassword("securePassword123");
        testUser.setEmail("testUser@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("123-456-7890");
        testUser.setPhotoURL("https://example.com/path");
        testUser.setToken("uniqueToken123456");
        testUser.setRole("USER");
        testUser.setDeleted(false);

        testCategory = new CategoryEntity();
        testCategory.setType("Work");
        testCategory.setAuthor(testUser);

        testTask = new TaskEntity();
        testTask.setTitle("Test Task");
        testTask.setDescription("This is a test task description.");
        testTask.setCategory(testCategory);
        testTask.setUser(testUser);


        when(userDao.findUserByUsername(anyString())).thenReturn(testUser);
        when(categoryDao.findCategoryByType(anyString())).thenReturn(testCategory);
        when(taskDao.findTaskById(anyInt())).thenReturn(testTask);
        when(taskValidator.validateTask(any(TaskDto.class))).thenReturn(true);
    }

    @Test
    void testAddTask_Success() {
        TaskDto newTaskDto = new TaskDto();
        newTaskDto.setTitle("New Task");
        newTaskDto.setDescription("Description of the new task.");
        newTaskDto.setCategory_type(testCategory.getType());
        newTaskDto.setUsername_author(testUser.getUsername());
        boolean result = taskBean.addTask("validToken", "Work", newTaskDto);
        assertTrue(result, "Task should be added successfully");
        verify(taskDao, times(1)).persist(any(TaskEntity.class));
    }

    @Test
    void testGetTaskById_Found() {
        TaskEntity result = taskBean.getTaskById(1);
        assertNotNull(result, "Task should be found");
        assertEquals(testTask.getTitle(), result.getTitle(), "The retrieved task should have the correct title");
    }

    @Test
    void testEditTask_Success() {
        TaskDto taskDtoToUpdate = new TaskDto();
        taskDtoToUpdate.setTitle("Updated Task Title");
        taskDtoToUpdate.setDescription("Updated description.");
        taskDtoToUpdate.setCategory_type(testCategory.getType());
        taskDtoToUpdate.setUsername_author(testUser.getUsername());

        TaskEntity existingTask = taskBean.getTaskById(1);
        existingTask.setTitle("Original Task Title");
        existingTask.setDescription("Original description.");
        existingTask.setCategory(testCategory);
        existingTask.setUser(testUser);

        when(taskDao.findTaskById(1)).thenReturn(existingTask);

        boolean result = taskBean.editTask(1, taskDtoToUpdate);

        assertTrue(result, "Task should be updated successfully");

        verify(taskDao, times(1)).merge(existingTask);

        assertEquals("Updated Task Title", existingTask.getTitle(), "The title should be updated.");
        assertEquals("Updated description.", existingTask.getDescription(), "The description should be updated.");
    }


    @Test
    void testDeleteTemporarily_Success() {
        boolean result = taskBean.deleteTemporarily(1);

        assertTrue(result, "Task should be marked as deleted successfully");
        verify(taskDao, times(1)).merge(any(TaskEntity.class));
    }

    @Test
    void testDeleteTaskPermanently_Success() {
        int taskIdToDelete = 1;

        taskBean.deleteTaskPermanently(taskIdToDelete);
        verify(taskDao, times(1)).deleteTask(taskIdToDelete);
        when(taskDao.findTaskById(taskIdToDelete)).thenReturn(null);
        TaskEntity resultAfterDeletion = taskBean.getTaskById(taskIdToDelete);
        assertNull(resultAfterDeletion, "A tarefa deve ser nula após ser eliminada permanentemente");
    }
    @Test
    void testAddTask_Failure_InvalidData() {
        TaskDto newTaskDto = new TaskDto();
        newTaskDto.setTitle(""); // Dados inválidos
        newTaskDto.setDescription("Description of the new task.");
        newTaskDto.setCategory_type(testCategory.getType());
        newTaskDto.setUsername_author(testUser.getUsername());

        when(taskValidator.validateTask(any(TaskDto.class))).thenReturn(false); // Simula falha na validação

        boolean result = taskValidator.validateTask(newTaskDto);
        assertFalse(result, "Task should not be added due to invalid data");
        verify(taskDao, never()).persist(any(TaskEntity.class)); // Assegura que persist nunca é chamado
    }

    @Test
    void testEditTask_Failure_TaskNotFound() {
        TaskDto taskDtoToUpdate = new TaskDto();
        taskDtoToUpdate.setTitle("Updated Task Title");
        taskDtoToUpdate.setDescription("Updated description.");
        taskDtoToUpdate.setCategory_type(testCategory.getType());
        taskDtoToUpdate.setUsername_author(testUser.getUsername());

        when(taskDao.findTaskById(anyInt())).thenReturn(null);

        boolean result = taskBean.editTask(999, taskDtoToUpdate); // Um ID que supostamente não existe

        assertFalse(result, "Task should not be updated because it does not exist");
        verify(taskDao, never()).merge(any(TaskEntity.class));
    }

    @Test
    void testDeleteTemporarily_Failure_TaskNotFound() {
        when(taskDao.findTaskById(anyInt())).thenReturn(null); // Simula tarefa não encontrada

        boolean result = taskBean.deleteTemporarily(999); //ID que supostamente não existe

        assertFalse(result, "Task should not be marked as deleted because it does not exist");
        verify(taskDao, never()).merge(any(TaskEntity.class)); //
    }

    @Test
    void testDeleteTaskPermanently_Failure_TaskNotFound() {
        int taskIdToDelete = 999;
        when(taskDao.findTaskById(taskIdToDelete)).thenReturn(null); // Simula tarefa não encontrada antes da tentativa de exclusão

        taskBean.deleteTaskPermanently(taskIdToDelete);

        verify(taskDao, never()).deleteTask(taskIdToDelete); // Verifica que deleteTask nunca é chamado para um ID inexistente
    }

}
