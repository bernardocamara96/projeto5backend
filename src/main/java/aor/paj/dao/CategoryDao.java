package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;

@Stateless
public class CategoryDao extends AbstractDao<CategoryEntity>{
    private static final long serialVersionUID = 1L;

    public CategoryDao() {super(CategoryEntity.class);}

    public CategoryEntity findCategoryByType(String type) {
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryByType").setParameter("type", type)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public CategoryEntity findCategoryById(int id) {
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<CategoryEntity> getAllCategories(){
        try {
            return (ArrayList<CategoryEntity>) em.createNamedQuery("Category.getAllCategories").getResultList();
        }catch (Exception e){
            return null;
        }
    }

    public ArrayList<CategoryEntity> getCategoriesByUser(UserEntity author) {
        try {

            return  (ArrayList<CategoryEntity>) em.createNamedQuery("Category.getAllCategoriesByAuthor").setParameter("author", author).getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteCategory(String type){
        try{
            em.createNamedQuery("Category.deleteCategoryByType").setParameter("type",type).executeUpdate();
        }catch (Exception e){
        }
    }
}
