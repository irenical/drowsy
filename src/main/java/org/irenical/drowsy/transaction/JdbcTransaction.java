package org.irenical.drowsy.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTransaction<OUTPUT> {

  private final Logger LOG = LoggerFactory.getLogger(JdbcTransaction.class);

  private final boolean assumeAutoCommit;

  public JdbcTransaction() {
    this(false);
  }

  /**
   * @param assumeAutoCommit
   *          - if set to false, JdbcTransaction will handle commit/rollback
   *          logic
   */
  public JdbcTransaction(boolean assumeAutoCommit) {
    this.assumeAutoCommit = assumeAutoCommit;
  }

  protected abstract OUTPUT execute(Connection connection) throws SQLException;

  public OUTPUT run(DataSource dataSource) throws SQLException {
    if (dataSource == null) {
      throw new IllegalArgumentException("JDBC datasource cannot be null");
    }
    return execute(dataSource.getConnection());
  }

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
    } catch (SQLException e) {
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
