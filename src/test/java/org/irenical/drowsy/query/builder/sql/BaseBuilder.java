package org.irenical.drowsy.query.builder.sql;

import java.util.List;

import org.irenical.drowsy.query.Query;
import org.irenical.drowsy.query.builder.QueryBuilder;
import org.junit.Assert;

public class BaseBuilder {
  
  public void assertBuilder(QueryBuilder<?> builder, String expectedQuery, List<Object> expectedParams) {
    Query q = builder.build();
    Assert.assertEquals(expectedQuery, q.getQuery());
    Assert.assertEquals(expectedParams,q.getParameters());
  }
  
}
