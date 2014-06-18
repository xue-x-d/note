package com.shomop.crm.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

@ContextConfiguration(locations = "classpath:/application.xml")
@TransactionConfiguration(defaultRollback = true)
public class BaseTestCase extends AbstractTransactionalJUnit4SpringContextTests {
	// applicationContext
	// logger
	// dbcTemplate
	
	private static boolean inited = false;

	@BeforeClass
	public static void init() {
		try {
			if (!inited) {
				System.out.println("Initializing running ...");
				
				inited = true;
			} else {
				System.out.println("inited: " + inited);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void destory() {
		System.out.println("test finished.");
	}
	
	@Autowired
	protected SessionFactory sessionFactory;
	
    /**
     * available to subclasses.
     */
	protected void flushSession() {
		Session session = sessionFactory.getCurrentSession();
		if (session != null) {
			session.flush();
		}
	}
	
	protected Session getCurrentSession(){
		
		return sessionFactory.getCurrentSession();
	}
	
}
