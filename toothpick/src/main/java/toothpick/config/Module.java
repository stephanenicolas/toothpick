package toothpick.config;

import java.util.HashSet;
import java.util.Set;
import toothpick.ProvidesSingletonInScope;

/**
 * Defines a set of bindings, {@code Module} instances are installed in a scope.
 *
 * There are a few kinds of bindings :
 * <dl>
 * <dt><tt>bind(IFoo.class).to(Foo.class)</tt></dt>
 * <dd>binds an interface or class to an implementation class. <br/>
 * If class Foo is annotated with the {@link javax.inject.Singleton} annotation, the same instance is reused for each injection. <br/>
 * Otherwise, a new instance of class Foo will be produced for each injection.</dd>
 * <dt><tt>bind(IFoo.class).to(new Foo())</tt></dt>
 * <dd>binds an interface or class to a singleton of this class.</dd>
 * <dt><tt>bind(IFoo.class).toProvider(FooProvider.class)</tt></dt>
 * <dd>binds an interface or class to a provider class. <br/>
 * If class FooProvider is annotated with the {@link javax.inject.Singleton} annotation, the same instance of the provider is reused for each
 * injection. <br/>
 * If class FooProvider is annotated with the {@link ProvidesSingletonInScope} annotation, the same instance of Foo is reused for each
 * injection. <br/>
 * Otherwise, a new instance of class Foo will be produced by a new instance of FooProvider for each injection.</dd>
 * <dt><tt>bind(IFoo.class).toProvider(new FooProvider())</tt></dt>
 * <dd>binds an interface or class to a singleton provider. <br/>
 * The same instance of the provider is reused for each injection.
 * </dd>
 * <dt><tt>bind(Foo.class)</tt></dt>
 * <dd>binds a class to itself. <br/>
 * If class Foo is annotated with the {@link javax.inject.Singleton} annotation, this binding declares a singleton <b>in this scope</b>.
 * If such a binding is not present in any scope, {@link javax.inject.Singleton} annotated classes discovered at runtime will
 * be added to the root scope of the current scope.<br/>
 * If class Foo is not annotated, this binding makes no real sense.
 * </dd>
 * </dl>
 *
 * <p>
 * <b>Named &amp; unnamed bindings:</b><br>
 * All bindings can be defined without a name (unamed binding) or with a name. <br/>
 * The 2 name notations are strictly equivalent :
 * <ul>
 * <li>bind(Foo.class).withName("qurtz.Bar").to...
 * <li>bind(Foo.class).withName(qurtz.Bar.class).to... where {@code qurtz.Bar} is the annotation {@code @Bar} in package {@code qurtz}.
 * </ul>
 * Those named bindings can be injected in 2 equivalent ways :
 * <ul>
 * <li>@Inject @Named("qurtz.Bar") Foo foo;
 * <li>@Inject @qurtz.Bar Foo foo;
 * </ul>
 * </p>
 *
 * <p>
 * <b>{@link javax.inject.Singleton} in ToothPick:</b><br>
 * <em>ToothPick broadens the notion of Singleton : they do not necessarily belong to the root scope but can be declared in any scope.</em><br>
 * To declare a Singleton in a scope, just declare a binding for it in a module installed to this scope.
 * If there is no binding in any parent scope of the current scope when the class is first injected, then Singleton will be added to root scope.
 * This latest behavior allows to be fully compatible with more traditional Singleton (e.g. in Guice / RoboGuice).
 * <br>
 * To summarize : <em>a Singleton is guaranteed to be a unique instance in a given scope and its sub-scopes at any point in time.</em> <br/>
 * This means that there can be multiple instances of a Singleton the scope forest, or that singleton can be garbage collected and recreated
 * if their scope is closed.
 * </p>
 *
 * <p>
 * <b>{@link javax.inject.Provider} &amp; concurrency:</b><br>
 * Instances of {@link javax.inject.Provider} are guaranteed to be called in a thread safe way.
 * Developers don't have to deal with concurrency.
 * </p>
 * <p>
 * <b>{@link javax.inject.Provider} &amp; recycling of instances :</b><br>
 * If providers are bound by class, the annotations
 * {@link javax.inject.Singleton} and {@link ProvidesSingletonInScope} can be used to precise how instances of the provider
 * and instances produced by this provider are recycled across multiple injections. <br/>
 * If providers are bound by instances, then the provider has to deal by itself with the recycling of the instance it produces.
 * </p>
 */
public class Module {
  private Set<Binding> bindingSet = new HashSet<>();

  public <T> Binding<T> bind(Class<T> key) {
    Binding<T> binding = new Binding<>(key);
    bindingSet.add(binding);
    return binding;
  }

  public Set<Binding> getBindingSet() {
    return bindingSet;
  }
}
