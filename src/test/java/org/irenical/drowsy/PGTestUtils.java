package org.irenical.drowsy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

public class PGTestUtils {

  protected static PostgresProcess postgresProcess;
  protected static PostgresConfig postgresConfig;

  @BeforeClass
  public static void startPg() throws ClassNotFoundException, IOException, SQLException {
    Class.forName("org.postgresql.Driver");
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    postgresConfig = PostgresConfig.defaultWithDbName("test");
    PostgresExecutable exec = runtime.prepare(postgresConfig);
    postgresProcess = exec.start();
    Connection connection = createConnection(false);

    // create table
    PreparedStatement createPeopleTableStatement = connection
        .prepareStatement("create table people (id serial primary key, name text, birth timestamptz)");
    createPeopleTableStatement.executeUpdate();
    createPeopleTableStatement.close();

    // create procedure
    PreparedStatement createProcedureStatement = connection.prepareStatement(
        "CREATE OR REPLACE FUNCTION create_person(name text, birth timestamptz) " + "RETURNS void AS $$ " + "BEGIN "
            + "  INSERT INTO people(name,birth) VALUES (name, birth); " + "END; " + "$$ LANGUAGE plpgsql;");
    createProcedureStatement.executeUpdate();
    createProcedureStatement.close();

    // create data
    PreparedStatement createPersonStatement = connection.prepareStatement("insert into people(name) values('Boda')");
    createPersonStatement.executeUpdate();
    createPersonStatement.close();
    
    createPersonStatement = connection.prepareStatement("insert into people(name) values('Buda')");
    createPersonStatement.executeUpdate();
    createPersonStatement.close();

    connection.commit();
    connection.close();

    String url = String.format("jdbc:postgresql://%s:%s/%s?", postgresConfig.net().host(), postgresConfig.net().port(),
        postgresConfig.storage().dbName());
    Config config = ConfigFactory.getConfig();
    String user = System.getProperty("user.name");
    config.setProperty("jdbc.jdbcUrl", url);
    config.setProperty("jdbc.username", user);
    config.setProperty("jdbc.password", null);
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

}
