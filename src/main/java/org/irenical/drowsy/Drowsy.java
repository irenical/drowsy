package org.irenical.drowsy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.irenical.drowsy.datasource.DrowsyDataSource;
import org.irenical.drowsy.mapper.BeanMapper;
import org.irenical.drowsy.query.Query;
import org.irenical.lifecycle.LifeCycle;

public class Drowsy implements LifeCycle {

  private final BeanMapper mapper = new BeanMapper();

  private DrowsyDataSource dataSource;

  @Override
  public void start() {
    if (dataSource != null) {
      dataSource.stop();
    }
    dataSource = new DrowsyDataSource();
    dataSource.start();
  }

  @Override
  public void stop() {
    if (dataSource != null) {
      dataSource.stop();
    }
  }

  @Override
  public boolean isRunning() {
    return dataSource != null && dataSource.isRunning();
  }

  public <OBJECT> List<OBJECT> executeQuery(Query query, Class<OBJECT> beanClass)
      throws SQLException, InstantiationException, IllegalAccessException {
    PreparedStatement statement = query.createPreparedStatement(dataSource.getConnection());
    return mapper.map(statement.executeQuery(), beanClass);
  }

}
