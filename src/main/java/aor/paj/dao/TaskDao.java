package aor.paj.dao;

import aor.paj.dto.User;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class TaskDao extends AbstractDao<TaskEntity> {

	private static final long serialVersionUID = 1L;

	public TaskDao() {
		super(TaskEntity.class);
	}
	

	public TaskEntity findTaskById(int id) {
		try {
			return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}

	}

	public ArrayList<TaskEntity> getAllTasks(){
		try {
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.getAllTasks").getResultList();
		}catch (Exception e){
			return null;
		}
	}

	public ArrayList<TaskEntity> getTasksByUser(UserEntity user) {
		try {

			return  (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByUser").setParameter("user", user).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> getTasksByCategory(CategoryEntity category){
		try{
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByCategory").setParameter("category",category).getResultList();
		}catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> getTasksByDeleted(boolean deleted){
		try{
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByDeleted").setParameter("deleted",deleted).getResultList();
		}catch (Exception e) {
			return null;
		}
	}

	public void deleteTask(int id){
		try{
			em.createNamedQuery("Task.deleteTasksBId").setParameter("id",id).executeUpdate();
		}catch (Exception e){
		}
	}
	public ArrayList<TaskEntity> getTasksByCategoryAndUser(CategoryEntity category, UserEntity user){
		try{

			TypedQuery<TaskEntity> query = em.createNamedQuery("Task.findTasksByCategoryAndUser", TaskEntity.class);
			query.setParameter("category", category);
			query.setParameter("user", user);
			return (ArrayList<TaskEntity>) query.getResultList();
		}catch (Exception e){
			return null;
		}
	}

	public ArrayList<TaskEntity> getTasksByStatusAndUser(int status, UserEntity user, boolean deleted) {
		try {
			TypedQuery<TaskEntity> query = em.createNamedQuery("Task.findTasksByStatusAndUser", TaskEntity.class);
			query.setParameter("status",status);
			query.setParameter("user",user);
			query.setParameter("deleted",deleted);
			return (ArrayList<TaskEntity>) query.getResultList();
		}catch (Exception e){
			return null;
		}
	}

	public ArrayList<TaskEntity> getTasksByUserAndDeleted(UserEntity user, boolean deleted) {
		try {
			TypedQuery<TaskEntity> query = em.createNamedQuery("Task.findTasksByUserAndDeleted", TaskEntity.class);
			query.setParameter("user",user);
			query.setParameter("deleted",deleted);
			return (ArrayList<TaskEntity>) query.getResultList();
		}catch (Exception e){
			return null;
		}
	}

	public int countTaskByUser(UserEntity user) {
		try {
			return ((Number)em.createNamedQuery("Task.countTasksByUser").setParameter("user",user).getSingleResult()).intValue();

		}catch (Exception e){
			return 0;
		}
	}

	public int countTasksByStatus(int status) {
		try {
			return ((Number)em.createNamedQuery("Task.countTasksByStatus").setParameter("status",status).getSingleResult()).intValue();

		}catch (Exception e){
			return 0;
		}
	}

	public int countTaskByCategory(CategoryEntity category) {
		try {
			return ((Number)em.createNamedQuery("Task.countTasksByCategory").setParameter("category",category).getSingleResult()).intValue();

		}catch (Exception e){
			return 0;
		}
	}

	public double calculateAverageConclusionTime() {
		try {
			return (double) em.createNamedQuery("Task.calculateAverageConclusionTime")
					.getSingleResult();
		} catch (Exception e) {
			// Log or handle the exception appropriately
			e.printStackTrace(); // Log the exception
			return 0;
		}
	}

	public List<LocalDateTime> getAllConclusionDates(){
		try {
			return  em.createNamedQuery("Task.findAllLastDoneDates").getResultList();
		}catch ( Exception e){
			return null;
		}
    }

}
