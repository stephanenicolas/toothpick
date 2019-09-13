<!-- Continue from top -->

## Release version 3.1.0 (Sep 13th, 2019)
 * Adding method getRootScope to Toothpick and KTP. Thanks to @code-twister for the this contribution!

## Release version 3.0.2 (Aug 30th, 2019)
 * Fixing issue #362: Improving IDE autocompletion for Binding API.
 * Improving KTP class autocompletion.
 * Trigger member injection even when delegates are used. This will allow to use method injection and inject parent dependencies on roots using KTP.

## Release version 3.0.1 (Aug 27th, 2019)
 * Fixing issue #360: Member injectors contains and invalid reference to the parent member injector if it contains a generic. Thanks to @zawadz88 for the this contribution!

## Release version 3.0.0 (Aug 2nd, 2019)
 * Enable field injection with qualifiers for KTP
 * Adding toothpick-sample

## Release version 3.0.0-RC1 (July 29th, 2019)
 * TP3 specs can be find at https://github.com/stephanenicolas/toothpick/issues/225
 * KTP specs can be found at https://github.com/stephanenicolas/toothpick/issues/316. Thx to @chenthorne and @dpreussler for their contributions to the Kotlin extensions of TP.
 * Scope annotations are not backward compatible with version 2. In version 2, scope annotations had 2 meaning: @ActivitySingleton meant
   both that a class could only be injected in a scope that supported this annotation and that the class would provide singletons of this scope.
   In TP 3, it is now more fine grained: @ActivityScope means a class can only be injected in a scope that supports this annotation 
   and it can be combined with @Singleton or not to denote that a class produces singletons of this scope or not.
 * The scope API is now a fluent API. It is possible to define all scopes and sub scope in the same method chain, and to configure them
   at the same time. See the new samples. https://github.com/stephanenicolas/toothpick/issues/217 and https://github.com/stephanenicolas/toothpick/issues/218
 * Kotlin is now a first class citizen of TP 3. KTP supports both field and constructor injection. Extensions are published to make things
   even more kotlin friendly. KTP doesn't offer method injections. KTP and TP are compatible. You can migrate classes to Kotlin incrementally
   in a java app using TP3.
 * Support for releasable singletons. TP can release @Releasable annotated singletons under memory pressure to save memory.
 * TP (java) supports method injections for methods throwing exceptions https://github.com/stephanenicolas/toothpick/issues/200
 * Support for package-protected classes https://github.com/stephanenicolas/toothpick/issues/227
 * Extension package for AndroidX life cycle allow to close a scope automatically when a FragmentActivity is closed. See sample.
 * Extension package for AndroidX view models allow to inject a view model and get things injected in a view model automatically. See sample. 
 * The binding API now is absolutely equivalent to the expressivity of the annotations. The 2 are strictly compatible.

## Release version 2.1.0 (Feb 4th, 2019)

* Add Toothpick.isScopeOpen method to check whether a scope is already open. Thx to terrakok for this.

## Release version 2.0.0 (Feb 1st, 2019)

* support incremental annotation processing. All processors are now incremental and isolating.
* remove all registries, TP 2.x doesn't use registries any more. They were actually slower than not using them after
we reworked on registries to enable obfuscation. Reflection is now used to load the factories and member injectors,
this is normal (and all other annotation processor based libs do that too) and there is no impact on performance.
* support multiple rounds of annotation processing.
* generated classes now use `__` instead of `$$` to make TP more compliant with some tools.
* removed the generate ContextSingleton annotation
* smoothie now binds the ClipClipboardManager service. Thx to Cody Henthrone for this. Sorry we were late merging this !
* smoothie now contains non support and non android x bindings. We have introduced 2 new artifacts:
smoothie-support and smoothie-androidx that respectively support the old legacy support library and the new android X libs).
* toothpick-testing now only contains core testing classes. New artifacts have been introduced to support junit 4 nd junit 5 (resp. : toothpick-testing-junit4, toothpick-testing-junit5)
* added a consumer proguard file

## Release version 1.1.3 (March 10th, 2018)

* Fix issue 146: generate registries in a way that is compatible with obfuscation.
Obfuscated apps can now use TP registries. Thx to Pavel Shmakov for this contribution !

## Release version 1.1.2 (Feb. 27th, 2018)
* Fix issue #283: Registries are not deterministic and it breaks compilation caching (for gradle)
* Fix issue #261. Better handling of non supported injected types (like primitives).
Thx to Alexey Ershov for the detailed bug report.

