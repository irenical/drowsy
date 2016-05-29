package org.irenical.drowsy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTransaction<OUTPUT> {

  private final Logger LOG = LoggerFactory.getLogger(JdbcTransaction.class);

  private final boolean autoCommit;

  public JdbcTransaction() {
    this(false);
  }

  public JdbcTransaction(boolean autoCommit) {
    this.autoCommit = autoCommit;
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
      if (!autoCommit) {
        connection.commit();
      }
      return result;
    } catch (SQLException e) {
      if (!autoCommit) {
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
