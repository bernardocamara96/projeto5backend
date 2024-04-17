package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.CategoryStatsDto;
import aor.paj.dto.StatisticsDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.userRoleManager;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.util.ArrayList;


@Stateless
public class CategoryBean implements Serializable {

    @EJB
    CategoryDao categoryDao;
    @EJB
    UserBean userBean;
    @EJB
    TaskBean taskBean;

    public CategoryDto convertCategoryEntitytoCategoryDto(CategoryEntity categoryEntity){
        CategoryDto categoryDto=new CategoryDto();
        categoryDto.setId(categoryEntity.getId());
        categoryDto.setOwner_username(categoryEntity.getAuthor().getUsername());
        categoryDto.setType(categoryEntity.getType());
        return  categoryDto;
    }

    public CategoryStatsDto convertCategoryEntitytoCategoryStatsDto(CategoryEntity categoryEntity){
        CategoryStatsDto categoryStatsDto=new CategoryStatsDto();
        categoryStatsDto.setId(categoryEntity.getId());
        categoryStatsDto.setOwner_username(categoryEntity.getAuthor().getUsername());
        categoryStatsDto.setType(categoryEntity.getType());
        categoryStatsDto.setTasksNumber(taskBean.tasksNumberByCategory(categoryEntity));
        return  categoryStatsDto;
    }
    public void createDefaultCategoryIfNotExistent(){
        CategoryEntity defaultCategory = categoryDao.findCategoryByType("No_Category");

        if(defaultCategory == null){
            categoryDao.persist(new CategoryEntity("No_Category",userBean.getUserByUsername("admin") ));

        }
    }

    public CategoryDto addCategory(UserEntity user, String type){
        if(categoryDao.findCategoryByType(type)==null) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setType(type);
            categoryEntity.setAuthor(user);
            categoryDao.persist(categoryEntity);
            return convertCategoryEntitytoCategoryDto(categoryEntity);
        }
        else return null;
    }

    public ArrayList<CategoryEntity> getAllCategories(){
        return categoryDao.getAllCategories();
    }


    public boolean editCategory(String newType, String oldType){
        if(categoryDao.findCategoryByType(newType)==null) {
            CategoryEntity categoryEntity = categoryDao.findCategoryByType(oldType);
            categoryEntity.setType(newType);

            categoryDao.merge(categoryEntity);
            return true;
        }
        return false;
    }

    public boolean deleteCategory(String category_type){
        if(categoryDao.findCategoryByType(category_type)!=null){
            categoryDao.deleteCategory(category_type);

            return true;
        } else return false;
    }
    public boolean categoryTypeValidator(String type){
        if(categoryDao.findCategoryByType(type)!=null) return true;
        else return false;
    }

    public boolean categoryWithTasks(String type){

        if(taskBean.getAllTasksByCategory(type).size()>0){
            return true;
        }
        else return false;
    }

    public ArrayList<CategoryStatsDto> ordenedCategoriesList(){
        ArrayList<CategoryEntity> categories=getAllCategories();
        ArrayList<CategoryStatsDto> categoriesDto=new ArrayList<>();

        int n = categories.size();

        for(CategoryEntity category:categories){
            categoriesDto.add(convertCategoryEntitytoCategoryStatsDto(category));
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (categoriesDto.get(j).getTasksNumber() < categoriesDto.get(j + 1).getTasksNumber()) {
                    // Swap list[j] and list[j+1]
                    CategoryStatsDto temp = categoriesDto.get(j);
                    categoriesDto.set(j, categoriesDto.get(j + 1));
                    categoriesDto.set(j + 1, temp);
                }
            }
        }


        return categoriesDto;
    }
}
