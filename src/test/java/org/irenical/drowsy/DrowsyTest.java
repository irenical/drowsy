package org.irenical.drowsy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.irenical.drowsy.mapper.bean.LegitPerson;
import org.irenical.drowsy.query.builder.sql.DeleteBuilder;
import org.irenical.drowsy.query.builder.sql.InsertBuilder;
import org.irenical.drowsy.query.builder.sql.SelectBuilder;
import org.junit.Assert;
import org.junit.Test;

public class DrowsyTest extends PGTestUtils {

  @Test
  public void testSelect() throws SQLException {
    Drowsy drowsy = new Drowsy();
    drowsy.start();
    List<LegitPerson> got = drowsy
        .read(SelectBuilder.create("select * from people order by id limit 1").build(), LegitPerson.class);
    Assert.assertEquals(1, got.size());
    Assert.assertEquals("Boda", got.get(0).getName());
  }

  @Test
  public void testInsertDelete() throws SQLException {
    Drowsy drowsy = new Drowsy();
    drowsy.start();
    List<LegitPerson> inserted = drowsy.write(InsertBuilder
        .create("insert into people(name) values(").param("John").literal(")").build(),
        LegitPerson.class);
    LegitPerson newJohn = inserted.get(0);
    Assert.assertNotEquals(1, newJohn.getId());
    Assert.assertEquals("John", newJohn.getName());

    List<LegitPerson> selected = drowsy.read(
        SelectBuilder.create("select * from people where id=").param(newJohn.getId()).build(),
        LegitPerson.class);

    Assert.assertEquals(1, selected.size());
    LegitPerson oldJohn = selected.get(0);
    Assert.assertEquals(newJohn.getName(), oldJohn.getName());
    Assert.assertEquals(newJohn.getId(), oldJohn.getId());

    int del = drowsy.write(
        DeleteBuilder.from("people").where("id").eq(oldJohn.getId()).build());
    Assert.assertEquals(1, del);
  }

  @Test
  public void testTransactionAutoCommited() throws SQLException {
    Drowsy drowsy = new Drowsy();
    drowsy.start();
    List<LegitPerson> inserted = drowsy.write(InsertBuilder
        .create("insert into people(name) values(").param("John").literal(")").build(),
        LegitPerson.class);
    LegitPerson newJohn = inserted.get(0);
    Assert.assertNotEquals(1, newJohn.getId());
    Assert.assertEquals("John", newJohn.getName());

    List<LegitPerson> selected = drowsy.read(
        SelectBuilder.create("select * from people where id=").param(newJohn.getId()).build(),
        LegitPerson.class);

    Assert.assertEquals(1, selected.size());
    LegitPerson oldJohn = selected.get(0);
    Assert.assertEquals(newJohn.getName(), oldJohn.getName());
    Assert.assertEquals(newJohn.getId(), oldJohn.getId());

    int del = drowsy.write(
        DeleteBuilder.from("people").where("id").eq(oldJohn.getId()).build());
    Assert.assertEquals(1, del);
  }

  @Test
  public void testTransactionCommited() throws SQLException {
    Drowsy drowsy = new Drowsy();
    drowsy.start();
    
    drowsy.executeTransaction(c->{
      PreparedStatement ps = InsertBuilder
        .create("insert into people(name) values(").param("John").literal(")")
        .build()
        .createPreparedStatement(c);
      ps.executeUpdate();
      return null;
    });

    List<LegitPerson> selected = drowsy.read(
        SelectBuilder.create("select * from people where name=").param("John").build(),
        LegitPerson.class);

    Assert.assertEquals(1, selected.size());

    int del = drowsy.write(
        DeleteBuilder.from("people").where("name").eq("John").build());
    Assert.assertEquals(1, del);
  }
  
  @Test
  public void testTransactionRolledback() throws SQLException {
    Drowsy drowsy = new Drowsy();
    drowsy.start();
    
    try{
      drowsy.executeTransaction(c->{
        PreparedStatement ps = InsertBuilder
          .create("insert into people(name) values(").param("John").literal(")")
          .build()
          .createPreparedStatement(c);
        ps.executeUpdate();
        throw new SQLException("oops");
      });
    } catch(SQLException e){
      //     (-_-)
      //      /\
      // excellent
    }
    
    List<LegitPerson> selected = drowsy.read(
        SelectBuilder.create("select * from people where name=").param("John").build(),
        LegitPerson.class);

    Assert.assertEquals(0, selected.size());

  }

}
