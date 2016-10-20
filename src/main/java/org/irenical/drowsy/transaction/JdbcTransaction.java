package org.irenical.drowsy.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the JDBC connection lifecycle, including resource release and connection rollback 
 * 
 * @param <OUTPUT> the transaction output class. Usually a bean or a collection of beans translated from the ResultSet
 */
public abstract class JdbcTransaction<OUTPUT> {

  private final Logger LOG = LoggerFactory.getLogger(JdbcTransaction.class);

  private final boolean assumeAutoCommit;

  /**
   * Creates a new re-usable, thread-safe transaction object
   * Same as {@link JdbcTransaction(false)}
   */
  public JdbcTransaction() {
    this(false);
  }

  /**
   * Creates a new re-usable, thread-safe transaction object
   * @param assumeAutoCommit
   *          - if set to false, JdbcTransaction will handle commit/rollback
   *          logic
   */
  public JdbcTransaction(boolean assumeAutoCommit) {
    this.assumeAutoCommit = assumeAutoCommit;
  }

  /**
   * Actual transaction implementation. The connection object will be
   * commited/rollbacked and closed afterwards.
   * 
   * @param connection
   *          - the connection to run the transaction on
   * @return the transaction output
   * @throws SQLException
   *           if a JDBC error occurrs. Other non-runtime exceptions should be
   *           wrapped in a SQLException
   */
  protected abstract OUTPUT execute(Connection connection) throws SQLException;

  /**
   * Executes this transaction on a connection retrieved by received DataSource.
   * Same as calling {@link #run(Connection)}
   * 
   * @param dataSource
   *          - the connection's data source
   * @return the transaction output
   * @throws SQLException
   *           if a JDBC error occurrs. An error during rollback will be logged
   *           but not raised
   */
  public OUTPUT run(DataSource dataSource) throws SQLException {
    if (dataSource == null) {
      throw new IllegalArgumentException("JDBC datasource cannot be null");
    }
    return run(dataSource.getConnection());
  }

  /**
   * Executes this transaction on given connection
   * 
   * @param connection
   *          - the connection to run the transaction on
   * @return the transaction output
   * @throws SQLException
   *           if a JDBC error occurrs. An error during rollback will be logged
   *           but not raised.
   */
  public OUTPUT run(Connection connection) throws SQLException {
    if (connection == null) {
      throw new IllegalArgumentException("JDBC connection cannot be null");
    }
    connection = DrowsyConnection.wrap(connection);
    try {
      OUTPUT result = execute(connection);
      if (!assumeAutoCommit) {
        connection.commit();
      }
      return result;
    } catch (Exception ex) {
      SQLException e = (ex instanceof SQLException) ? ((SQLException) ex) : new SQLException(ex);
      if (!assumeAutoCommit) {
        LOG.error("Error executing transaction. Raising exception after trying to rollback.");
        try {
          connection.rollback();
        } catch (SQLException rollbackException) {
          LOG.error("Error rollbacking transaction. Cause: " + rollbackException.getMessage());
        }
      }
      throw e;
    } finally {
      connection.close();
    }
  }

}
