package org.irenical.drowsy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

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

  private static Connection createConnection(boolean autocommit) throws SQLException, IOException {
    String user = System.getProperty("user.name");
    String url = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", postgresConfig.net().host(), postgresConfig.net().port(), postgresConfig.storage().dbName(), user, null);
    Connection connection = DriverManager.getConnection(url);
    connection.setAutoCommit(autocommit);
    return connection;
  }

  @BeforeClass
  public static void init() throws ClassNotFoundException, SQLException, IOException {
    Class.forName("org.postgresql.Driver");
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    postgresConfig = PostgresConfig.defaultWithDbName("test");
    PostgresExecutable exec = runtime.prepare(postgresConfig);
    postgresProcess = exec.start();

    Connection connection = createConnection(false);

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
    new JdbcOperation<String>(){
      @Override
      protected String execute(Connection connection) throws SQLException {
        return null;
      }
    }.run(createConnection(true));
  }
  
  @Test
  public void testEmptyTransaction() throws SQLException, IOException {
    new JdbcTransaction<String>(){
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
  
  @Test(expected=SQLException.class)
  public void testAutoCommitTransaction() throws SQLException, IOException {
    new JdbcTransaction<String>(){
      @Override
      protected String execute(Connection connection) throws SQLException {
        return null;
      }
    }.run(createConnection(true));
  }
  
  @Test
  public void testReuseConnection() throws SQLException, IOException {
    Connection [] c = new Connection[1];
    c[0] = createConnection(true);
    new JdbcOperation<String>(){
      @Override
      protected String execute(Connection connection) throws SQLException {
        Assert.assertNotEquals(System.identityHashCode(connection),System.identityHashCode(c[0]));
        c[0]=connection;
        return null;
      }
    }.run(c[0]);
    new JdbcOperation<String>(){
      @Override
      protected String execute(Connection connection) throws SQLException {
        Assert.assertEquals(System.identityHashCode(connection),System.identityHashCode(c[0]));
        c[0]=connection;
        return null;
      }
    }.run(c[0]);
  }
  
  @Test
  public void testNonAutoCommitOperation() throws SQLException, IOException {
    JdbcOperation<Integer> insert = new JdbcOperation<Integer>(){
      @Override
      protected Integer execute(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("insert into people(name) values('Moleman')");
        return ps.executeUpdate();
      }
    };
    JdbcOperation<String> select = new JdbcOperation<String>(){
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
    JdbcTransaction<Integer> insert = new JdbcTransaction<Integer>(){
      @Override
      protected Integer execute(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("insert into people(name) values('Moleman')");
        return ps.executeUpdate();
      }
    };
    JdbcOperation<String> select = new JdbcOperation<String>(){
      @Override
      protected String execute(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select name from people where name=?");
        ps.setString(1, "Moleman");
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString(1) : null;
      }
    };
    JdbcOperation<Integer> delete = new JdbcOperation<Integer>(){
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

}
