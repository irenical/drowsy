package org.irenical.drowsy;

import org.irenical.drowsy.datasource.DrowsyDataSource;
import org.irenical.jindy.Config;

public class DrowsySimpleDataSource extends DrowsyDataSource {
  
  private final Boolean autoCommit;
  
  private final Boolean readOnly;
  
  private final Boolean flywayBypass;
  
  public DrowsySimpleDataSource(Config config, Boolean autoCommit, Boolean readOnly, Boolean flywayBypass) {
    super(config);
    this.autoCommit=autoCommit;
    this.readOnly=readOnly;
    this.flywayBypass=flywayBypass;
  }
  
  @Override
  protected boolean isAutoCommit() {
    return autoCommit == null ? super.isAutoCommit() : autoCommit;
  }

  @Override
  protected boolean isReadOnly() {
    return readOnly == null ? super.isReadOnly() : readOnly;
  }

  @Override
  protected boolean isFlywayBypass() {
    return flywayBypass == null ? super.isFlywayBypass() : flywayBypass;
  }

}
