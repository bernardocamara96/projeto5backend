package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {

	List<String> excludedUsernames = new ArrayList<>(Arrays.asList("deletedTasks"));
	private static final long serialVersionUID = 1L;

	public UserDao() {
		super(UserEntity.class);
	}


	public UserEntity findUserByToken(String token) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByAuxiliarToken(String auxiliarToken) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByAuxiliarToken").setParameter("auxiliarToken", auxiliarToken)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
	public boolean updateUser(UserEntity user) {
		try {
			System.out.println(user);
			em.merge(user);
			System.out.println("entrou");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public List<UserEntity> findAllUsers() {
		try {
			return (List<UserEntity>) em.createNamedQuery("User.findAllUsers").getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}


	public UserEntity findUserByEmail(String email) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
	public UserEntity findUserByUsername(String username) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
					.getSingleResult();

		} catch (NoResultException | NullPointerException e) {
			return null;
		}
	}
	public boolean deleteUser(String username){
		try{
			em.createNamedQuery("User.deleteUserById").setParameter("username", username).executeUpdate();
			return true;
		}catch (Exception e){
			return false;
		}
	}

	public int countConfirmedUsers() {
		try {
			return  ((Number)em.createNamedQuery("User.countConfirmedUsers").setParameter("excludedUsernames",excludedUsernames).getSingleResult()).intValue();

		} catch (NoResultException e) {
			return 0;
		}
	}

	public int countNotConfirmedUsers() {
		try {
			return  ((Number)em.createNamedQuery("User.countNotConfirmedUsers").getSingleResult()).intValue();

		} catch (NoResultException e) {
			return 0;
		}
	}

	public List<LocalDateTime> getUsersRegisterDates() {
		try {
			return   em.createNamedQuery("User.findAllRegisterDates").setParameter("excludedUsernames",excludedUsernames).getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	public LocalDateTime getRegisterDate(String username) {
		try {
			return (LocalDateTime) em.createNamedQuery("User.findRegisterDateByUsername").setParameter("username", username)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public LocalDateTime getLastActivityDate(String token) {
		try {
			return (LocalDateTime) em.createNamedQuery("User.findLastActivityDateByToken").setParameter("token", token)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean setLastActivityDate(LocalDateTime lastActivityDate, String token){
		try{
		Query query = em.createNamedQuery("User.updateLastActivityDateByToken");
		query.setParameter("lastActivityDate", lastActivityDate);
		query.setParameter("token", token);

		int updatedRows = query.executeUpdate();

		if (updatedRows == 1) {
			return true;
		} else {
			return false;
		}
	} catch (Exception e) {
		e.printStackTrace(); // Handle the exception appropriately
		return false;
	}
	}

}
