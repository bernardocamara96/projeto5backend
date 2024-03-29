package aor.paj.dao;

import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {

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

		} catch (NoResultException e) {
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
}
