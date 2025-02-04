package com.integration.demo.util;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.integration.demo.dto.UserDTO;


@WebListener
public final class HibernateUtil implements ServletContextListener {

	/**
	 * Location of hibernate.cfg.xml file. Location should be on the classpath as
	 * Hibernate uses #resourceAsStream style lookup for its configuration file. The
	 * default classpath location of the hibernate config file is in the default
	 * package. Use #setConfigFile() to update the location of the configuration
	 * file for the current session.
	 */
	private static final ThreadLocal<Session> THREAD_LOCAL = new ThreadLocal<>();
	private static Configuration configuration = new Configuration();
	private static SessionFactory sessionFactory;
	private static Properties objProperties = new Properties();

	@Override
	public void contextInitialized(ServletContextEvent event) {
		System.out.print("Loading Hibernate properties");
		URL url = HibernateUtil.class.getClassLoader().getResource("Hibernate.properties");
		objProperties = getHibernateProperties(url);
	}

	/**
	 * Returns the ThreadLocal Session instance. Lazy initialize the
	 * <code>SessionFactory</code> if needed.
	 *
	 * @return Session
	 * @throws HibernateException
	 */
	public static Session getSession() throws HibernateException {
		Session session = THREAD_LOCAL.get();

		if (session == null || !session.isOpen() || !session.isJoinedToTransaction()) {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (Exception e) {
					e.getMessage();
				}
			}
			if (sessionFactory == null) {
				rebuildSessionFactory();
			}
			session = sessionFactory != null ? sessionFactory.openSession() : null;

			if (session != null) {
				System.out.println("Giving connection: isConnected: " + session.isConnected() + ",  isDirty: "
						+ session.isDirty() + ", isOpen: " + session.isOpen());
			} else {
				System.out.println("$$$$$$ SESSION IS NULL");
			}
			THREAD_LOCAL.set(session);
		}
		return session;
	}
	
	public static void closeSession() throws HibernateException {
		Session session = THREAD_LOCAL.get();
		THREAD_LOCAL.remove();
		if (session != null) {
			session.close();
		}
	}
	
	/**
	 * return session factory
	 *
	 */
	public static org.hibernate.SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * return hibernate configuration
	 *
	 */
	public static Configuration getConfiguration() {
		return configuration;
	}
	
	public static void rebuildSessionFactory() {
		try {
			System.out.println(objProperties);
			Configuration configuration = new Configuration().addProperties(objProperties)
					.addAnnotatedClass(UserDTO.class);
			sessionFactory = configuration.addPackage("com.integration.demo.dto").buildSessionFactory();
			System.out.println("Configuring session factory....DONE");
		} catch (Exception e) {
			System.out.println("%%%% Error Creating SessionFactory %%%%"+ e);
		}
	}
	
	public static Properties getHibernateProperties(URL hibernateUrl) {
		Properties hibernateProperties = new Properties();
		try (final InputStream hibernateFile = hibernateUrl.openStream();) {
			hibernateProperties.load(hibernateFile);
			System.out.println("********* Project config file loaded");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return hibernateProperties;
	}
}
