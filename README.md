[![][maven img]][maven]
[![][travis img]][travis]
[![][codecov img]][codecov]
[![][codacy img]][codacy]

# Drowsy
Easy to use, lightweight java framework, built on top of JDBC that allows you to build high performance DB access for your Java applications, without the typical productivity and reliability penalties of not using an ORM.

![alt text][bear] Drowsy, the bear

<b>Things you can do with Drowsy</b>  
- Prevent resource leaks caused by developer oopsies, such as unclosed connections, result sets or statements  
- Build SQL queries with little boilerplate and good legibility, without sacrificing the ability to do weird stuff  
- Trivial ResultSet to Java objects mapping  
- Truly modular framework, allowing the developer to pick and choose what features of Drowsy to use, even in a multiple framework context  

<b>Things you cannot expect from Drowsy</b>  
- Hiding the relational model from your application  
- Non-JDBC data sources support  

<h2>Drowsy Modules</h2>
As of now, Drowsy is still a single project.
```maven
<dependency>
  <groupId>org.irenical.drowsy</groupId>
  <artifactId>drowsy</artifactId>
  <version>0.0.8</version>
</dependency>
```
In it you can find the following modules:
<h3>DataSource</h3>
[org.irenical.drowsy.datasource]  
A DataSource implementation with built-in dynamic configuration, allowing you to change any property at runtime. It's built uppon [HikariCP](https://github.com/brettwooldridge/HikariCP), [Flyway](https://github.com/flyway/flyway) and [Jindy](https://github.com/irenical/jindy).

<h3>Transaction</h3>
[org.irenical.drowsy.transaction]  
Models database operations at the connection and transaction level. Ensures resource deallocation, transaction commit and rollback on error.

<h3>Query</h3>
[org.irenical.drowsy.query]  
Query builder classes that help you build prepared statements.

<h3>Mapper</h3>
[org.irenical.drowsy.mapper]  
Simple ResultSet to bean mapping.

<h3>Drowsy</h3>
[org.irenical.drowsy]  
The full bundle. Glues all the modules together in a simplified, easy to use API.

## Usage
Note: You can use the modules separately, these examples bellow apply when you're using the entire Drowsy bundle.  

<b>Life cycle</b>
```java
Drowsy drowsy = new Drowsy();
drowsy.start();
... your app doing stuff ...
drowsy.stop();
```

<b>One liner select</b>
```java
List<LegitPerson> got = drowsy.read(SelectBuilder.create("select * from people").build(), LegitPerson.class);
```

## Configuration
DataSource configuration is as in [HikariCP](https://github.com/brettwooldridge/HikariCP), prefixed with jdbc. Plus two Flyway specific configurations.
```properties
jdbc.url=jdbc:postgresql://localhost:5432/MyDB
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
    <version>2.1.0</version>
</dependency>
```

[bear]:https://www.irenical.org/drowsy/bear.jpg "Sometimes, to hibernate is just too much - Drowsy, the bear"

[maven]:http://search.maven.org/#search|gav|1|g:"org.irenical.drowsy"%20AND%20a:"drowsy"
[maven img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.drowsy/drowsy/badge.svg

[travis]:https://travis-ci.org/irenical/drowsy
[travis img]:https://travis-ci.org/irenical/drowsy.svg?branch=master

[codecov]:https://codecov.io/gh/irenical/drowsy
[codecov img]:https://codecov.io/gh/irenical/drowsy/branch/master/graph/badge.svg

[codacy]:https://www.codacy.com/app/tiagosimao/drowsy?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=irenical/drowsy&amp;utm_campaign=Badge_Grade
[codacy img]:https://api.codacy.com/project/badge/Grade/8a7f2277e24e4f619b13fb879c7c44b4
