package org.irenical.drowsy.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface Query {
  
  enum TYPE {
    SELECT, INSERT, UPDATE, DELETE, CALL
  };
  
  TYPE getType();
  
  List<Object> getParameters();

  String getQuery();
  
  PreparedStatement createPreparedStatement(Connection connection) throws SQLException;
  
}
