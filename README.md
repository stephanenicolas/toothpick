#Toothpick  *(a.k.a T.P. like a teepee)*

<table style="border:0px">
  <tr  style="border:0px">
    <td width="125" style="border:0px">
      <img src="https://raw.github.com/stephanenicolas/toothpick/master/assets/logo.jpg" width="125px" /> 
    </td>
    <td  style="border:0px">
      <a alt="Build Status" href="https://travis-ci.org/stephanenicolas/toothpick">
      <img src="https://travis-ci.org/stephanenicolas/toothpick.svg?branch=master"/></a>
      <br/>
      <a alt="Coverage Status" href="https://coveralls.io/github/stephanenicolas/toothpick?branch=master">
      <img src="https://coveralls.io/repos/github/stephanenicolas/toothpick/badge.svg?branch=master"/></a>
      <br/>
      <a alt="License" href="https://raw.githubusercontent.com/stephanenicolas/toothpick/master/LICENSE">
      <img src="http://img.shields.io/:license-apache-blue.svg"/></a>
      <br/>
      <a alt="Maven Central" href="http://search.maven.org/#search|gav|1|g:'com.github.stephanenicolas.toothpick'%20AND%20a:'toothpick'">
      <img src="https://img.shields.io/maven-central/v/com.github.stephanenicolas.toothpick/toothpick.svg?maxAge=2592000"/></a>
      <br/>
      <a alt="Android Dev Weekly" href="http://androidweekly.net/issues/issue-207">
      <img src="https://img.shields.io/badge/Android%20Weekly-%23207-brightgreen.svg"/></a>
      <br/>
      <a alt="Android Arsenal" href="http://android-arsenal.com/details/1/3646">
      <img src="https://img.shields.io/badge/Android%20Arsenal-Toothpick-brightgreen.svg?style=flat"/></a>
    </td>
    <td>
      <a href="https://github.com/stephanenicolas/toothpick/wiki">
      Visit TP wiki !
      </a>
    </td>
  </tr>
</table>

## What is Toothpick ?
 
Toothpick is a scope tree based Dependency Injection (DI) library for Java.

It is a full-featured, runtime based, but reflection free, implementation of [JSR 330](https://github.com/stephanenicolas/toothpick/wiki/Relation-to-JSR-330).

## What does Toothpick offer ?

```
//a typical Toothpick scope tree during the execution of an Android app.

       @ApplicationSingleton 
         /              |    \  
        /               |     \
       /                |      \
   @PresenterSingleton  |   Service 2
         /              | 
        /            Service 1  
       /            
Activity 1
    /   \
   /   Fragment 2
  /
Fragment 1
```

Toothpick is :
* pure java
* [fast](https://github.com/stephanenicolas/toothpick/wiki/FAQ#how-does-toothpick-perform-compared-to-dagger-2-), it doesn't use reflection but [annotation processing](https://github.com/stephanenicolas/toothpick/wiki/Factories-and-Member-Injectors)
* simple, flexible, extensible & powerful, [robust & tested](https://coveralls.io/github/stephanenicolas/toothpick?branch=master)
* thread safe
* [documented](https://github.com/stephanenicolas/toothpick/wiki) & [Open Source](https://raw.githubusercontent.com/stephanenicolas/toothpick/master/LICENSE)
* [scope safe](https://github.com/stephanenicolas/toothpick/wiki/Scope-Resolution) : it enforces leak free apps
* [test oriented](https://github.com/stephanenicolas/toothpick/blob/master/toothpick-sample/src/test/java/toothpick/sample/SimpleEntryPointTestWithRules.java) : it makes tests easier
* it works very well with Android or any other context based framework (such as web containers)

Hey, Android Devs, you can use TP to [create MVP apps so easily](https://github.com/stephanenicolas/toothpick/blob/master/smoothie-sample/src/main/java/com/example/smoothie/RxMVPActivity.java) !


## Examples

Currently Toothpick has 2 sets of examples : 
* one [in pure Java](https://github.com/stephanenicolas/toothpick/tree/master/toothpick-sample)
* one [for Android](https://github.com/stephanenicolas/toothpick/tree/master/smoothie-sample)

## Setup
For Android : 
```groovy
#android setup using apt 
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
  }
}
dependencies {
  compile 'com.github.stephanenicolas.toothpick:toothpick-runtime:1.0.0-RC3'
  //and 'com.github.stephanenicolas.toothpick:smoothie:1.0.0-RC3'
  apt 'com.github.stephanenicolas.toothpick:toothpick-compiler:1.0.0-RC3'

  //highly recommended
  testCompile 'com.github.stephanenicolas.toothpick:toothpick-testing:1.0.0-RC3'
  testCompile 'mockito or easymock'
}
```

For java:
```xml
<!--java setup with maven -->
  <dependencies>
    <dependency>
      <groupId>com.github.stephanenicolas.toothpick</groupId>
      <artifactId>toothpick-compiler</artifactId>
      <version>1.0.0-RC3</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>com.github.stephanenicolas.toothpick</groupId>
      <artifactId>toothpick-runtime</artifactId>
      <version>1.0.0-RC3</version>
      <scope>compile</scope>
    </dependency>
    
    <!-- highly recommended-->
    <dependency> 
      <groupId>com.github.stephanenicolas.toothpick</groupId>
      <artifactId>toothpick-testing</artifactId>
      <version>1.0.0-RC3</version>
      <scope>test</scope>
    </dependency>
    <dependency>
    <easymock or mockito>
    </dependency>
  </dependencies>
```

## Talks

Droidcon Berlin 2016 - [Slides](https://speakerdeck.com/dlemures/toothpick-and-dependency-injection)

# Wanna know more ?

Visit [Toothpick's wiki](https://github.com/stephanenicolas/toothpick/wiki) !




