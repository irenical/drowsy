package org.irenical.drowsy;

public abstract class JdbcOperation<OUTPUT> extends JdbcTransaction<OUTPUT> {

  public JdbcOperation() {
    super(true);
  }

}
