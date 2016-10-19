package org.irenical.drowsy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.irenical.drowsy.datasource.DrowsyDataSource;
import org.irenical.drowsy.mapper.BeanMapper;
import org.irenical.drowsy.query.Query;
import org.irenical.drowsy.transaction.JdbcOperation;
import org.irenical.drowsy.transaction.JdbcTransaction;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.lifecycle.LifeCycle;

/**
 * Drowsy contains DrowsyDataSource's needed to handle transactional/single
 * operations/read-only queries.
 *
 * Configuration is done by the current Jindy binding.
 * <h3>DataSource configuration:</h3> As described in
 * https://github.com/brettwooldridge/HikariCP<br>
 * Prefixed with jdbc by default (ex: jdbc.username, jdbc.password,
 * jdbc.jdbcUrl, etc...)
 *
 * @see DrowsyDataSource
 */
public class Drowsy implements LifeCycle {

  private final BeanMapper mapper = new BeanMapper();

  private final Config config;

  private DrowsyDataSource transactionDataSource;
  private DrowsyDataSource operationDataSource;
  private DrowsyDataSource readOnlyDataSource;

  /**
   * Initialized a Drowsy object with configuration prefixed with 'jdbc'.
   *
   * @see DrowsyDataSource
   */
  public Drowsy() {
    this(ConfigFactory.getConfig().filterPrefix("jdbc"));
  }

  /**
   * Initialized a Drowsy object with the given config.
   *
   * @param config
   *          a jindy Config instance containing the DataSource configuration
   *          properties.
   * @see DrowsyDataSource
   *
   */
  public Drowsy(Config config) {
    this.config = config;
  }

  @Override
  public void start() {
    transactionDataSource = new DrowsyDataSource(config) {
      @Override
      protected boolean isAutoCommit() {
        return false;
      }
    };
    transactionDataSource.start();

    operationDataSource = new DrowsyDataSource(config) {
      @Override
      protected boolean isAutoCommit() {
        return true;
      }

      @Override
      protected boolean isReadOnly() {
        return false;
      }

      @Override
      protected boolean isFlywayBypass() {
        return true;
      }
    };
    operationDataSource.start();

    readOnlyDataSource = new DrowsyDataSource(config) {
      @Override
      protected boolean isAutoCommit() {
        return true;
      }

      @Override
      protected boolean isReadOnly() {
        return true;
      }

      @Override
      protected boolean isFlywayBypass() {
        return true;
      }
    };
    readOnlyDataSource.start();
  }

  @Override
  public void stop() {
    operationDataSource.stop();
    transactionDataSource.stop();
    readOnlyDataSource.stop();
  }

  @Override
  public boolean isRunning() {
    return transactionDataSource.isRunning() && operationDataSource.isRunning() && readOnlyDataSource.isRunning();
  }

  public <OUTPUT> OUTPUT read(Query query, JdbcFunction<ResultSet,OUTPUT> reader) throws SQLException {
    return new JdbcOperation<OUTPUT>() {
      @Override
      protected OUTPUT execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return reader.apply(statement.executeQuery());
      }
    }.run(readOnlyDataSource);
  }

  public <OBJECT> List<OBJECT> read(Query query, Class<OBJECT> beanClass) throws SQLException {
    return new JdbcOperation<List<OBJECT>>() {
      @Override
      protected List<OBJECT> execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return mapper.map(statement.executeQuery(), beanClass);
      }
    }.run(readOnlyDataSource);
  }

  public <OUTPUT> OUTPUT write(Query query, JdbcFunction<ResultSet,OUTPUT> reader) throws SQLException {
    return new JdbcOperation<OUTPUT>() {
      @Override
      protected OUTPUT execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        statement.execute();
        return reader.apply(statement.getResultSet());
      }
    }.run(operationDataSource);
  }

  public <OBJECT> List<OBJECT> write(Query query, Class<OBJECT> beanClass) throws SQLException {
    return new JdbcOperation<List<OBJECT>>() {
      @Override
      protected List<OBJECT> execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        statement.executeUpdate();
        return mapper.map(statement.getGeneratedKeys(), beanClass);
      }
    }.run(operationDataSource);
  }

  public int write(Query query) throws SQLException {
    return new JdbcOperation<Integer>() {
      @Override
      protected Integer execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return statement.executeUpdate();
      }
    }.run(operationDataSource);
  }

  public <OBJECT> OBJECT executeTransaction(JdbcFunction<Connection, OBJECT> transaction) throws SQLException {
    return new JdbcTransaction<OBJECT>() {
      @Override
      protected OBJECT execute(Connection connection) throws SQLException {
        return transaction.apply(connection);
      }
    }.run(transactionDataSource);
  }

}
