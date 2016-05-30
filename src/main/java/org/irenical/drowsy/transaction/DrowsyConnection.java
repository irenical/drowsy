package org.irenical.drowsy.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    return (Connection) Proxy.newProxyInstance(DrowsyConnection.class.getClassLoader(), new Class[] { Connection.class }, proxy);
  }
  
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if ("close".equals(method.getName())) {
        for (Statement stmt : statements) {
          try {
            stmt.close();
          } catch (Exception e) {
            LOG.warn("Ignoring error closing statement " + stmt + ". Cause: " + e.getMessage());
          }
        }
        statements.clear();
      }

      final Object ret = method.invoke(connection, args);
      if (ret instanceof Statement) {
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
