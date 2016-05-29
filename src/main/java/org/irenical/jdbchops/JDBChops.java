package org.irenical.jdbchops;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class JDBChops {

  private JDBChops() {
  }

  public static void setInputParameter(PreparedStatement statement, int idx, Object value) throws SQLException {
    if (value instanceof Timestamp) {
      statement.setTimestamp(idx, (Timestamp) value);
    } else if (value instanceof ZonedDateTime) {
      ZonedDateTime zdt = (ZonedDateTime) value;
      Calendar cal = GregorianCalendar.from(zdt);
      Timestamp t = Timestamp.from(zdt.toInstant());
      statement.setTimestamp(idx, t, cal);
    } else if (value instanceof Time) {
      statement.setTime(idx, (Time) value);
    } else if (value instanceof Date) {
      statement.setDate(idx, (Date) value);
    } else if (value instanceof Enum<?>) {
      statement.setString(idx, value.toString());
    } else {
      statement.setObject(idx, value);
    }
  }

  public static void setOutputParameter(CallableStatement statement, int idx, Object value) throws SQLException {
    if (value instanceof String) {
      statement.registerOutParameter(idx, java.sql.Types.VARCHAR);
    } else if (value instanceof Float) {
      statement.registerOutParameter(idx, java.sql.Types.FLOAT);
    } else if (value instanceof Integer) {
      statement.registerOutParameter(idx, java.sql.Types.INTEGER);
    } else if (value instanceof Timestamp) {
      statement.registerOutParameter(idx, java.sql.Types.TIMESTAMP);
    } else if (value instanceof Boolean) {
      statement.registerOutParameter(idx, java.sql.Types.BOOLEAN);
    } else {
      statement.registerOutParameter(idx, java.sql.Types.JAVA_OBJECT);
    }
  }

}
