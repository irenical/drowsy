package org.irenical.drowsy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

public class DrowsyTesty {

  private static PostgresConfig postgresConfig;
  private static PostgresProcess postgresProcess;

  private static Connection createConnection() throws SQLException, IOException {
    String user = System.getProperty("user.name");
    String url = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", postgresConfig.net().host(), postgresConfig.net().port(), postgresConfig.storage().dbName(), user, null);
    Connection connection = DriverManager.getConnection(url);
    connection.setAutoCommit(false);
    return connection;
  }

  @BeforeClass
  public static void init() throws ClassNotFoundException, SQLException, IOException {
    Class.forName("org.postgresql.Driver");
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    postgresConfig = PostgresConfig.defaultWithDbName("test");
    PostgresExecutable exec = runtime.prepare(postgresConfig);
    postgresProcess = exec.start();

    Connection connection = createConnection();

    // create table
    PreparedStatement createPeopleTableStatement = connection.prepareStatement("create table people (id serial primary key, name text)");
    createPeopleTableStatement.executeUpdate();
    createPeopleTableStatement.close();

    // create data
    PreparedStatement createPersonStatement = connection.prepareStatement("insert into people(name) values('Boda')");
    createPersonStatement.executeUpdate();
    createPersonStatement.close();

    connection.commit();
    connection.close();
  }

  @AfterClass
  public static void shutdown() {
    postgresProcess.stop();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoConnection() throws SQLException {
    DrowsyTransaction<String> transaction = new DrowsyTransaction<>(c -> {
      return "";
    });
    transaction.execute(null);
  }

  @Test
  public void testEmptyTransaction() throws SQLException, IOException {
    DrowsyTransaction<String> transaction = new DrowsyTransaction<>(c -> {
      return "";
    });
    transaction.execute(createConnection());
  }

  @Test
  public void testConnectionClosed() throws SQLException, IOException {
    DrowsyTransaction<String> transaction = new DrowsyTransaction<>(c -> {
      return "";
    });
    Connection c = createConnection();
    transaction.execute(c);
    Assert.assertTrue(c.isClosed());
  }

  @Test
  public void testResourcesClosed() throws SQLException, IOException {
    PreparedStatement[] ps = new PreparedStatement[2];
    ResultSet[] rs = new ResultSet[2];
    DrowsyTransaction<String> transaction = new DrowsyTransaction<>(c -> {
      ps[0] = c.prepareStatement("select 1");
      ps[1] = c.prepareStatement("select 2");
      rs[0] = ps[0].executeQuery();
      rs[1] = ps[1].executeQuery();
      rs[0].next();
      rs[1].next();
      return rs[0].getString(1) + rs[1].getString(1);
    });

    Connection c = createConnection();
    String got = transaction.execute(c);
    Assert.assertEquals("12", got);
    Assert.assertTrue(ps[0].isClosed());
    Assert.assertTrue(ps[1].isClosed());
    Assert.assertTrue(rs[0].isClosed());
    Assert.assertTrue(rs[1].isClosed());
    Assert.assertTrue(c.isClosed());
  }

}
