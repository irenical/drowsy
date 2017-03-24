package org.irenical.drowsy.transaction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
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

public class TransactionTest {

	protected static PostgresProcess postgresProcess;
	protected static PostgresConfig postgresConfig;

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

	@Test(expected = IllegalArgumentException.class)
	public void testNoConnectionTransaction() throws SQLException {
		new JdbcTransaction<String>() {
			@Override
			public String execute(Connection t) throws SQLException {
				return null;
			}
		}.run((Connection) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoConnectionOperation() throws SQLException {
		new JdbcOperation<String>() {
			@Override
			public String execute(Connection t) throws SQLException {
				return null;
			}
		}.run((Connection) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoDataSourceTransaction() throws SQLException {
		new JdbcTransaction<String>() {
			@Override
			public String execute(Connection t) throws SQLException {
				return null;
			}
		}.run((DataSource) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoDataSourceOperation() throws SQLException {
		new JdbcOperation<String>() {
			@Override
			public String execute(Connection t) throws SQLException {
				return null;
			}
		}.run((DataSource) null);
	}

	@Test
	public void testEmptyOperation() throws SQLException, IOException {
		new JdbcOperation<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				return null;
			}
		}.run(createConnection(true));
	}

	@Test
	public void testEmptyTransaction() throws SQLException, IOException {
		new JdbcTransaction<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				return null;
			}
		}.run(createConnection(false));
	}

	@Test
	public void testConnectionClosed() throws SQLException, IOException {
		Connection c = createConnection(true);
		new JdbcOperation<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				return null;
			}
		}.run(c);
		Assert.assertTrue(c.isClosed());
	}

	@Test
	public void testResourcesClosed() throws SQLException, IOException {
		PreparedStatement[] ps = new PreparedStatement[2];
		ResultSet[] rs = new ResultSet[2];
		JdbcTransaction<String> transaction = new JdbcTransaction<String>() {
			@Override
			protected String execute(Connection c) throws SQLException {
				ps[0] = c.prepareStatement("select 1");
				ps[1] = c.prepareStatement("select 2");
				rs[0] = ps[0].executeQuery();
				rs[1] = ps[1].executeQuery();
				rs[0].next();
				rs[1].next();
				return rs[0].getString(1) + rs[1].getString(1);
			}
		};
		Connection c = createConnection(false);
		String got = transaction.run(c);
		Assert.assertEquals("12", got);
		Assert.assertTrue(rs[0].isClosed());
		Assert.assertTrue(rs[1].isClosed());
		Assert.assertTrue(ps[0].isClosed());
		Assert.assertTrue(ps[1].isClosed());

		Assert.assertTrue(c.isClosed());
	}

	@Test(expected = SQLException.class)
	public void testAutoCommitTransaction() throws SQLException, IOException {
		new JdbcTransaction<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				return null;
			}
		}.run(createConnection(true));
	}

	@Test
	public void testReuseConnection() throws SQLException, IOException {
		Connection[] c = new Connection[1];
		c[0] = createConnection(true);
		new JdbcOperation<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				Assert.assertNotEquals(System.identityHashCode(connection), System.identityHashCode(c[0]));
				c[0] = connection;
				return null;
			}
		}.run(c[0]);
		new JdbcOperation<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				Assert.assertEquals(System.identityHashCode(connection), System.identityHashCode(c[0]));
				c[0] = connection;
				return null;
			}
		}.run(c[0]);
	}

	@Test
	public void testNonAutoCommitOperation() throws SQLException, IOException {
		JdbcOperation<Integer> insert = new JdbcOperation<Integer>() {
			@Override
			protected Integer execute(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("insert into people(name) values('Moleman')");
				return ps.executeUpdate();
			}
		};
		JdbcOperation<String> select = new JdbcOperation<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("select name from people where name=?");
				ps.setString(1, "Moleman");
				ResultSet rs = ps.executeQuery();
				return rs.next() ? rs.getString(1) : null;
			}
		};
		insert.run(createConnection(false));
		Assert.assertNull(select.run(createConnection(false)));
	}

	@Test
	public void testTransactionCommit() throws SQLException, IOException {
		JdbcTransaction<Integer> insert = new JdbcTransaction<Integer>() {
			@Override
			protected Integer execute(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("insert into people(name) values('Moleman')");
				return ps.executeUpdate();
			}
		};
		JdbcOperation<String> select = new JdbcOperation<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("select name from people where name=?");
				ps.setString(1, "Moleman");
				ResultSet rs = ps.executeQuery();
				return rs.next() ? rs.getString(1) : null;
			}
		};
		JdbcOperation<Integer> delete = new JdbcOperation<Integer>() {
			@Override
			protected Integer execute(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("delete from people where name=?");
				ps.setString(1, "Moleman");
				return ps.executeUpdate();
			}
		};
		insert.run(createConnection(false));
		Assert.assertNotNull(select.run(createConnection(true)));
		delete.run(createConnection(true));
		Assert.assertNull(select.run(createConnection(true)));
	}

	@Test
	public void testTransactionRollback() throws SQLException, IOException {
		JdbcTransaction<Integer> insert = new JdbcTransaction<Integer>() {
			@Override
			protected Integer execute(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("insert into people(name) values('Moleman')");
				ps.executeUpdate();
				ps = connection.prepareStatement("insert onto peepal(neime) valuez('Moleman')");
				return ps.executeUpdate();
			}
		};
		JdbcOperation<String> select = new JdbcOperation<String>() {
			@Override
			protected String execute(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("select name from people where name=?");
				ps.setString(1, "Moleman");
				ResultSet rs = ps.executeQuery();
				return rs.next() ? rs.getString(1) : null;
			}
		};
		try {
			insert.run(createConnection(false));
			Assert.fail();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Assert.assertNull(select.run(createConnection(true)));
	}

}
