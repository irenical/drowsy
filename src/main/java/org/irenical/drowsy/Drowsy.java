package org.irenical.drowsy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.irenical.drowsy.datasource.DrowsyDataSource;
import org.irenical.drowsy.mapper.BeanMapper;
import org.irenical.drowsy.query.Query;
import org.irenical.drowsy.transaction.JdbcOperation;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.lifecycle.LifeCycle;

public class Drowsy implements LifeCycle {

  private static final String TRANSACTION_PREFIX = "drowsy.transaction";

  private static final String OPERATION_PREFIX = "drowsy.operation";

  private final BeanMapper mapper = new BeanMapper();

  private DrowsyDataSource transactionDataSource;
  private DrowsyDataSource operationDataSource;

  public Drowsy() {
    transactionDataSource = new DrowsyDataSource(TRANSACTION_PREFIX);
    operationDataSource = new DrowsyDataSource(OPERATION_PREFIX);
  }

  @Override
  public void start() {
    prepareProperties();
    transactionDataSource = new DrowsyDataSource(TRANSACTION_PREFIX + ".jdbc");
    transactionDataSource.start();
    operationDataSource = new DrowsyDataSource(OPERATION_PREFIX + ".jdbc");
    operationDataSource.start();
  }

  @Override
  public void stop() {
    operationDataSource.stop();
    transactionDataSource.stop();
  }

  private void prepareProperties() {
    Config config = ConfigFactory.getConfig();
    for (String k : config.getKeys("jdbc")) {
      config.setProperty(TRANSACTION_PREFIX + "." + k, config.getString(k));
      config.setProperty(OPERATION_PREFIX + "." + k, config.getString(k));
    }
    config.setProperty(OPERATION_PREFIX + "." + "jdbc.autoCommit", "true");
    config.setProperty(OPERATION_PREFIX + "." + "jdbc.flyway.bypass", "true");
    config.setProperty(TRANSACTION_PREFIX + "." + "jdbc.autoCommit", "false");
  }

  @Override
  public boolean isRunning() {
    return transactionDataSource.isRunning() && operationDataSource.isRunning();
  }

  public <OBJECT> List<OBJECT> executeSelect(Query query, Class<OBJECT> beanClass)
      throws SQLException {
    return new JdbcOperation<List<OBJECT>>() {
      @Override
      protected List<OBJECT> execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return mapper.map(statement.executeQuery(), beanClass);
      }
    }.run(operationDataSource);
  }

  public <OBJECT> List<OBJECT> executeInsert(Query query, Class<OBJECT> beanClass)
      throws SQLException, InstantiationException, IllegalAccessException {
    return new JdbcOperation<List<OBJECT>>() {
      @Override
      protected List<OBJECT> execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        statement.executeUpdate();
        return mapper.map(statement.getGeneratedKeys(), beanClass);
      }
    }.run(operationDataSource);
  }

  public int executeDelete(Query query) throws SQLException {
    return new JdbcOperation<Integer>() {
      @Override
      protected Integer execute(Connection connection) throws SQLException {
        PreparedStatement statement = query.createPreparedStatement(connection);
        return statement.executeUpdate();
      }
    }.run(operationDataSource);
  }

}
