package org.irenical.drowsy.transaction;

public abstract class JdbcOperation<OUTPUT> extends JdbcTransaction<OUTPUT> {

  public JdbcOperation() {
    super(true);
  }

}
