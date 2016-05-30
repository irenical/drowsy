package org.irenical.drowsy.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import org.irenical.drowsy.PGTestUtils;
import org.irenical.jindy.ConfigNotFoundException;
import org.junit.Test;

public class DataSourceTest extends PGTestUtils {

  @Test
  public void testLifecycle() throws ConfigNotFoundException, SQLException {
    DrowsyDataSource ds = new DrowsyDataSource();
    ds.start();
    Connection got = ds.getConnection();
    got.close();
    ds.stop();
  }

}
