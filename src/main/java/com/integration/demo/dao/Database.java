package com.integration.demo.dao;

import java.io.Serializable;

/**
 * Copyright 2021 Connfido B.V. All rights reserved.
 * Distribution of this software without explicit written consent from Connfido is
 * strictly prohibited. No part of this software is to be reverse engineered,
 * copied, reproduced or modified.
 *
 * Class Name:Database.java Abstract: This file prepares database queries and
 * set named parameters
 *
 * @author  Connfido B.V.
 */

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

import com.integration.demo.util.HibernateUtil;

import lombok.extern.log4j.Log4j2;

public final class Database {
	private static final String MAX_RESULT = "MAX_RESULT";
	private static final String STARTING_PAGE = "STARTING_PAGE";
	private static Map<String, Object> mapNull = null;

	/**
	 * Instantiates a new database.
	 */
	private Database() {
	}

	public static <T extends Object> List<T> getSelectResult(String strQuery, long lngValue) throws Exception {
		String strKeyName = getKeyword(strQuery);
		Map<String, Object> mapKeyValues = getKeyValues(strKeyName, lngValue);
		return getSelectResult(strQuery, mapKeyValues);
	}

	public static <T extends Object> List<T> getSelectResult(String query, String value) throws Exception {
		return getSelectResult(query, value, -1);
	}

	public static <T extends Object> List<T> getSelectResult(String query) throws Exception {
		return getSelectResult(query, mapNull);
	}

	/**
	 * Gets the key and values from the query.
	 *
	 * @param strKeyName the string key name
	 * @param objValue   the object value
	 * @return the key value map
	 */
	private static Map<String, Object> getKeyValues(String strKeyName, Object objValue) {
		Map<String, Object> mapKeyValues = new HashMap<>(1);
		mapKeyValues.put(strKeyName, objValue);
		return mapKeyValues;
	}

	/**
	 * Gets the query result.
	 *
	 * @param strQuery     the string query
	 * @param strValue     the string key value
	 * @param intMaxResult the int max result count
	 * @return the list of entities
	 * @throws Exception the database exception
	 */
	private static <T extends Object> List<T> getSelectResult(String strQuery, Object strValue, int intMaxResult)
			throws Exception {
		String strKeyName = getKeyword(strQuery);
		Map<String, Object> mapKeyValues = getKeyValues(strKeyName, strValue);
		mapKeyValues.put(MAX_RESULT, intMaxResult);
		return getSelectResult(strQuery, mapKeyValues);
	}

	/**
	 * Gets the query result.
	 *
	 * @param query        the string query
	 * @param mapKeyValues the map key values of named parameters
	 * @return the list of entities
	 * @throws Exception the database exception
	 */
	public static <T extends Object> List<T> getSelectResult(String query, Map<String, Object> mapKeyValues)
			throws Exception {
		System.out.println(query + "...[" + mapKeyValues + "]");
		List<T> lstResult = new ArrayList<>();
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Query objQuery = session.createQuery(query);
			if (mapKeyValues != null && !mapKeyValues.isEmpty()) {
				objQuery = updateObjQuery(mapKeyValues, objQuery);
			}
			lstResult = objQuery.getResultList();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult;
	}

	public static <T> T getById(Class<T> entityClass, Serializable id) throws Exception {
		System.out.println(entityClass + "...[" + id + "]");
		T lstResult = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			lstResult = (T) session.get(entityClass, id);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult;
	}

	public static <T> List<T> getByIds(Class<T> entityClass, List<Serializable> ids) throws Exception {
		System.out.println(entityClass + "...[" + ids + "]");
		List<T> lstResult = new ArrayList<>();
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			lstResult = session.byMultipleIds(entityClass).multiLoad(ids);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult;
	}

	public static <T> List<T> getByParamInList(Class<T> entityClass, String paramName,
			Collection<? extends Serializable> paramValues) throws Exception {
		System.out.println(entityClass + "...[" + paramName + ":" + paramValues + "]");
		List<T> lstResult = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = builder.createQuery(entityClass);
			Root<T> root = criteriaQuery.from(entityClass);
			In<Serializable> inCriteria = builder.in(root.get(paramName));
			paramValues.forEach(inCriteria::value);
			criteriaQuery.select(root).where(inCriteria);
			Query query = session.createQuery(criteriaQuery);
			lstResult = query.getResultList();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult;
	}

