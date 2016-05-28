package org.irenical.drowsy;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrowsyTransaction<OUTPUT> {

  private final Logger LOG = LoggerFactory.getLogger(DrowsyTransaction.class);

  private final JdbcFunction<Connection, OUTPUT> execution;

  public DrowsyTransaction(JdbcFunction<Connection, OUTPUT> execution) {
    this.execution = execution;
  }

  public OUTPUT execute(Connection connection) throws SQLException {
    if (connection == null) {
      throw new IllegalArgumentException("JDBC connection cannot be null");
    }
    try {
      connection = DrowsyConnection.wrap(connection);
      OUTPUT result = execution.apply(connection);
      connection.commit();
      return result;
    } catch (SQLException e) {
      LOG.error("Error executing transaction. Raising exception after trying to rollback.");
      try {
        connection.rollback();
      } catch (SQLException rollbackException) {
        LOG.error("Error rollbacking transaction. Cause: " + rollbackException.getMessage());
      }
      throw e;
    } finally {
      connection.close();
    }
  }

}
