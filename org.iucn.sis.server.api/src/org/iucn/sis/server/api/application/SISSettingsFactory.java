package org.iucn.sis.server.api.application;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.connection.DriverManagerConnectionProvider;
import org.hibernate.transaction.TransactionFactory;
import org.hibernate.transaction.TransactionFactoryFactory;
import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.transaction.TransactionManagerLookupFactory;
import org.postgresql.Driver;

public class SISSettingsFactory extends SettingsFactory {

	private static final long serialVersionUID = 963768129170876992L;

	public Settings buildSettings(Properties props) {
		System.out.println("Right before building settings");
		try {
			return super.buildSettings(props);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		

	}

	@Override
	protected ConnectionProvider createConnectionProvider(Properties properties) {
		// System.out.println("this is properties " + properties.toString());
		// return ConnectionProviderFactory.newConnectionProvider(properties);

		DriverManagerConnectionProvider conP = new DriverManagerConnectionProvider() {

			protected String sis_url;
			protected Properties sis_props;
			protected Properties sis_conn_props;
			
			@Override
			public void configure(Properties props) throws HibernateException {
				System.out.println("in configure of driver manager connection provider");
				super.configure(props);
				sis_url = props.getProperty(Environment.URL);
				sis_props = props;
				sis_conn_props = ConnectionProviderFactory.getConnectionProperties(sis_props);
				System.out.println("in the catch of configure");

			}

			@Override
			public Connection getConnection() throws SQLException {
//				System.out.println("right before get connection");
//				Connection connection =  super.getConnection();
//				System.out.println("right after getting connection");
//				return connection;
				
				
				System.out.println("right before get connection");
				Driver driver = new Driver();
				System.out.println("the props are : " + sis_conn_props);
				Connection connection =  driver.connect(sis_url, sis_conn_props);
				System.out.println("right after getting connection");
				return connection;
				
			}
			
			
			
			
		};
		conP.configure(properties);
		return conP;

	}

	protected TransactionFactory createTransactionFactory(Properties properties) {
		return TransactionFactoryFactory.buildTransactionFactory(properties);
	}

	protected TransactionManagerLookup createTransactionManagerLookup(Properties properties) {
		return TransactionManagerLookupFactory.getTransactionManagerLookup(properties);
	}

	// private Dialect determineDialect(Properties props, String databaseName,
	// int databaseMajorVersion) {
	// return DialectFactory.buildDialect( props, databaseName,
	// databaseMajorVersion );
	// }

}
