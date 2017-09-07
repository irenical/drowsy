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
   * Initialized a Drowsy object with configuration prefixed by 'jdbc'. You must
   * call {@link start} before executing JDBC operations on this instance
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

  private void coldStart() {
    if (this.transactionDataSource == null) {
      DrowsySimpleDataSource transactionDataSource = new DrowsySimpleDataSource(config, false, null, null);
      transactionDataSource.start();
      this.transactionDataSource = transactionDataSource;
    }

    if (this.operationDataSource == null) {
      DrowsySimpleDataSource operationDataSource = new DrowsySimpleDataSource(config, true, false, true);
      operationDataSource.start();
      this.operationDataSource = operationDataSource;
    }

    if (this.readOnlyDataSource == null) {
      DrowsyDataSource readOnlyDataSource = new DrowsySimpleDataSource(config, true, true, true);
      readOnlyDataSource.start();
      this.readOnlyDataSource = readOnlyDataSource;
    }
  }

  /**
   * Launches this Drowsy instance, initializing the underlying datasources
   */
  @Override
  public void start() {
    coldStart();
  }

  /**
   * Stops this Drowsy instance, shutting down the underlying datasources
   */
  @Override
  public void stop() {
    if (operationDataSource != null) {
      operationDataSource.stop();
      operationDataSource = null;
    }
    if (transactionDataSource != null) {
      transactionDataSource.stop();
      transactionDataSource = null;
    }
    if (readOnlyDataSource != null) {
      readOnlyDataSource.stop();
      readOnlyDataSource = null;
    }
  }

  /**
   * Checks if the underlying datasources are healthy
   */
  @Override
  public boolean isRunning() {
    return transactionDataSource != null && operationDataSource != null && readOnlyDataSource != null
        && transactionDataSource.isRunning() && operationDataSource.isRunning() && readOnlyDataSource.isRunning();
  }

  /**
   * Atomically executes a read operation represented by given query
   * 
   * @param query
   *          the read operation, usually a select
   * @param reader
   *          the lambda function ResultSet to transaction output
   * @param <OUTPUT>
   *          the operation's output type
   * @return the transaction's output
   * @throws SQLException
   *           if an error occurs
   */
  public <OUTPUT> OUTPUT read(Query query, JdbcFunction<ResultSet, OUTPUT> reader) throws SQLException {
    coldStart();
    return new JdbcOperation<OUTPUT>() {
      @Override
      protected OUTPUT execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return reader.apply(statement.executeQuery());
      }
    }.run(readOnlyDataSource);
  }

  /**
   * Atomically executes a read operation represented by given query.
   * 
   * @param query
   *          the read operation, usually a select
   * @param beanClass
   *          a bean class whose fields directly match the query's columns
   * @param <OUTPUT>
   *          the operation's output type
   * @return the transaction's output
   * @throws SQLException
   *           if an error occurs
   */
  public <OUTPUT> List<OUTPUT> read(Query query, Class<OUTPUT> beanClass) throws SQLException {
    coldStart();
    return new JdbcOperation<List<OUTPUT>>() {
      @Override
      protected List<OUTPUT> execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return mapper.map(statement.executeQuery(), beanClass);
      }
    }.run(readOnlyDataSource);
  }

  /**
   * Atomically executes a write operation represented by given query. The
   * ResultSet will be fed to given lambda.
   * 
   * @param query
   *          the write operation, usually an insert, update or delete
   * @param reader
   *          the lambda function ResultSet to transaction output
   * @param <OUTPUT>
   *          the operation's output type
   * @return the transaction's output
   * @throws SQLException
   *           if an error occurs
   */
  public <OUTPUT> OUTPUT write(Query query, JdbcFunction<ResultSet, OUTPUT> reader) throws SQLException {
    coldStart();
    return new JdbcOperation<OUTPUT>() {
      @Override
      protected OUTPUT execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        ResultSet got = null;
        if (query.returnGeneratedKeys()) {
          statement.executeUpdate();
          got = statement.getGeneratedKeys();
        } else {
          statement.execute();
          got = statement.getResultSet();
        }
        return reader.apply(got);
      }
    }.run(operationDataSource);
  }

  /**
   * Atomically executes a write operation represented by given query. Maps the
   * result set to a list containing objects of given class
   * 
   * @param query
   *          the write operation, usually an insert, update or delete
   * @param beanClass
   *          a bean class whose fields directly match the query's columns
   * @param <OUTPUT>
   *          the operation's output type
   * @return a list of instantiated objects
   * @throws SQLException
   *           if an error occurs
   */
  public <OUTPUT> List<OUTPUT> write(Query query, Class<OUTPUT> beanClass) throws SQLException {
    coldStart();
    return write(query, rs -> {
      return mapper.map(rs, beanClass);
    });
  }

  /**
   * Atomically executes a write operation represented by given query
   * 
   * @param query
   *          the write operation, usually an insert, update or delete
   * @return the number of affected rows
   * @throws SQLException
   *           if an error occurs
   */
  public int write(Query query) throws SQLException {
    coldStart();
    return new JdbcOperation<Integer>() {
      @Override
      protected Integer execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return statement.executeUpdate();
      }
    }.run(operationDataSource);
  }

  /**
   * Runs an arbitrary set of instructions over a JDBC connection with auto-commit
   * disabled. The transaction will be commited afterwards or rollbacked on error.
   * The connection and any JDBC resources created by the transaction code will be
   * closed automatically.
   * 
   * @param transaction
   *          the JDBC transaction's code
   * @param <OUTPUT>
   *          the transaction's output type
   * @return the transaction output object
   * @throws SQLException
   *           if an error occurs
   */
  public <OUTPUT> OUTPUT executeTransaction(JdbcFunction<Connection, OUTPUT> transaction) throws SQLException {
    coldStart();
    return new JdbcTransaction<OUTPUT>() {
      @Override
      protected OUTPUT execute(Connection connection) throws SQLException {
        return transaction.apply(connection);
      }
    }.run(transactionDataSource);
  }

}
