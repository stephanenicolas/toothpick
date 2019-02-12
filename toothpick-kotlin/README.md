# Toothpick Extensions  
Toothpick Extensions

### Base Extensions
```kt
// Inject from Scope
scope.inject(obj)

// Inject with Kotlin Class
scope.getInstance(KotlinClass::class)
// Inject with reified type
val type: KotlinClass = scope.getInstance()

// In a Module, bind with Kotlin Class
module.bind(KotlinClass::class)...

// Inject dependency without specifying type
val dependency: Dependency = inject(scope)
val dependency: Dependency = inject(scope = scope, name = "Name")
val dependency: Dependency = inject(scope = scope, annotationName = AnnotationClass::class)

// Lazy inject dependency via delegate
val dependency: Dependency by injectLazy(scope)

// Lazy inject with lazy scope assignment
private lateinit var lateScope: Scope
private lateinit var lateName: String

private val dependency: Dependency by inject { scope = lateScope }
private val namedDependency: Dependency by inject(name = "Name") { scope = lateScope }
private val lateNamedDependency: Dependency by { scope = lateScope, name = lateName }

// ...later
scope = Toothpick.openScope(...)
lateName = "myLateName"
dependency.foo()           // Injection happens here
namedDependency.foo()      // Injection happens here
lateNamedDependency.foo()  // Injection happens here

// Lazy inject with scope search
class ScopeSearch : HasScope {
    override lateinit var scope: Scope
        protected set

    private val dependency: Dependency by inject()
    private val namedDependency: Dependency by inject(name = "Name")

    init {
        scope = Toothpick.openScopes(this)
    }
}
```

### Lazy inject dependencies at call site
```kt
val scope = Toothpick.openScopes(application, this)
val dependency = Dependency(otherDependency = inject(scope)) 
val orDependency = Dependency(otherDependency = scope.getInstance())  
```

### Module Creation Infix Functions and DSL

#### Infix Functions
```kt
// Set Provider Instance or Class on Binding
bind(Dep::class) providedBy DepProvider()
bind(Dep::class) providedBy DepProvider::class

// Set Provider Instance or Class on Binding and provide as Singleton
bind(Dep::class) singletonProvidedBy DepProvider()
bind(Dep::class) singletonPovidedBy DepProvider::class

// Provide via Instance
bind(Dep::class) providedBy Dep()

// Provide type with provider of another type
bind(IDep::class) providedAs Dep::class

// Naming
bind(Dep::class) named "DepName" providedBy DepProvider() // or class or dep instance
bind(Dep::class) named DepNameAnnotation::class providedBy DepProvider() // or class or dep instance
```
	
#### DSL
```kt
@Qualifier  
annotation class NamedPrefs  
  
fun exampleModule(application: Application) = module {  
  // Provided by Provider  
  Picasso::class providedBy PicassoProvider()         // Instance  
  Picasso::class providedBy PicassoProvider::class // or Class  
  
  // Provide singleton by provider 
  Picasso::class singletonProvidedBy PicassoProvider()      // Instance
  Picasso::class singletonProvidedBy PicassoProvider::class // or Class  
  
  // Provide with instance 
  Picasso::class providedBy Picasso.Builder().build()  
  // Provide interface as actual implementation  
  InjectContentInterface::class providedAs InjectedContent::class  
  
  // Provided named version  
  SharedPreferences::class named NamedPrefs::class providedBy SharedPreferencesProvider(application, "NamedPrefs")  
  SharedPreferences::class named "NamedPrefs" providedBy SharedPreferencesProvider(application, "NamedPrefs")  
}  
  
// Annotations here still work too  
// @Singleton @ProvidesSingletonInScope  
class PicassoProvider : Provider<Picasso> {  
    override fun get(): Picasso {  
        return Picasso.Builder().apply { image = R.drawable.ic_launcher_foreground }.build()  
    }  
}
```
