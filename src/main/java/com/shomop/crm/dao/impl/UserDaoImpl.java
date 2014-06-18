package com.shomop.crm.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.shomop.crm.dao.UserDao;
import com.shomop.crm.model.User;

@Repository("userDao")
public class UserDaoImpl extends GenericDaoImpl<User, String> implements UserDao {
	@Override
	public User findUserByName(String username) {
		Query query = getCurrentSession().createQuery("from User where username=:username");
		query.setString("username", username);
		return(User)query.uniqueResult();
	}

	@Override
	public <E> List<E> find(String sqlId, Object object, Class<E> clazz) {
		
		return null;
	}
}
