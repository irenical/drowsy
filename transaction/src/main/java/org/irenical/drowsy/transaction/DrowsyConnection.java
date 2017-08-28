package org.irenical.drowsy.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

public class DrowsyConnection implements InvocationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DrowsyConnection.class);

  private final Collection<Statement> statements;

  private final Connection connection;

  private DrowsyConnection(Connection connection) {
    this.connection = connection;
    this.statements = new LinkedList<>();
  }

  public static Connection wrap(Connection connection) {
    if (Proxy.isProxyClass(connection.getClass())
      && Proxy.getInvocationHandler(connection) instanceof DrowsyConnection) {
      return connection;
    }
    DrowsyConnection proxy = new DrowsyConnection(connection);
    return (Connection) Proxy.newProxyInstance(DrowsyConnection.class.getClassLoader(),
      new Class[]{Connection.class}, proxy);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if ("close".equals(method.getName())) {
        for (Statement stmt : statements) {
          try {
            if (!stmt.isClosed()) {
              LOG.debug("Statement{} was left open. Closing from DrowsyConnection proxy", stmt);
              stmt.close();
            }
          } catch (Exception e) {
            LOG.error("Ignoring error closing statement {}", stmt, e);
          }
        }
        statements.clear();
      }

      Object ret = method.invoke(connection, args);
      if (ret instanceof Statement) {
        ret = DrowsyStatement.wrap((Statement) ret);
        statements.add((Statement) ret);
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
