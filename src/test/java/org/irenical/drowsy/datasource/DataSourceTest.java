package org.irenical.drowsy.datasource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

public class DataSourceTest {

  private static Config config;
  private static PostgresProcess postgresProcess;

  @BeforeClass
  public static void init() throws ClassNotFoundException, IOException {
    Class.forName("org.postgresql.Driver");
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter
        .getDefaultInstance();
    PostgresConfig postgresConfig = PostgresConfig.defaultWithDbName("test");
    PostgresExecutable exec = runtime.prepare(postgresConfig);
    postgresProcess = exec.start();
    config = ConfigFactory.getConfig();
    String user = System.getProperty("user.name");
    String url = String.format("jdbc:postgresql://%s:%s/%s?", postgresConfig.net().host(),
        postgresConfig.net().port(), postgresConfig.storage().dbName());
    config.setProperty("jdbc.username", user);
    config.setProperty("jdbc.password", null);
    config.setProperty("jdbc.jdbcUrl", url);
  }
  
  @AfterClass
  public static void shutdown() {
    postgresProcess.stop();
  }

  @Test
  public void testLifecycle() throws ConfigNotFoundException, SQLException {
    DrowsyDataSource ds = new DrowsyDataSource();
    ds.start();
    Connection got = ds.getConnection();
    got.close();
    ds.stop();
  }

}
