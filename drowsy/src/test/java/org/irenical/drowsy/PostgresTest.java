package org.irenical.drowsy;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresTest {
  
  private static Config config = ConfigFactory.getConfig();
  
  @SuppressWarnings("rawtypes")
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer().withDatabaseName("test")
          .withUsername(config.getString("jdbc.username")).withPassword(config.getString("jdbc.password"));

  @BeforeClass
  public static void startDb() throws Exception {
      config.setProperty("jdbc.jdbcUrl", postgres.getJdbcUrl());
  }
  
  @AfterClass
  public static void stopDb() {
      postgres.stop();
  }

}
