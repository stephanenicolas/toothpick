package toothpick.config;

import javax.inject.Provider;
import javax.inject.Singleton;
import org.junit.Test;

public class BindingTest {

    @Test
    public void testBindingAPI() {
        //simple
        new Module().bind(String.class);

        //simple in scope
        new Module().bind(String.class)
            .inScope();

        //simple in custom scope
        new Module().bind(String.class)
            .inScope(Singleton.class);

        //simple with name
        new Module().bind(String.class)
            .withName("");

        //simple with name in scope
        new Module().bind(String.class)
            .withName("")
            .inScope();

        //simple with name and singleton
        new Module().bind(String.class)
            .withName("")
            .singleton();

        //simple with name and singleton in scope
        new Module().bind(String.class)
            .withName("")
            .singleton()
            .inScope();

        //simple with name and singleton and releasable
        new Module().bind(String.class)
            .withName("")
            .singleton()
            .releasable();

        //simple with name and singleton and releasable in scope
        new Module().bind(String.class)
            .withName("")
            .singleton()
            .releasable()
            .inScope();

        //simple singleton
        new Module().bind(String.class)
            .singleton();

        //simple singleton and releasable
        new Module().bind(String.class)
            .singleton()
            .releasable();

        //bind to class
        new Module().bind(String.class)
            .to(String.class);

        //bind to class with name
        new Module().bind(String.class)
            .withName("")
            .to(String.class);

        //bind to class with name in scope
        new Module().bind(String.class)
            .withName("")
            .to(String.class)
            .inScope();

        //bind to class with name and singleton
        new Module().bind(String.class)
            .withName("")
            .to(String.class)
            .singleton();

        //bind to class with name and singleton in scope
        new Module().bind(String.class)
            .withName("")
            .to(String.class)
            .singleton()
            .inScope();

        //bind to class with name and releasable singleton
        new Module().bind(String.class)
            .withName("")
            .to(String.class)
            .singleton()
            .releasable();

        //bind to class with name and releasable singleton
        new Module().bind(String.class)
            .withName("")
            .to(String.class)
            .singleton()
            .releasable()
            .inScope();

        //binding to provider instance
        new Module().bind(String.class)
            .toProviderInstance(new StringProvider());

        //binding to provider instance in scope
        new Module().bind(String.class)
            .toProviderInstance(new StringProvider())
            .inScope();

        //binding to provider instance with name
        new Module().bind(String.class)
            .withName("")
            .toProviderInstance(new StringProvider());

        //binding to provider instance and provides singleton
        new Module().bind(String.class)
            .toProviderInstance(new StringProvider())
            .providesSingleton();

        //binding to provider instance and provides singleton in scope
        new Module().bind(String.class)
            .toProviderInstance(new StringProvider())
            .providesSingleton()
            .inScope();

        //binding to provider instance and provides releasable singleton
        new Module().bind(String.class)
            .toProviderInstance(new StringProvider())
            .providesSingleton()
            .providesReleasable();

        //binding to provider instance and provides releasable singleton in scope
        new Module().bind(String.class)
            .toProviderInstance(new StringProvider())
            .providesSingleton()
            .providesReleasable()
            .inScope();

        //binding to provider and provides singleton and provider singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .singleton();

        //binding to provider and provides singleton and provider releasable singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .providesReleasable()
            .singleton();

        //binding to provider and provider singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .singleton();

        //binding to provider and provider releasable singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .singleton()
            .releasable();

        //binding to provider and provides singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton();

        //binding to provider and provides releasable singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .providesReleasable();

        //binding to provider and provides singleton and provider singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .singleton();

        //binding to provider and provides singleton and provider releasable singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .providesReleasable()
            .singleton();

        //binding to provider and provides singleton and provider releasable singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .providesReleasable()
            .singleton()
            .inScope();

        //binding to provider and provides singleton and provider releasable singleton
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .providesReleasable()
            .singleton()
            .releasable();

        //binding to provider and provides singleton and provider releasable singleton in scope
        new Module().bind(String.class)
            .toProvider(StringProvider.class)
            .providesSingleton()
            .providesReleasable()
            .singleton()
            .releasable()
            .inScope();
    }

    static class StringProvider implements Provider<String> {
        @Override
        public String get() {
            return null;
        }
    }
}
