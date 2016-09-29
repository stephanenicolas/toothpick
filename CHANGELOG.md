//Continue from top

## Release version 1.0.0-RC10 (XXX 2016)

* Solves issue [#110](https://github.com/stephanenicolas/toothpick/issues/110) Adding support for the SuppressWarnings annotation to remove missing constructor warning.
* Solves issue [#111](https://github.com/stephanenicolas/toothpick/issues/111) Changing factory to get the target scope only when needed.
* Solves issue [#150](https://github.com/stephanenicolas/toothpick/issues/150): bindings are now allowed when the target of the binding uses an annotation that is supported by the scope where the binding is installed.
* Solves issue [#149](https://github.com/stephanenicolas/toothpick/issues/149): add methods to create singleton and producers producing singletons via binding programmatic API. Refine the meaning of the scope() method.

## Release version 1.0.0-RC9 (Aug 26th 2016)

* Fixing Runtime check bug: Cycle should end even if lookupProvider fails.
* Updating Configuration API.

## Release version 1.0.0-RC7 (Aug 19th 2016)

* Fixing wrong binding for WifiManager.
* Fixing Smoothie sample presenter crash.
* Catching error about untyped Lazy or Provider.
* Fixing Lazies & Providers injected through constructors or methods.

## Release version 1.0.0-RC6 (Jul 22th 2016)

* Decrease retention to classes for JSR 330 annotations. This implies to repackage them and use the new package 
instead of JSR 330 pure annotations. It makes no differences for developers and TP can contribute to create slimer
first dexes with it.

## Release version 1.0.0-RC5 (Jul 18th 2016)

* Injecting named shared preferences is possible now.
* Registry getter method length warning removed.

## Release version 1.0.0-RC4 (Jul 5th 2016)

* fixing annotation processor compatibility issues with java 7.

## Release version 1.0.0-RC3 (Jun 23th 2016)

* Renaming binding methods for instances: to -> toInstance and toProvider -> toProviderInstance.
* Adding SearchManager and PackageManager to Smoothie.
* Renaming Smoothie Modules: ActivityModule -> SmoothieActivityModule, ApplicationModule -> SmoothieApplicationModule
and SupportActivityModule -> SmoothieSupportActivityModule.
* Bug fixes.

## Release version 1.0.0-RC2 (May 25th 2016)

* Optimistic factories are dropped. We realized we should restrain anntation processing only to classes that
the annotation processors do compile. The new system requires a bit more work for devs (basically annotating injected classes)
but it is far more aligned with javac compiler. The new system also reduces the usage of excludes options in annotation processing.
* Introduction of the concept of relaxed factory creation. A classes with a scope annotation or injected members will get a factory.
* Changing groupid to com.github.stephanenicolas.toothpick.

## Release version 1.0.0-RC1 (May 15th 2016)

* End of incubation period.
* TP is fully functional.
* Fully tested, code coverage is good.
* Benchmarks are pretty good.
* It has been reviewed by Carlos Sessa, Henri Tremblay and Michael Ma already. Thx reviewers !
* Wiki is complete.
* We are very close to a first API freeze now.

## First commit by 'snicolas' at '3/24/16 3:48 AM'
