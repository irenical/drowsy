package org.irenical.drowsy;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcFunction<RESULT> {
  
  public RESULT apply(Connection connection) throws SQLException;

}
