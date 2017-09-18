package org.irenical.drowsy.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.PropertyChangedCallback;
import org.irenical.lifecycle.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

/**
 * DrowsyDataSource is a DataSource with dynamic configuration capabilities. It
 * includes FlyWay and uses HikariCP as the actual DataSource implementation.
 *
 * Configuration is done by the given Jindy Config instance.
 * <h3>DataSource configuration:</h3> As described in
 * https://github.com/brettwooldridge/HikariCP<br>
 * <h3>FlyWay configuration:</h3> flyway.bypass - whether to bypass flyway
 * [optional,default=false]<br>
 * flyway.baselineVersion - the baseline version to consider [optional,
 * default=null]
 */
public class DrowsyDataSource implements LifeCycle, DataSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(DrowsyDataSource.class);

  private static String DATASOURCECLASSNAME = "dataSourceClassName";
  private static String JDBCURL = "jdbcUrl";
  private static String USERNAME = "username";
  private static String PASSWORD = "password";
  private static String AUTOCOMMIT = "autoCommit";
  private static String CONNECTIONTIMEOUT = "connectionTimeout";
  private static String IDLETIMEOUT = "idleTimeout";
  private static String MAXLIFETIME = "maxLifetime";
  private static String CONNECTIONTESTQUERY = "connectionTestQuery";
  private static String MINIMUMIDLE = "minimumIdle";
  private static String MAXIMUMPOOLSIZE = "maximumPoolSize";
  private static String POOLNAME = "poolName";
  private static String INITIALIZATIONFAILTIMEOUT = "initializationFailTimeout";
  private static String ISOLATEINTERNALQUERIES = "isolateInternalQueries";
  private static String ALLOWPOOLSUSPENSION = "allowPoolSuspension";
  private static String READONLY = "readOnly";
  private static String REGISTERMBEANS = "registerMbeans";
  private static String CATALOG = "catalog";
  private static String CONNECTIONINITSQL = "connectionInitSql";
  private static String DRIVERCLASSNAME = "driverClassName";
  private static String TRANSACTIONISOLATION = "transactionIsolation";
  private static String VALIDATIONTIMEOUT = "validationTimeout";
  private static String LEAKDETECTIONTHRESHOLD = "leakDetectionThreshold";

  private static String FLYWAY_BYPASS = "flyway.bypass";
  private static String FLYWAY_BASELINE_VERSION = "flyway.baselineVersion";
  private static String FLYWAY_BASELINE_ON_MIGRATE = "flyway.baselineOnMigrate";
  
  private final List<String> registeredListeners = new LinkedList<>();

  private final Config config;

  private HikariDataSource dataSource;

  public DrowsyDataSource() {
    this(ConfigFactory.getConfig().filterPrefix("jdbc"));
  }

  public DrowsyDataSource(Config config) {
    this.config = config;
  }

  @Override
  public void start() {
    initDataSource();
    setupConfigListeners();
  }

  @Override
  public void stop() {
    teardownConfigListeners();
    if (dataSource != null) {
      dataSource.close();
    }
  }

  @Override
  public boolean isRunning() {
    boolean isClosed = dataSource.isClosed();
    if (isClosed) {
      return false;
    }

    Connection connection = null;
    try {
      connection = getConnection();
      return true;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      return false;
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          LOGGER.error(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return dataSource.getConnection(username, password);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return dataSource.getLoginTimeout();
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return dataSource.getLogWriter();
  }

  @Override
  public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return dataSource.getParentLogger();
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return dataSource.isWrapperFor(iface);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    dataSource.setLoginTimeout(seconds);
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    dataSource.setLogWriter(out);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return dataSource.unwrap(iface);
  }

  private HikariDataSource createDataSource() {
    HikariConfig hikariConfig = jindyToHikari();
    return new HikariDataSource(hikariConfig);
  }

  private HikariConfig jindyToHikari() {
    HikariConfig result = new HikariConfig();
    result.setDataSourceClassName(config.getString(DATASOURCECLASSNAME));
    result.setJdbcUrl(config.getString(JDBCURL));
    result.setUsername(config.getString(USERNAME));
    result.setPassword(config.getString(PASSWORD));
    result.setConnectionTimeout(config.getInt(CONNECTIONTIMEOUT, 30000));
    result.setIdleTimeout(config.getInt(IDLETIMEOUT, 600000));
    result.setMaxLifetime(config.getInt(MAXLIFETIME, 1800000));
    result.setConnectionTestQuery(config.getString(CONNECTIONTESTQUERY));
    result.setMinimumIdle(config.getInt(MINIMUMIDLE, 1));
    result.setMaximumPoolSize(config.getInt(MAXIMUMPOOLSIZE, 10));
    result.setPoolName(config.getString(POOLNAME));
    result.setInitializationFailTimeout(config.getLong(INITIALIZATIONFAILTIMEOUT, 1));
    result.setIsolateInternalQueries(config.getBoolean(ISOLATEINTERNALQUERIES, false));
    result.setAllowPoolSuspension(config.getBoolean(ALLOWPOOLSUSPENSION, false));
    result.setRegisterMbeans(config.getBoolean(REGISTERMBEANS, false));
    result.setCatalog(config.getString(CATALOG));
    result.setConnectionInitSql(config.getString(CONNECTIONINITSQL));
    String driverClassName = config.getString(DRIVERCLASSNAME);
    if (driverClassName != null) {
      result.setDriverClassName(driverClassName);
    }
    result.setTransactionIsolation(config.getString(TRANSACTIONISOLATION));
    result.setValidationTimeout(config.getInt(VALIDATIONTIMEOUT, 5000));
    result.setLeakDetectionThreshold(config.getInt(LEAKDETECTIONTHRESHOLD, 0));
    result.setAutoCommit(isAutoCommit());
    result.setReadOnly(isReadOnly());
    return result;
  }

  private void initDataSource() {
    this.dataSource = createDataSource();
    migrate();
  }

  private void migrate() {
    if (isFlywayBypass()) {
      return;
    }
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource);
    String baseline = config.getString(FLYWAY_BASELINE_VERSION);
    flyway.setBaselineOnMigrate(config.getBoolean(FLYWAY_BASELINE_ON_MIGRATE, false));
    if (baseline != null) {
      flyway.setBaselineVersionAsString(baseline);
    }
    flyway.migrate();
  }

  private void setupConfigListeners() {
    // require reboot
    Arrays
        .asList(DATASOURCECLASSNAME, JDBCURL, USERNAME, PASSWORD, AUTOCOMMIT, CONNECTIONTIMEOUT, POOLNAME,
            CONNECTIONTESTQUERY, INITIALIZATIONFAILTIMEOUT, ISOLATEINTERNALQUERIES, ALLOWPOOLSUSPENSION, READONLY,
            REGISTERMBEANS, CATALOG, CONNECTIONINITSQL, DRIVERCLASSNAME, TRANSACTIONISOLATION, LEAKDETECTIONTHRESHOLD,
            FLYWAY_BYPASS, FLYWAY_BASELINE_VERSION, FLYWAY_BASELINE_ON_MIGRATE)
        .stream().forEach(p -> {
          listen(config, p, this::onConnectionPropertyChanged);          
        });

    // hot swappable
    listen(config, MAXIMUMPOOLSIZE, p -> dataSource.setMaximumPoolSize(config.getInt(MAXIMUMPOOLSIZE, 10)));
    listen(config, MINIMUMIDLE, p -> dataSource.setMinimumIdle(config.getInt(MINIMUMIDLE, 1)));
    listen(config, IDLETIMEOUT, p -> dataSource.setIdleTimeout(config.getInt(IDLETIMEOUT, 600000)));
    listen(config, MAXLIFETIME, p -> dataSource.setMaxLifetime(config.getInt(MAXLIFETIME, 1800000)));
    listen(config, VALIDATIONTIMEOUT, p -> dataSource.setValidationTimeout(config.getInt(VALIDATIONTIMEOUT, 5000)));
  }
  
  
  private void listen(Config config, String property, PropertyChangedCallback callback) {
    String listenerId = config.listen(property, callback);
    registeredListeners.add(listenerId);
  }

  private void teardownConfigListeners() {
    for(String listenerId : registeredListeners) {
      config.unListen(listenerId);
    }
    registeredListeners.clear();
  }

  private void onConnectionPropertyChanged(String prop) {
    LOGGER.info("DataSource Configuration changed. Creating new datasource...");
    try {
      HikariDataSource oldDataSource = dataSource;
      initDataSource();
      oldDataSource.close();
    } catch (HikariPool.PoolInitializationException e) {
      LOGGER.error("Error initializing backend", e);
      if (dataSource != null && !dataSource.isClosed()) {
        dataSource.close();
      }
    }
  }

  protected boolean isAutoCommit() {
    return config.getBoolean(AUTOCOMMIT, false);
  }

  protected boolean isReadOnly() {
    return config.getBoolean(READONLY, false);
  }

  protected boolean isFlywayBypass() {
    return config.getBoolean(FLYWAY_BYPASS, false);
  }

}