## Release version 1.1.1 (Oct. 13th, 2017)
* Fix issue #258, #256, #253: remove dependency to JSR 250 annotations, fix missing overrides.

## Release version 1.1.0 (Oct. 10th, 2017)

* Welcome to Cody Henthorne and thanks for your first contributions !
* Solves issue #232: Make it possible to reset a single scope. Useful for automation testing when we want to reset the scope used to install test modules.
* Add sample for integration tests.
* Modernisation of the build, JVM tools and CI
* Fix issue #251. Give more details in errors when installing modules
* Fix issue #253. Add Generated annotation to generated classes (for error prone)

## Release version 1.0.8 (August 22nd 2017)

A more debuggable release of TP with better error messages to help tracking bugs in TP usage. 
We also allow devs to clean their builds by letting them fail the builds instead of issuing warnings.

* Solves issue #228: Add scope name to error message: The instance provided by the lazy/provider...
* Solves issue #229: Add parent scope names to error message: No binding was defined for class %s and name %s in scope %s and its parents.
* Solves issue #196: Change ContextSingleton retention policy to RUNTIME & deprecate it.
* Solves issue #227: Let TP use injection on non public classes.
* Solves issue #207: Add an option to fail the build if TP can't create a factory for a class.
* Solves issue #202: Fix error message.
* Solves issue #236: Add an option to fail the build if an injected method is not package private.

## Release version 1.0.7 (June 19th 2017)

* Solves issue [#222](https://github.com/stephanenicolas/toothpick/issues/222) installTestModules should override previous bindings in the same scope.

## Release version 1.0.6 (Mar 25th 2017)

* Allow @Inject annotated constructors to send Exceptions #199

## Release version 1.0.5 (Jan 24th 2017)

* Smoothie module, changing Android dependency scope to optional.

## Release version 1.0.4 (Jan 23th 2017)

* Solves issue [#161](https://github.com/stephanenicolas/toothpick/issues/161) Forcing custom Scope Annotations to use Runtime Retention.
* Solves issue [#176](https://github.com/stephanenicolas/toothpick/issues/176) Making toothpick-javax-annotations optional.
* Solves issue [#181](https://github.com/stephanenicolas/toothpick/issues/181) Code generated for the super MemberInjector field was not using the right FQN when super class is static.
* Solves issue [#182](https://github.com/stephanenicolas/toothpick/issues/182) Removing usage of String.replace() inside Registries, it is not efficient.
* Solves issue [#186](https://github.com/stephanenicolas/toothpick/issues/186) Solving JDK8-JDK7 compatibility issue with ConcurrentHashMap.keySet().

## Release version 1.0.3 (Dec 19th 2016)

* Solves issue [#178](https://github.com/stephanenicolas/toothpick/issues/178) Make closed scopes invalid.

## Release version 1.0.2 (Nov 8th 2016)

* Solves issue [#170](https://github.com/stephanenicolas/toothpick/issues/170) ToothPickRule should reset after test method is finished.

## Release version 1.0.1 (Oct 26th 2016)

* Solves issue [#163](https://github.com/stephanenicolas/toothpick/issues/163) getAnnotationsByType is available from java 8.
* Adding configuration to detect multiple trees in the forest of scopes.

## Release version 1.0.0 (Oct 4th 2016)

* Solves issue [#117](https://github.com/stephanenicolas/toothpick/issues/117) Factory code generator should strip the generic part of dependencies.
* Solves issue [#118](https://github.com/stephanenicolas/toothpick/issues/118) Support custom annotations.
* Solves issue [#119](https://github.com/stephanenicolas/toothpick/issues/119) Adding a warning when injected methods have public or protected visibility
* Solved issue [#158](https://github.com/stephanenicolas/toothpick/issues/158) Removing last round warnings during annotation processing

## Release version 1.0.0-RC10 (Sep 29th 2016)

* Solves issue [#110](https://github.com/stephanenicolas/toothpick/issues/110) Adding support for the SuppressWarnings annotation to remove missing constructor warning.
* Solves issue [#111](https://github.com/stephanenicolas/toothpick/issues/111) Changing factory to get the target scope only when needed.
* Solves issue [#149](https://github.com/stephanenicolas/toothpick/issues/149): add methods to create singleton and producers producing singletons via binding programmatic API. Refine the meaning of the scope() method.
* Solves issue [#150](https://github.com/stephanenicolas/toothpick/issues/150): bindings are now allowed when the target of the binding uses an annotation that is supported by the scope where the binding is installed.

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
