# Toothpick  *(a.k.a T.P. like a teepee)*

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
      <img src="https://img.shields.io/maven-central/v/com.github.stephanenicolas.toothpick/toothpick.svg?style=flat"/></a>
      <br/>
    </td>
    <td  style="border:0px">
      <a alt="Android Dev Weekly" href="http://androidweekly.net/issues/issue-207">
      <img src="https://img.shields.io/badge/Android%20Weekly-%23207-brightgreen.svg"/></a>
      <br/>
      <a alt="Android Arsenal" href="http://android-arsenal.com/details/1/3646">
      <img src="https://img.shields.io/badge/Android%20Arsenal-Toothpick-brightgreen.svg?style=flat"/></a>
      <br/>
      <a alt="Incap: isolating" href="https://github.com/gradle/gradle/blob/master/subprojects/docs/src/docs/userguide/java_plugin.adoc#isolating-annotation-processors">
      <img src="https://img.shields.io/badge/incap-isolating-4AC31D?style=flat"/></a>
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

           @ApplicationScope 
             /          |    \  
            /           |     \
           /            |      \
   @ViewModelScope      |   Service 2
         /              | 
        /            Service 1  
       /            
 @Activity1Scope
      /
     /
Activity 1
   /   \
  /   Fragment 2
 /
Fragment 1
```

Scopes offer to compartmentalize memory during the runtime of an app and prevent memory leaks.
All dependencies created via Toothpick, and available for injections, will be fully garbage collected when this scope is closed.
To learn more about scopes, read [TP wiki](https://github.com/stephanenicolas/toothpick/wiki/Scopes#what-is-a-scope-).

Toothpick is :
* pure java (Android support is provided: "Smoothie", Kotlin support is provided by "KTP")
* [fast](https://github.com/stephanenicolas/toothpick/wiki/FAQ#how-does-toothpick-perform-compared-to-dagger-2-), it doesn't use reflection but [annotation processing](https://github.com/stephanenicolas/toothpick/wiki/Factories-and-Member-Injectors)
* TP supports [incremental annotation processing (isolating)](https://github.com/gradle/gradle/blob/master/subprojects/docs/src/docs/userguide/java_plugin.adoc#isolating-annotation-processors).
* simple, flexible, extensible & powerful, [robust & tested](https://coveralls.io/github/stephanenicolas/toothpick?branch=master)
* thread safe
* [documented](https://github.com/stephanenicolas/toothpick/wiki) & [Open Source](https://raw.githubusercontent.com/stephanenicolas/toothpick/master/LICENSE)
* [scope safe](https://github.com/stephanenicolas/toothpick/wiki/Scope-Resolution) : it enforces leak free apps
* [test oriented](https://github.com/stephanenicolas/toothpick/blob/master/toothpick-sample/src/test/java/toothpick/sample/SimpleEntryPointTestWithRules.java) : it makes tests easier
* is a candidate of choice for Android or any other context based framework (such as web containers)

## Examples

This is the example:
* https://github.com/stephanenicolas/toothpick/tree/master/toothpick-sample

## Setup

The latest version of TP is provided by a badge at the top of this page.

For Android : 
```groovy
#android setup using gradle 5.5.1
buildscript {
  repositories {
    google()
    jcenter()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:3.4.x'
  }
}

...
#for java
dependencies {
  implementation 'com.github.stephanenicolas.toothpick:toothpick-runtime:3.x'
  // and for android -> implementation 'com.github.stephanenicolas.toothpick:smoothie-androidx:3.x'
  annotationProcessor 'com.github.stephanenicolas.toothpick:toothpick-compiler:3.x'

  //highly recommended
  testImplementation 'com.github.stephanenicolas.toothpick:toothpick-testing-junit5:3.x'
  testImplementation 'mockito or easymock'
}

#for kotlin
dependencies {
  implementation 'com.github.stephanenicolas.toothpick:ktp:3.x'
  kapt 'com.github.stephanenicolas.toothpick:toothpick-compiler:3.x'

  //highly recommended
  testImplementation 'com.github.stephanenicolas.toothpick:toothpick-testing-junit5:3.x'
  testImplementation 'mockito or easymock'
}

```

For java:
```xml
<!--java setup with maven -->
  <dependencies>
    <dependency>
      <groupId>com.github.stephanenicolas.toothpick</groupId>
      <artifactId>toothpick-compiler</artifactId>
      <version>3.x</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>com.github.stephanenicolas.toothpick</groupId>
      <artifactId>toothpick-runtime</artifactId>
      <version>3.x</version>
      <scope>compile</scope>
    </dependency>
    
    <!-- highly recommended-->
    <dependency> 
      <groupId>com.github.stephanenicolas.toothpick</groupId>
      <artifactId>toothpick-testing</artifactId>
      <version>3.x</version>
      <scope>test</scope>
    </dependency>
    <dependency>
    <easymock or mockito>
    </dependency>
  </dependencies>
