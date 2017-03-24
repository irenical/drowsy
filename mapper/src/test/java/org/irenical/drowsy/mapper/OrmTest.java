package org.irenical.drowsy.mapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.irenical.drowsy.mapper.bean.LegitPerson;
import org.irenical.drowsy.mapper.bean.UnlegitPerson;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

public class OrmTest {

	private static PostgresProcess postgresProcess;
	private static PostgresConfig postgresConfig;

	@BeforeClass
	public static void startPg() throws ClassNotFoundException, IOException, SQLException {
		Class.forName("org.postgresql.Driver");
		PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
		postgresConfig = PostgresConfig.defaultWithDbName("test");
		PostgresExecutable exec = runtime.prepare(postgresConfig);
		postgresProcess = exec.start();

		String url = String.format("jdbc:postgresql://%s:%s/%s?", postgresConfig.net().host(),
				postgresConfig.net().port(), postgresConfig.storage().dbName());
		Config config = ConfigFactory.getConfig();
		String user = System.getProperty("user.name");
		config.setProperty("jdbc.jdbcUrl", url);
		config.setProperty("jdbc.username", user);
		config.setProperty("jdbc.password", null);

		Flyway flyway = new Flyway();
		flyway.setDataSource(url, user, null);
		flyway.migrate();
	}

	@AfterClass
	public static void shutdown() {
		postgresProcess.stop();
	}

	protected static Connection createConnection(boolean autocommit) throws SQLException, IOException {
		String user = System.getProperty("user.name");
		String url = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", postgresConfig.net().host(),
				postgresConfig.net().port(), postgresConfig.storage().dbName(), user, null);
		Connection connection = DriverManager.getConnection(url);
		connection.setAutoCommit(autocommit);
		return connection;
	}

	@Test
	public void testOk() throws SQLException, IOException, InstantiationException, IllegalAccessException {
		Connection c = createConnection(true);

		PreparedStatement ps = c.prepareStatement("select * from people order by id limit 2");
		ResultSet rs = ps.executeQuery();
		BeanMapper orm = new BeanMapper();

		List<LegitPerson> peeps = orm.map(rs, LegitPerson.class);
		Assert.assertTrue(peeps.size() > 1);
		LegitPerson doodarino = peeps.get(0);
		LegitPerson doodarina = peeps.get(1);
		System.out.println(doodarino.getId() < doodarina.getId());
	}

	@Test(expected = SQLException.class)
	public void testReflectionException()
			throws SQLException, IOException, InstantiationException, IllegalAccessException {
		Connection c = createConnection(true);

		PreparedStatement ps = c.prepareStatement("select * from people limit 1");
		ResultSet rs = ps.executeQuery();
		BeanMapper orm = new BeanMapper();

		orm.map(rs, UnlegitPerson.class);
	}

}
