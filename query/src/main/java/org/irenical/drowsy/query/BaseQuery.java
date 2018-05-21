package org.irenical.drowsy.query;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

public class BaseQuery implements Query {

  private TYPE type;

  private boolean returnGeneratedKeys;

  private List<Object> parameters;

  private String query;

  public void setParameters(List<Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public List<Object> getParameters() {
    return parameters;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void setQueryFromResource(ClassLoader classLoader, String resourcePath) {
    if (classLoader == null) {
      throw new IllegalArgumentException("Classloader cannot be nul");
    }
    InputStream is = classLoader.getResourceAsStream(resourcePath);
    if (is == null) {
      throw new IllegalArgumentException(String.format("Resource could not be found in the provided path: %s", resourcePath));
    }
    try (
      Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
    ) {
      if (!scanner.hasNext()) {
        throw new IllegalStateException(String.format("File at %s is empty", resourcePath));
      }
      this.setQuery(scanner.next().trim());
    }
  }

  public void setQueryFromResource(String resourcePath) {
    this.setQueryFromResource(this.getClass().getClassLoader(), resourcePath);
  }

  @Override
  public String getQuery() {
    return query;
  }

  public void setType(TYPE type) {
    this.type = type;
  }

  @Override
  public boolean returnGeneratedKeys() {
    return returnGeneratedKeys;
  }

  public void setReturnGeneratedKeys(boolean returnGeneratedKeys) {
    this.returnGeneratedKeys = returnGeneratedKeys;
  }

  @Override
  public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
    PreparedStatement ps;
    if (TYPE.CALL.equals(type)) {
      ps = connection.prepareCall(query);
    } else if (returnGeneratedKeys) {
      ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    } else {
      ps = connection.prepareStatement(query);
    }
    setParameters(ps, parameters);
    return ps;
  }

  private void setParameters(PreparedStatement ps, Collection<Object> parameters) throws SQLException {
    if (parameters != null) {
      int current = 1;
      for (Object param : parameters) {
        setInputParameter(ps, current++, param);
      }
    }
  }

  private void setInputParameter(PreparedStatement ps, int idx, Object value) throws SQLException {
    if (value instanceof Timestamp) {
      ps.setTimestamp(idx, (Timestamp) value);
    } else if (value instanceof ZonedDateTime) {
      ZonedDateTime zdt = (ZonedDateTime) value;
      Calendar cal = GregorianCalendar.from(zdt);
      Timestamp t = Timestamp.from(zdt.toInstant());
      ps.setTimestamp(idx, t, cal);
    } else if (value instanceof Time) {
      ps.setTime(idx, (Time) value);
    } else if (value instanceof Date) {
      ps.setDate(idx, (Date) value);
    } else if (value instanceof Enum<?>) {
      ps.setString(idx, value.toString());
    } else if (value instanceof Class<?>) {
      if (ps instanceof CallableStatement) {
        setOutputParameter((CallableStatement) ps, idx, (Class<?>) value);
      } else {
        throw new IllegalArgumentException("Invalid parameter type for non-CallableStatement: " + value.getClass());
      }
    } else {
      ps.setObject(idx, value);
    }
  }

  private void setOutputParameter(CallableStatement statement, int idx, Class<?> value) throws SQLException {
    if (String.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.VARCHAR);
    } else if (Float.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.FLOAT);
    } else if (Integer.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.INTEGER);
    } else if (Timestamp.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.TIMESTAMP);
    } else if (Boolean.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.BOOLEAN);
    } else {
      statement.registerOutParameter(idx, java.sql.Types.JAVA_OBJECT);
    }
  }

}
