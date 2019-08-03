/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.config;

import java.util.HashSet;
import java.util.Set;
import toothpick.ProvidesSingleton;

/**
 * Defines a set of bindings, {@code Module} instances are installed in a scope.
 *
 * <p>There are a few kinds of bindings :
 *
 * <dl>
 *   <dt><tt>bind(IFoo.class).to(Foo.class)</tt>
 *   <dd>binds an interface or class to an implementation class. <br>
 *       If class Foo is annotated with the {@link javax.inject.Singleton} annotation, the same
 *       instance is reused for each injection. <br>
 *       Otherwise, a new instance of class Foo will be produced for each injection.
 *   <dt><tt>bind(IFoo.class).to(new Foo())</tt>
 *   <dd>binds an interface or class to a singleton of this class.
 *   <dt><tt>bind(IFoo.class).toProvider(FooProvider.class)</tt>
 *   <dd>binds an interface or class to a provider class. <br>
 *       If class FooProvider is annotated with the {@link javax.inject.Singleton} annotation, the
 *       same instance of the provider is reused for each injection. <br>
 *       If class FooProvider is annotated with the {@link ProvidesSingleton} annotation, the same
 *       instance of Foo is reused for each injection. <br>
 *       Otherwise, a new instance of class Foo will be produced by a new instance of FooProvider
 *       for each injection.
 *   <dt><tt>bind(IFoo.class).toProvider(new FooProvider())</tt>
 *   <dd>binds an interface or class to a singleton provider. <br>
 *       The same instance of the provider is reused for each injection.
 *   <dt><tt>bind(Foo.class)</tt>
 *   <dd>binds a class to itself. <br>
 *       If class Foo is annotated with the {@link javax.inject.Singleton} annotation, this binding
 *       declares a singleton <b>in this scope</b>. If such a binding is not present in any scope,
 *       {@link javax.inject.Singleton} annotated classes discovered at runtime will be added to the
 *       root scope of the current scope.<br>
 *       If class Foo is not annotated, this binding makes no real sense.
 * </dl>
 *
 * <p><b>Named &amp; unnamed bindings:</b><br>
 * All bindings can be defined without a name (unamed binding) or with a name. <br>
 * The 2 name notations are strictly equivalent :
 *
 * <ul>
 *   <li>bind(Foo.class).withName("qurtz.Bar").to...
 *   <li>bind(Foo.class).withName(qurtz.Bar.class).to... where {@code qurtz.Bar} is the annotation
 *       {@code @Bar} in package {@code qurtz}.
 * </ul>
 *
 * Those named bindings can be injected in 2 equivalent ways :
 *
 * <ul>
 *   <li>@Inject @Named("qurtz.Bar") Foo foo;
 *   <li>@Inject @qurtz.Bar Foo foo;
 * </ul>
 *
 * <p><b>{@link javax.inject.Singleton} in ToothPick:</b><br>
 * <em>ToothPick broadens the notion of Singleton : they do not necessarily belong to the root scope
 * but can be declared in any scope.</em><br>
 * To declare a Singleton in a scope, just declare a binding for it in a module installed to this
 * scope. <br>
 * To summarize : <em>a Singleton is guaranteed to be a unique instance in a given scope and its
 * sub-scopes at any point in time.</em> <br>
 * This means that there can be multiple instances of a Singleton the scope forest, or that
 * singleton can be garbage collected and recreated if their scope is closed and re-opened.
 *
 * <p>*
 *
 * <p><b>{@link toothpick.Releasable} in ToothPick:</b><br>
 * Toothpick allows to release singletons that are declared releasable.
 *
 * <p><b>{@link javax.inject.Provider} &amp; concurrency:</b><br>
 * Instances of {@link javax.inject.Provider} are guaranteed to be called in a thread safe way.
 * Developers don't have to deal with concurrency.
 *
 * <p><b>{@link javax.inject.Provider} &amp; recycling of instances :</b><br>
 * If providers are bound by class, the annotations {@link javax.inject.Singleton} and {@link
 * ProvidesSingleton} can be used to precise how instances of the provider and instances produced by
 * this provider are recycled across multiple injections. <br>
 * If providers are bound by instances, then the provider has to deal by itself with the recycling
 * of the instance it produces.
 */
public class Module {
  private Set<Binding> bindingSet = new HashSet<>();

  public <T> Binding<T>.CanBeNamed bind(Class<T> key) {
    Binding<T> binding = new Binding<>(key);
    bindingSet.add(binding);
    return binding.new CanBeNamed();
  }

  public Set<Binding> getBindingSet() {
    return bindingSet;
  }
}