```
## Support

TP is actively maintained and we provide support to questions via the [Toothpick-di](https://stackoverflow.com/questions/tagged/toothpick-di) tag on Stack Over Flow. 

Ask questions on Stack Over Flow while keeping the GitHub issue board for real issues. Thx in advance !

## Talks & Articles

* A Dependency Injection Showdown - [Pro Android Dev article](https://proandroiddev.com/a-dependency-injection-showdown-213339c76515)
* Toothpick 3 — a hidden gem in the DI world - [Medium article](https://medium.com/swlh/toothpick-3-a-hidden-gem-194ae6ee8671)
* How I learned to love unit testing with Toothpick - [Groupon's medium blog article](https://medium.com/groupon-eng/how-i-learned-to-love-unit-testing-with-toothpick-13ad305b35d)
* Droidcon, Boston 2017 - [Slides](https://speakerdeck.com/dlemures/toothpick-a-fresh-approach-to-di-including-unit-testing) & [Video](https://news.realm.io/news/droidcon-boston-daniel-molinero-toothpick-dependency-injection-android/)
* Migrating off RoboGuice 3 (to Toothpick) - [Part 1](https://medium.com/@markchristopherng/migrating-off-roboguice-3-part-1-cee0875f6620) & [Part 2](https://medium.com/@markchristopherng/migrating-off-roboguice-3-part-2-c06644d2d1ef)
* Mobius, Saint Petersburg 2017 - [Slides](https://speakerdeck.com/stephanenicolas/tp-mobile-era-2016-final-compressed)
* Mobius, Saint Petersburg 2017 - DI frameworks & Internals [Slides](https://speakerdeck.com/stephanenicolas/comparing-di-frameworks)
* Android Makers, Paris 2017 - [Slides](https://speakerdeck.com/stephanenicolas/tp-mobile-era-2016-final-compressed) & [Video](https://www.youtube.com/watch?v=rn4EAzimslw)
* DroidCon, Kaigi 2017 - [Slides](https://speakerdeck.com/stephanenicolas/tp-mobile-era-2016-final-compressed)
* Andevcon, San Francisco 2016 - [Slides](https://speakerdeck.com/stephanenicolas/tp-mobile-era-2016-final-compressed)
* DroidCon, Krakow 2016 - TP Vs Dagger 2 Talk from Danny Preussler [slides](http://www.slideshare.net/dpreussler/demystifying-dependency-injection-dagger-and-toothpick)
* Mobile Era, Oslo 2016 - [Slides](https://speakerdeck.com/stephanenicolas/tp-mobile-era-2016-final-compressed)
* Droidcon, Berlin 2016 - [Slides](https://speakerdeck.com/dlemures/toothpick-and-dependency-injection) - [Sketch notes](https://twitter.com/TeresaHolfeld/status/743026908552663041)
* Android Leaks 2016 - [podcast in French](http://androidleakspodcast.com/2016/09/25/episode-4-de-la-dague-au-cure-dent-en-passant-par-un-petit-jus/)
* DevFest Belgium 2016 - [video in French](https://www.youtube.com/watch?v=ytBmu5ciPCQ)
* DevFest Siberia 2017 - [video in Russian](https://www.youtube.com/watch?v=EOFrA-MHbjU)
* DevFest North 2017 - [video in Russian](https://www.youtube.com/watch?v=t55nFVurCHw)
* [Toothpick: a simple DI for Android development (Russian)](https://medium.com/@alaershov/toothpick-di-android-1-intro-ru-151015616f0?source=friends_link&sk=ccfdda35cd968f1cd9986ab4a8cf0016)

# Wanna know more ?

Visit [Toothpick's wiki](https://github.com/stephanenicolas/toothpick/wiki) !

# Alternative Dependency Injection (DI) engines for Android

* ~~[RoboGuice](https://github.com/roboguice/roboguice)~~ (deprecated)
* ~~[Dagger 1](https://github.com/square/dagger)~~ (deprecated)
* [Dagger 2](https://github.com/google/dagger)
* [transfuse](http://androidtransfuse.org/)
* [lightsaber](https://github.com/MichaelRocks/lightsaber)
* ~~[tiger](https://github.com/google/tiger)~~ (deprecated)
* [feather](https://github.com/zsoltherpai/feather)
* [proton](https://github.com/hnakagawa/proton)
* [koin](https://github.com/InsertKoinIO/koin)
* [Kodein-DI](https://github.com/Kodein-Framework/Kodein-DI)

# Libs / Apps using TP 2

* [Okuki](https://github.com/wongcain/okuki) is a simple, hierarchical navigation bus and back stack for Android, with optional Rx bindings, and Toothpick DI integration.
* [KotlinWeather](https://github.com/ekamp/KotlinWeather) is a simple example of using ToothPick with Kotlin and gradle integration using kapt.

# Credits

TP 1 & 3 have been developped by Stephane Nicolas and Daniel Molinero Reguera.
Most of the effort on version 2 has been actively supported by Groupon. Thanks for this awesome OSS commitment !
