package org.irenical.drowsy;

import java.sql.SQLException;

@FunctionalInterface
public interface JdbcFunction<T, R> {

  R apply(T t) throws SQLException;

}
