package com.integration.demo.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.integration.demo.dto.UserDTO;
import com.integration.demo.util.HibernateUtil;


public class UserDAO {
	
	public static List<UserDTO> getAllUsers() throws Exception {
		String hql = "from UserDTO";
		List<UserDTO> userDTOs = Database.getSelectResult(hql);
		return userDTOs;
	}
	
	public static void addNewUser(UserDTO user) {
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();

		session.save(user);
		tx.commit();
		session.close();
	}
	
	public static UserDTO getUserByUserId(String userId) throws Exception {
		String hql = "from UserDTO where userId = :userId";
		List<UserDTO> userDTOs = Database.getSelectResult(hql, userId);
		UserDTO userDTO = !userDTOs.isEmpty() ? userDTOs.get(0) : null;
		return userDTO;
	}

}
