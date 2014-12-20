# storm-gen #

Simple ORM for Android SQLite

### Community ###

Use the [issue tracker](https://github.com/turbomanage/storm-gen/issues) to report bugs or request enhancements, and [StackOverflow with tag storm-gen](http://stackoverflow.com/questions/tagged/storm-gen) for assistance. Join the [storm-gen G+ community](https://plus.google.com/u/0/communities/111849422096213317275) for project news and feature discussion.

### Setup (Eclipse / ADT) ###
 1. Download storm-api.jar from the [latest release](https://github.com/turbomanage/storm-gen/releases/latest) and add to your Android project's libs folder so it will be on the project's build path.
 1. Download storm-apt.jar from the [latest release](https://github.com/turbomanage/storm-gen/releases/latest) and add to your annotation factory classpath (in Eclipse, project properties | Java Compiler > Annotation Processing > Factory Path). Also check the box to enable annotation processing. Note that the annotation processing preference has disappeared from ADT 23. To restore it, go to Help | Install New Software, select the Juno repository, and install Programming > Eclipse Java Development Tools.

### Setup (Android Studio) ###
The lib is available on Maven Central. Add the android-apt plugin and stORM dependencies to build.gradle. Log4j and javax.persistence are needed if you use the JPA @Entity annotation instead of stORM's @Entity.

```
// top-level build.gradle

buildscript {
    repositories {
        mavenCentral()
     }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.10.+'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.2'
    }
}

// app module build.gradle

apply plugin: 'android'
apply plugin: 'android-apt'

...

dependencies {
        apt 'com.turbomanage.storm:storm-impl:0.98'
        compile 'com.android.support:appcompat-v7:19.1.0'
        compile 'log4j:log4j:1.2.17'
        compile 'javax.persistence:persistence-api:1.0'
        compile 'com.turbomanage.storm:storm-api:0.98'
}
```

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

### Advanced features ###

#### BaseDaoClass ####

All your storm-generated Dao classes extend by default the abstract class SQliteDao:

    public class SimpleEntityDao extends SQLiteDao<SimpleEntity>{ ... }

If you would like to add some extra methods you can create a class that extends SQliteDao to be used instead:

    public abstract class ExtendingSQLiteDao<T> extends SQLiteDao<T> {
       public ExtendingSQLDao(Context ctx) {
          super(ctx);
       }
       // we are adding a count method to all entities that will use this Base Dao Class
       public int count(){
          Cursor cursor = query(null, null);
          int count = cursor.getCount();
          cursor.close();
          return count;
       }
    }

Then use the attribute baseDaoClass of the @Entity annotation:

    @Entity(baseDaoClass = ExtendingSQLiteDao.class)
    public class SimpleEntity { ... }
    
You can then see that your Base Dao Class is now used:

    public class SimpleEntityDao extends ExtendingSQLiteDao<SimpleEntity>{ ... }


### Troubleshooting ###
In Eclipse / ADT, generated classes are saved in the .apt_generated folder. To see this folder in Eclipse, go to the view pane settings | Filters..., and uncheck .* resources.

Whenever you add or change entities in an existing app, don't forget to increment the @Database version in your DatabaseHelper class in order to upgrade the schema.

Due to differences in the annotation processing lifecycle in various environments, changes to code may not take effect immediately. In some cases, such as deleting an entity class, you may need to reset stORM. Whenever you want a clean build, delete the file .apt_generated/stormEnv and clean the project. This will trigger all-new generation from scratch.
