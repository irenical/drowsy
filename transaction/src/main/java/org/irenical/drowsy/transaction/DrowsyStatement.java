package org.irenical.drowsy.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by gluiz on 06/04/2017.
 */
public final class DrowsyStatement implements InvocationHandler {
  private static final Logger LOG = LoggerFactory.getLogger(DrowsyStatement.class);
  private final Collection<ResultSet> resultSets;
  private final Statement statement;

  private DrowsyStatement(Statement statement) {
    this.statement = statement;
    this.resultSets = new HashSet<>();
  }

  public static Statement wrap(Statement statement) {
    if (Proxy.isProxyClass(statement.getClass())
      && Proxy.getInvocationHandler(statement) instanceof DrowsyStatement) {
      return statement;
    }
    DrowsyStatement proxy = new DrowsyStatement(statement);

    if (statement instanceof CallableStatement) {
      return (CallableStatement) Proxy.newProxyInstance(DrowsyStatement.class.getClassLoader(),
        new Class[]{CallableStatement.class}, proxy);
    }
    if (statement instanceof PreparedStatement) {
      return (PreparedStatement) Proxy.newProxyInstance(DrowsyStatement.class.getClassLoader(),
        new Class[]{PreparedStatement.class}, proxy);
    }
    return (Statement) Proxy.newProxyInstance(DrowsyStatement.class.getClassLoader(),
      new Class[]{Statement.class}, proxy);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if ("close".equals(method.getName())) {
        for (ResultSet rst : resultSets) {
          try {
            if (!rst.isClosed()) {
              LOG.debug("Result set {} was left open. Closing from DrowsyStatement proxy", rst);
              rst.close();
            }
          } catch (Exception e) {
            LOG.error("Ignoring error closing result set {}", rst, e);
          }
        }
        resultSets.clear();
      }

      final Object ret = method.invoke(statement, args);
      if (ret instanceof ResultSet) {
        resultSets.add((ResultSet) ret);
      }

      return ret;
    } catch (InvocationTargetException ex) {
      if (ex.getCause() instanceof SQLException) {
        throw ex.getCause();
      } else {
        throw ex;
      }
    }
  }
}
