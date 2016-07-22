//Continue from top

## Release version 1.0.0-RC6 (Jul 22th 2016)

* Decrease retention to classes for JSR 330 annotations. This implies to repackage them and use the new package 
instead of JSR 330 pure annotations. It makes no differences for developers and TP can contribute to create slimer
first dexes with it.

## Release version 1.0.0-RC5 (Jul 18th 2016)

* Injecting named shared preferences is possible now
* Registry getter method length warning removed

## Release version 1.0.0-RC4 (Jul 5th 2016)

* fixing annotation processor compatibility issues with java 7

## Release version 1.0.0-RC3 (Jun 23th 2016)

* renaming binding methods for instances: to -> toInstance and toProvider -> toProviderInstance
* adding SearchManager and PackageManager to Smoothie
* renaming Smoothie Modules: ActivityModule -> SmoothieActivityModule, ApplicationModule -> SmoothieApplicationModule
and SupportActivityModule -> SmoothieSupportActivityModule
* bug fixes

## Release version 1.0.0-RC2 (May 25th 2016)

* optimistic factories are dropped. We realized we should restrain anntation processing only to classes that 
the annotation processors do compile. The new system requires a bit more work for devs (basically annotating injected classes)
but it is far more aligned with javac compiler. The new system also reduces the usage of excludes options in annotation processing.
* introduction of the concept of relaxed factory creation. A classes with a scope annotation or injected members will get a factory.
* changing groupid to com.github.stephanenicolas.toothpick

## Release version 1.0.0-RC1 (May 15th 2016)

* end of incubation period
* TP is fully functional
* fully tested, code coverage is good
* benchmarks are pretty good
* it has been reviewed by carlos sessa, henri tremblay and michael ma already. Thx reviewers !
* wiki is complete
* we are very close to a first API freeze now.

## First commit by 'snicolas' at '3/24/16 3:48 AM'
