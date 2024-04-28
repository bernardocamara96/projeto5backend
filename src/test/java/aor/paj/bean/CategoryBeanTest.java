package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.CategoryStatsDto;
import aor.paj.dto.TaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CategoryBeanTest {

    @InjectMocks
    private CategoryBean categoryBean;

    @Mock
    private CategoryDao categoryDao;
    @Mock
    private TaskDao taskDao;
    @Mock
    private TaskBean taskBean;
    @Mock
    private UserBean userBean;

    private UserEntity testUser;
    private CategoryEntity testCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new UserEntity();
        testUser.setUsername("admin");

        testCategory = new CategoryEntity();
        testCategory.setType("No_Category");
        testCategory.setAuthor(testUser);

        when(userBean.getUserByUsername("admin")).thenReturn(testUser);
    }

    @Test
    void testAddCategory_Success() {
        when(categoryDao.findCategoryByType(anyString())).thenReturn(null);
        doAnswer(invocation -> {
            CategoryEntity category = invocation.getArgument(0);
            category.setId(1); // Simulando o comportamento do persist.
            return null; // Retorno null é aceitável aqui porque estamos em um contexto doAnswer para um método void.
        }).when(categoryDao).persist(any(CategoryEntity.class));

        CategoryDto result = categoryBean.addCategory(testUser, "Work");
        assertNotNull(result, "The result should not be null");
        assertEquals("Work", result.getType(), "The type of the category should be 'Work'");
    }


    @Test
    void testAddCategory_Failure_AlreadyExists() {
        when(categoryDao.findCategoryByType("Work")).thenReturn(testCategory);

        CategoryDto result = categoryBean.addCategory(testUser, "Work");
        assertNull(result, "The result should be null because the category already exists");
    }

    @Test
    void testEditCategory_Success() {
        when(categoryDao.findCategoryByType("Old_Category")).thenReturn(testCategory);
        when(categoryDao.findCategoryByType("New_Category")).thenReturn(null);

        boolean result = categoryBean.editCategory("New_Category", "Old_Category");
        assertTrue(result, "The category should be updated successfully");
    }

    @Test
    void testEditCategory_Failure_AlreadyExists() {
        when(categoryDao.findCategoryByType("Old_Category")).thenReturn(testCategory);
        when(categoryDao.findCategoryByType("New_Category")).thenReturn(new CategoryEntity());

        boolean result = categoryBean.editCategory("New_Category", "Old_Category");
        assertFalse(result, "The category should not be updated because the new category name already exists");
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryDao.findCategoryByType("Work")).thenReturn(testCategory);

        boolean result = categoryBean.deleteCategory("Work");
        assertTrue(result, "The category should be deleted successfully");
        verify(categoryDao, times(1)).deleteCategory("Work");
    }

    @Test
    void testDeleteCategory_Failure_NotFound() {
        when(categoryDao.findCategoryByType("Non_Existent")).thenReturn(null);

        boolean result = categoryBean.deleteCategory("Non_Existent");
        assertFalse(result, "The category should not be deleted because it does not exist");
        verify(categoryDao, never()).deleteCategory(anyString());
    }

    @Test
    void testGetAllCategories_Success() {
        List<CategoryEntity> categories = new ArrayList<>();
        categories.add(testCategory);
        when(categoryDao.getAllCategories()).thenReturn(new ArrayList<>(categories));

        List<CategoryEntity> result = categoryBean.getAllCategories();
        assertNotNull(result, "The result should not be null");
        assertFalse(result.isEmpty(), "The result list should not be empty");
        assertEquals(1, result.size(), "The result list should contain one category");
        assertEquals(testCategory, result.get(0), "The category in the result list should match the test category");
    }

    @Test
    void testOrdenedCategoriesList() {
        CategoryEntity category1 = new CategoryEntity("Category1", userBean.getUserByUsername("admin"));
        CategoryEntity category2 = new CategoryEntity("Category2", userBean.getUserByUsername("admin"));
        CategoryEntity category3 = new CategoryEntity("Category3", userBean.getUserByUsername("admin"));

        ArrayList<CategoryEntity> categories = new ArrayList<>();
        categories.add(category1);
        categories.add(category2);
        categories.add(category3);


        when(categoryBean.getAllCategories()).thenReturn(categories);
        when(taskBean.tasksNumberByCategory(category1)).thenReturn(10);
        when(taskBean.tasksNumberByCategory(category2)).thenReturn(5);
        when(taskBean.tasksNumberByCategory(category3)).thenReturn(8);

        ArrayList<CategoryStatsDto> orderedCategories = categoryBean.ordenedCategoriesList();

        assertEquals(10, orderedCategories.get(0).getTasksNumber()); // First category should have tasks number 10
        assertEquals(8, orderedCategories.get(1).getTasksNumber()); // Second category should have tasks number 8
        assertEquals(5, orderedCategories.get(2).getTasksNumber()); // Third category should have tasks number 5

    }
}
