# storm-gen #

Simple ORM for Android SQLite

### Community ###

Use the [issue tracker](https://github.com/turbomanage/storm-gen/issues) to report bugs or request enhancements, and [StackOverflow with tag storm-gen](http://stackoverflow.com/questions/tagged/storm-gen) for assistance. Join the [storm-gen G+ community](https://plus.google.com/u/0/communities/111849422096213317275) for project news and feature discussion.

### Setup ###
 1. Download storm-api.jar from the [latest release](https://github.com/turbomanage/storm-gen/releases/latest) and add to your Android project's libs folder so it will be on the project's build path.
 1. Download storm-apt.jar from the [latest release](https://github.com/turbomanage/storm-gen/releases/latest) and add to your annotation factory classpath (in Eclipse, project properties | Java Compiler > Annotation Processing > Factory Path). Also check the box to enable annotation processing.

### Basic Usage ###
 1. Create a new class that extends com.turbomanage.storm.DatabaseHelper (it's in the storm-api jar). 
 1. Add @Database and supply a database name and version [example](https://github.com/turbomanage/storm-gen/blob/master/test/src/com/turbomanage/storm/TestDatabaseHelper.java)
 1. Override getUpgradeStrategy() to choose one of the available strategies (DROP_CREATE for new projects)
 1. Create a POJO class you want to persist to the database
 1. Make sure it has a field of type long named "id" or annotated with @Id
 1. Add the @Entity annotation to the class [example](https://github.com/turbomanage/storm-gen/blob/master/test/src/com/turbomanage/storm/entity/SimpleEntity.java)

You'll see 3 generated classes under .apt_generated (you might need to unfilter hidden resources in your IDE to see it):
 - a DbFactory class
 - a Dao class
 - a Table class

You can use the DAO like this:

    PersonDao dao = new PersonDao(getContext());
    long id = dao.insert(new Person());
    Person person = dao.get(id);

For more info, see the [unit tests](https://github.com/turbomanage/storm-gen/tree/master/test/src/com/turbomanage/storm/test) and the resources on the project home page.
### Integration ###
The lib is available on Maven Central. Add the android-apt plugin and stORM dependencies to build.gradle. Log4j and javax.persistence are needed if you use the JPA @Entity annotation instead of stORM's @Entity.

```
apply plugin: 'android'
apply plugin: 'android-apt'

buildscript {
    repositories {
        mavenCentral()
     }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.10.+'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.2'
    }
}

dependencies {
        apt 'com.turbomanage.storm:storm-impl:0.98'
        compile 'com.android.support:appcompat-v7:19.1.0'
        compile 'log4j:log4j:1.2.17'
        compile 'javax.persistence:persistence-api:1.0'
        compile 'com.turbomanage.storm:storm-api:0.98'
}
```
