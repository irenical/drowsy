package org.irenical.drowsy;

import java.sql.SQLException;

@FunctionalInterface
public interface JdbcFunction<INPUT, OUTPUT> {
  
  OUTPUT apply(INPUT input) throws SQLException;

}