	public static <T> T getByParam(Class<T> entityClass, String paramName, Serializable paramValue, Order order)
			throws Exception {
		System.out.println(entityClass + "...[" + paramName + ":" + paramValue + "]");
		List<T> lstResult = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = builder.createQuery(entityClass);
			Root<T> root = criteriaQuery.from(entityClass);
			criteriaQuery.select(root).where(builder.equal(root.get(paramName), paramValue));
			updateForOrder(order, builder, criteriaQuery, root);
			Query query = session.createQuery(criteriaQuery);
			query.setMaxResults(1);
			lstResult = query.getResultList();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult != null && !lstResult.isEmpty() ? lstResult.get(0) : null;
	}

	public static <T> List<T> getAllByParam(Class<T> entityClass, String paramName, Serializable paramValue,
			Order order) throws Exception {
		System.out.println(entityClass + "...[" + paramName + ":" + paramValue + "]");
		List<T> lstResult = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = builder.createQuery(entityClass);
			Root<T> root = criteriaQuery.from(entityClass);
			criteriaQuery.select(root).where(builder.equal(root.get(paramName), paramValue));
			updateForOrder(order, builder, criteriaQuery, root);
			Query query = session.createQuery(criteriaQuery);
			lstResult = query.getResultList();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult;
	}

	public static <T> T getByParams(Class<T> entityClass, Map<String, ? extends Object> params, Order order)
			throws Exception {
		System.out.println(entityClass + "...[" + params.toString() + "]");
		List<T> lstResult = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = builder.createQuery(entityClass);
			Root<T> root = criteriaQuery.from(entityClass);
			List<Predicate> filters = new ArrayList<>();
			params.entrySet().forEach(entry -> {
				if (entry.getValue() instanceof Collection<?>) {
					filters.add(root.get(entry.getKey()).in((Collection) entry.getValue()));
				} else {
					filters.add(builder.equal(root.get(entry.getKey()), entry.getValue()));
				}
			});
			criteriaQuery.select(root).where(filters.toArray(new Predicate[0]));
			updateForOrder(order, builder, criteriaQuery, root);
			Query query = session.createQuery(criteriaQuery);
			query.setMaxResults(1);
			lstResult = query.getResultList();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult != null && !lstResult.isEmpty() ? lstResult.get(0) : null;
	}

	public static <T> List<T> getAllByParams(Class<T> entityClass, Map<String, ? extends Object> params, Order order)
			throws Exception {
		System.out.println(entityClass + "...[" + params.toString() + "]");
		List<T> lstResult = null;
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = builder.createQuery(entityClass);
			Root<T> root = criteriaQuery.from(entityClass);
			List<Predicate> filters = new ArrayList<>();
			params.entrySet().forEach(entry -> {
				if (entry.getValue() instanceof Collection<?>) {
					filters.add(root.get(entry.getKey()).in((Collection) entry.getValue()));
				} else {
					filters.add(builder.equal(root.get(entry.getKey()), entry.getValue()));
				}
			});
			criteriaQuery.select(root).where(filters.toArray(new Predicate[0]));
			updateForOrder(order, builder, criteriaQuery, root);
			Query query = session.createQuery(criteriaQuery);
			lstResult = query.getResultList();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult;
	}

