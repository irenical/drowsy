package org.irenical.drowsy.query;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.irenical.drowsy.query.Query.TYPE;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

public class BaseQueryTest {

  protected static HikariDataSource ds;

  protected Connection c;
  
  protected static PostgresProcess postgresProcess;
  protected static PostgresConfig postgresConfig;

  @BeforeClass
  public static void startPg() throws ClassNotFoundException, IOException, SQLException {
    Class.forName("org.postgresql.Driver");
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    postgresConfig = PostgresConfig.defaultWithDbName("test");
    PostgresExecutable exec = runtime.prepare(postgresConfig);
    postgresProcess = exec.start();

    String url = String.format("jdbc:postgresql://%s:%s/%s?", postgresConfig.net().host(), postgresConfig.net().port(),
        postgresConfig.storage().dbName());
    Config config = ConfigFactory.getConfig();
    String user = System.getProperty("user.name");
    config.setProperty("jdbc.jdbcUrl", url);
    config.setProperty("jdbc.username", user);
    config.setProperty("jdbc.password", null);
    
    Flyway flyway = new Flyway();
    flyway.setDataSource(url, user, null);
    flyway.migrate();
    
    HikariConfig hc = new HikariConfig();
    hc.setJdbcUrl(config.getString("jdbc.jdbcUrl"));
    hc.setUsername(config.getString("jdbc.username"));
    hc.setPassword(config.getString("jdbc.password"));
    ds = new HikariDataSource(hc);
  }

  @AfterClass
  public static void shutdown() {
	  ds.close();
    postgresProcess.stop();
  }

  @Before
  public void startConnection() throws SQLException {
    c = ds.getConnection();
  }

  @After
  public void stopConnection() throws SQLException {
    c.close();
  }

  @Test
  public void testSelect() throws SQLException {
    BaseQuery q = new BaseQuery();
    q.setQuery("select * from people");
    PreparedStatement ps = q.createPreparedStatement(c);
    Assert.assertNotNull(ps);
    ResultSet rs = ps.executeQuery();
    Assert.assertNotNull(rs);
    Assert.assertTrue(rs.next());
    rs.close();
    ps.close();
  }

  @Test
  public void testCall() throws SQLException {
    String name = UUID.randomUUID().toString();

    BaseQuery q = new BaseQuery();
    q.setType(TYPE.CALL);
    q.setQuery("{call create_person(?,?)}");
    q.setParameters(Arrays.asList(name, new Timestamp(0)));
    PreparedStatement ps = q.createPreparedStatement(c);
    ps.executeUpdate();

    q = new BaseQuery();
    q.setType(TYPE.SELECT);
    q.setQuery("select * from people where name=?");
    q.setParameters(Arrays.asList(name));
    ps = q.createPreparedStatement(c);
    ResultSet set = ps.executeQuery();
    Assert.assertTrue(set.next());
    Assert.assertFalse(set.next());
  }

  @Test
  public void testInsert() throws SQLException {
    BaseQuery q = new BaseQuery();
    q.setType(TYPE.INSERT);
    q.setReturnGeneratedKeys(true);
    q.setQuery("insert into people(name) values('obama')");
    PreparedStatement ps = q.createPreparedStatement(c);
    Assert.assertNotNull(ps);
    ps.executeUpdate();
    ResultSet rs = ps.getGeneratedKeys();
    Assert.assertNotNull(rs);
    Assert.assertTrue(rs.next());
    rs.close();
    ps.close();
  }

  @Test
  public void testTimestamp() throws SQLException {
    TimeZone was = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    String name = UUID.randomUUID().toString();
    
    BaseQuery q = new BaseQuery();
    q.setType(TYPE.INSERT);
    q.setQuery("insert into people(name,birth) values(?,?)");
    Timestamp birth = new Timestamp(0);
    q.setParameters(Arrays.asList(name, birth));
    PreparedStatement ps = q.createPreparedStatement(c);
    ps.executeUpdate();
    ps.close();

    TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    q = new BaseQuery();
    q.setType(TYPE.SELECT);
    q.setQuery("select * from people where name=?");
    q.setParameters(Arrays.asList(name));
    ps = q.createPreparedStatement(c);
    ResultSet set = ps.executeQuery();
    set.next();
    Timestamp readTimestamp = set.getTimestamp("birth");
    Assert.assertEquals(birth, readTimestamp);
    set.close();
    ps.close();
    TimeZone.setDefault(was);
  }
  
  @Test
  public void testZonedDateTime() throws SQLException {
    TimeZone was = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    String name = UUID.randomUUID().toString();
    
    BaseQuery q = new BaseQuery();
    q.setType(TYPE.INSERT);
    q.setQuery("insert into people(name,birth) values(?,?)");
    ZonedDateTime birth = ZonedDateTime.now();
    q.setParameters(Arrays.asList(name, birth));
    PreparedStatement ps = q.createPreparedStatement(c);
    ps.executeUpdate();
    ps.close();
    
    TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    q = new BaseQuery();
    q.setType(TYPE.SELECT);
    q.setQuery("select * from people where name=?");
    q.setParameters(Arrays.asList(name));
    ps = q.createPreparedStatement(c);
    ResultSet set = ps.executeQuery();
    set.next();
    Timestamp readTimestamp = set.getTimestamp("birth");
    Assert.assertEquals(birth.toInstant().toEpochMilli(), readTimestamp.getTime());
    set.close();
    ps.close();
    
    TimeZone.setDefault(was);
  }

}
