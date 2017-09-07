package org.irenical.drowsy;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.PRODUCTION;

import java.io.IOException;
import java.sql.SQLException;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Credentials;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Net;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Storage;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Timeout;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

public class PostgresTest {

  private static PostgresProcess postgresProcess;
  
  private static PostgresConfig postgresConfig;

  @BeforeClass
  public static void startPg() throws ClassNotFoundException, IOException, SQLException, ConfigNotFoundException {
    Config config = ConfigFactory.getConfig();
    Class.forName("org.postgresql.Driver");
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();

    postgresConfig = new PostgresConfig(PRODUCTION, new Net("127.0.0.1", 54321), new Storage("test"), new Timeout(),
        new Credentials(config.getMandatoryString("jdbc.username"), config.getString("jdbc.password")));

    PostgresExecutable exec = runtime.prepare(postgresConfig);
    postgresProcess = exec.start();
  }

  @AfterClass
  public static void shutdown() {
    postgresProcess.stop();
  }

}