	/**
	 * Gets the query result.
	 *
	 * @param query        the string query
	 * @param mapKeyValues the map key values of named parameters
	 * @return the list of entities
	 * @throws Exception the database exception
	 */
	public static <T extends Object> List<T> getHealthCheck() throws Exception {
		List<T> lstResult = new ArrayList<>();
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Query objQuery = session.createNativeQuery("SELECT 1");
			lstResult = objQuery.getResultList();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			flushSession(session);
		}
		return lstResult;
	}

	/**
	 * This method update the entity based on query.
	 *
	 * @param mapKeyValues the map key values of named parameters
	 * @param targetQuery  the target query
	 * @return the query entity
	 */
	private static Query updateObjQuery(Map<String, Object> mapKeyValues, Query targetQuery) {
		Query objQuery = targetQuery;
		Set<Map.Entry<String, Object>> entries = mapKeyValues.entrySet();
		for (Map.Entry<String, Object> entry : entries) {
			String strKeyName = entry.getKey();

			if (strKeyName.equals(STARTING_PAGE) || strKeyName.equals(MAX_RESULT)) {
				Object strValue = entry.getValue();
				setFilters(strValue, objQuery, strKeyName);
			} else if (entry.getValue() instanceof Long) {
				objQuery.setParameter(strKeyName, (Long) entry.getValue());
			} else {
				getUdatedQuery(strKeyName, objQuery, mapKeyValues);
			}
		}
		return objQuery;
	}

	private static Query getUdatedQuery(String strKeyName, Query objQuery, Map<String, Object> mapKeyValues) {
		if (mapKeyValues.get(strKeyName) instanceof Integer) {
			objQuery.setParameter(strKeyName, (Integer) mapKeyValues.get(strKeyName));
		} else if (mapKeyValues.get(strKeyName) instanceof Timestamp) {
			objQuery.setParameter(strKeyName, (Timestamp) mapKeyValues.get(strKeyName));
		} else if (mapKeyValues.get(strKeyName) instanceof Date) {
			objQuery.setParameter(strKeyName, (Date) mapKeyValues.get(strKeyName));
		} else if (mapKeyValues.get(strKeyName) instanceof java.sql.Date) {
			objQuery.setParameter(strKeyName, (java.sql.Date) mapKeyValues.get(strKeyName));
		} else if (mapKeyValues.get(strKeyName) instanceof Collection) {
			objQuery.setParameter(strKeyName, (Collection) mapKeyValues.get(strKeyName));
		} else {
			objQuery.setParameter(strKeyName, (String) mapKeyValues.get(strKeyName));
		}
		return objQuery;
	}

	private static <T> void updateForOrder(Order order, CriteriaBuilder builder, CriteriaQuery<T> criteriaQuery,
			Root<T> root) {
		if (order == null) {
			return;
		}
		if (order.isAscending()) {
			criteriaQuery.orderBy(builder.asc(root.get(order.getPropertyName())));
		} else {
			criteriaQuery.orderBy(builder.desc(root.get(order.getPropertyName())));
		}
	}

	/**
	 * Sets the query filters.
	 *
	 * @param strValue   the string value
	 * @param objQuery   the object query
	 * @param strKeyName the string key name
	 * @return the query entity
	 */
	private static Query setFilters(Object strValue, Query objQuery, String strKeyName) {
		if (Integer.parseInt(strValue.toString()) <= 0) {
			return objQuery;
		}
		if (strKeyName.equals(STARTING_PAGE)) {
			objQuery.setFirstResult(Integer.parseInt(strValue.toString()));
			System.out.println("Value set: {}"+ strValue);
		} else {
			objQuery.setMaxResults(Integer.parseInt(strValue.toString()));
		}
		return objQuery;
	}

	/**
	 * Flush session of current Transaction.
	 *
	 * @param session the session entity
	 */
	@Transactional
	static void flushSession(Session session) {

		System.out.println("Flushing the session...");
		if (session != null && session.isOpen() && session.isJoinedToTransaction()) {
			try {
				System.out.println("1 Flushing the session... {}"+ session.isOpen());
				session.flush();
				System.out.println("2 Flushing the session... {}"+ session.isOpen());
				session.clear();
				System.out.println("3 Flushing the session... {}"+ session.isOpen());
			} catch (Exception e) {
				System.out.println("Error while closing the connection."+ e);
				session.close();
			}
		}
	}

	/**
	 * Gets the keyword from query.
	 *
	 * @param strQuery the string query
	 * @return the string keyword value
	 */
	private static String getKeyword(String strQuery) {
		String strKeyword = null;
		int intParamIndex = strQuery.indexOf(':');
		if (intParamIndex >= 0) {
			String strRemainingQry = strQuery.substring(intParamIndex + 1, strQuery.length());
			int intNextIndex = strRemainingQry.indexOf(' ');
			if (intNextIndex < 0) {
				intNextIndex = strRemainingQry.length();
			}
			strKeyword = strRemainingQry.substring(0, intNextIndex);
		}
		return strKeyword;
	}

	/**
	 * Close hibernate session.
	 */
	static void closeSession(Session session) {
		session.close();
	}

	public static void closeConnection() {
		HibernateUtil.closeSession();
	}

}
