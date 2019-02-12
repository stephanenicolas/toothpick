# Android focused Toothpick Extensions
```kt
// In in a View
customView.inject()
// Or in the view init, just:
inject()
```

### Delegate inject in Activity or Fragment with scope search
```kt
// Will inject with Toothpick.openScope(activityRef.application, activityRef)
class MyActivity : Activity() {
    private val dependency1: Dependency by inject()
    private val dependency2: Dependency by inject(named = "Name")
    private val dependency3: Dependency by inject(annotationName = AnnotationClass::class)
}

// Will inject with this.scope
class MyActivity : Activity(), HasScope {
    override lateinit var scope: Scope
        protected set
        
    private val dependency1: Dependency by inject()
    private val dependency2: Dependency by inject(named = "Name")
    private val dependency3: Dependency by inject(annotationName = AnnotationClass::class)

    override fun onCreate(savedInstanceState: Bundle?) {  
        super.onCreate(savedInstanceState)  
        scope = Toothpick.openScopes(application, this)  
    }
}

// Will inject with lazy provided scope
class MyActivity : Activity() {
    private lateinit var lateScope: Scope
    private val dependency1: Dependency by inject { scope = lateScope }
    private val dependency2: Dependency by inject(named = "Name") { scope = lateScope }
    private val dependency3: Dependency by inject(annotationName = AnnotationClass::class) { scope = lateScope }

    override fun onCreate(savedInstanceState: Bundle?) {  
        super.onCreate(savedInstanceState)  
        ...
        // Delayed scope creation for any reason
        lateScope = Toothpick.openScopes(application, this)  
        ...
        dependency1.foo() // Injects when called
    }
}
```

### Lazy inject dependencies at call site
```kt
class MyActivity : Activity() {
    private lateinit var scope: Scope
    
    override fun onCreate(savedInstanceState: Bundle?) {  
        super.onCreate(savedInstanceState)  
        scope = Toothpick.openScopes(application, this)
        
        // Inject instance of OtherDependency without needing it in the activity where it's not used
        val dependency = Dependency(otherDependency = inject())  
    }
}

```
