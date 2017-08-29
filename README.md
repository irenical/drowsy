[![][maven img]][maven]
[![][travis img]][travis]
[![][codecov img]][codecov]
[![][codacy img]][codacy]

# Drowsy
Easy to use, lightweight java framework, built on top of JDBC that allows you to build high performance DB access for your Java applications, without the typical productivity and reliability penalties of not using an ORM.

![alt text][bear]  
Drowsy, the bear

**Things you can do with Drowsy**  
- Prevent resource leaks caused by developer oopsies, such as unclosed connections, result sets or statements  
- Build SQL queries with little boilerplate and good legibility, without sacrificing the ability to do weird stuff  
- Trivial ResultSet to Java objects mapping  
- Truly modular framework, allowing the developer to pick and choose what features of Drowsy to use, even in a multiple framework context  

**Things you cannot expect from Drowsy**  
- Hiding the relational model from your application  
- Non-JDBC data sources support  

## Drowsy Modules
Database access can quickly become a complex problem that in many cases cannot be solved with a one-size-fits-all solution. With this in mind, Drowsy is built upon 4 independent and strictly separated modules, each tackling a different concern. If you only want query building, there is no reason to include configuration, database migration, connection pooling or any other unnecessary dependencies in your project.  
Additionally and for convenience there is a 5th module that groups the other four and exposes an unified API. 

### DataSource
[![][maven-datasource img]][maven-datasource]  
A DataSource implementation with built-in dynamic configuration, allowing you to change any property at runtime. It's built upon [HikariCP](https://github.com/brettwooldridge/HikariCP), [Flyway](https://github.com/flyway/flyway) and [Jindy](https://github.com/irenical/jindy).  
[DataSource's module wiki](https://github.com/irenical/drowsy/wiki/DataSource)

### Transaction
[![][maven-transaction img]][maven-transaction]  
Models database operations at the connection and transaction level. Ensures resource deallocation, transaction commit and rollback on error.  
[Transaction's module wiki](https://github.com/irenical/drowsy/wiki/Transaction)

### Query
[![][maven-query img]][maven-query]  
Query builder classes that help you build prepared statements.  
[Query's module wiki](https://github.com/irenical/drowsy/wiki/Query)

### Mapper
[![][maven-mapper img]][maven-mapper]  
Simple ResultSet to bean mapping.  
[Mapper's module wiki](https://github.com/irenical/drowsy/wiki/Mapper)

### Drowsy
[![][maven img]][maven]  
The full bundle. Glues all the modules together in a simplified, easy to use API.  

## Usage
Note: You can use the modules separately, the examples bellow assume you are using the entire Drowsy bundle.  

**Life cycle**
```java
Drowsy drowsy = new Drowsy();
drowsy.start();
... your app doing stuff ...
drowsy.stop();
```

**Simple select**
```java
Query query = SelectBuilder.create("select * from people").build();
List<LegitPerson> got = drowsy.read(query, LegitPerson.class);
```

## Configuration
DataSource configuration is as in [HikariCP](https://github.com/brettwooldridge/HikariCP), prefixed with jdbc. Plus two Flyway specific configurations.
```properties
jdbc.jdbcUrl=jdbc:postgresql://localhost:5432/MyDB
jdbc.username=me
jdbc.password=hunter2
...

# Whether or not to use Flyway, defaults to false
jdbc.flyway.bypass=false
#If set and Flyway is active, only updates greater that this will be applied, defaults to null
jdbc.flyway.baselineVersion=3
```

Drowsy uses [Jindy](https://github.com/irenical/jindy) for configuration, so a Jindy binding is required. The easiest way to do this is probably by adding the following dependency to your application. You can then use System.setProperty() or a config.properties file in your resources to set Drowsy's configuration.
```
<dependency>
    <groupId>org.irenical.jindy</groupId>
    <artifactId>jindy-apacheconfig-impl</artifactId>
    <version>3.0.2</version>
</dependency>
```

[bear]:https://www.irenical.org/drowsy/bear.jpg "Sometimes, hibernating is just too much - Drowsy, the bear"

[maven]:http://search.maven.org/#search|gav|1|g:"org.irenical.drowsy"%20AND%20a:"drowsy"
[maven img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.drowsy/drowsy/badge.svg

[maven-datasource]:http://search.maven.org/#search|gav|1|g:"org.irenical.drowsy"%20AND%20a:"drowsy-datasource"
[maven-datasource img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.drowsy/drowsy-datasource/badge.svg

[maven-transaction]:http://search.maven.org/#search|gav|1|g:"org.irenical.drowsy"%20AND%20a:"drowsy-transaction"
[maven-transaction img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.drowsy/drowsy-transaction/badge.svg

[maven-query]:http://search.maven.org/#search|gav|1|g:"org.irenical.drowsy"%20AND%20a:"drowsy-query"
[maven-query img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.drowsy/drowsy-query/badge.svg

[maven-mapper]:http://search.maven.org/#search|gav|1|g:"org.irenical.drowsy"%20AND%20a:"drowsy-mapper"
[maven-mapper img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.drowsy/drowsy-mapper/badge.svg

[travis]:https://travis-ci.org/irenical/drowsy
[travis img]:https://travis-ci.org/irenical/drowsy.svg?branch=master

[codecov]:https://codecov.io/gh/irenical/drowsy
[codecov img]:https://codecov.io/gh/irenical/drowsy/branch/master/graph/badge.svg

[codacy]:https://www.codacy.com/app/tiagosimao/drowsy?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=irenical/drowsy&amp;utm_campaign=Badge_Grade
[codacy img]:https://api.codacy.com/project/badge/Grade/8a7f2277e24e4f619b13fb879c7c44b4
