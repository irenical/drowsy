package org.irenical.drowsy;

import java.sql.SQLException;
import java.util.List;

import org.irenical.drowsy.mapper.bean.LegitPerson;
import org.irenical.drowsy.query.SQLQueryBuilder;
import org.junit.Assert;
import org.junit.Test;

public class DrowsyTest extends PGTestUtils {
  
  @Test
  public void test() throws InstantiationException, IllegalAccessException, SQLException{
    Drowsy drowsy = new Drowsy();
    drowsy.start();
    List<LegitPerson> got = drowsy.executeQuery(SQLQueryBuilder.select("* from people").build(), LegitPerson.class);
    Assert.assertEquals(1, got.size());
    Assert.assertEquals("Boda", got.get(0).getName());
  }

}
