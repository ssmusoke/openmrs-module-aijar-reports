package org.openmrs.module.ugandaemrreports;

import org.junit.Before;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

import java.util.Properties;

@SkipBaseSetup
public abstract class StandaloneContextSensitiveTest extends BaseModuleContextSensitiveTest {
	
	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}
	
	/**
	 * @return MS Note: use port 3306 as standard, 5538 for sandbox 5.5 mysql environment
	 */
	@Override
	public Properties getRuntimeProperties() {
		System.setProperty("databaseUrl", "jdbc:mysql://localhost:3306/openmrs?autoReconnect=true&sessionVariables=storage_engine%3DInnoDB&useUnicode=true&characterEncoding=UTF-8");
		System.setProperty("databaseUsername", "openmrs");
		System.setProperty("databasePassword", "openmrs");
		System.setProperty("databaseDriver", "com.mysql.jdbc.Driver");
		System.setProperty("databaseDialect", "org.hibernate.dialect.MySQLDialect");

		return super.getRuntimeProperties();
	}
	
	@Before
	public void setupForTest() throws Exception {
		if (!Context.isSessionOpen()) {
			Context.openSession();
		}
		Context.clearSession();
		authenticate();
	}
	
	@Override
	public void deleteAllData() throws Exception {
	}


}