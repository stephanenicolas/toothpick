package toothpick.configuration;

import toothpick.Factory;
import toothpick.MemberInjector;
import toothpick.config.Binding;

/**
 * Strategy pattern that allows to change various behaviors of Toothpick.
 * The default configuration is {@link #forProduction()} and {@link #enableReflection()}.
 * A custom configuration can be created and used by toothpick,
 * it is even possible to use a composition of the built-in configurations.
 */
public class Configuration implements RuntimeCheckConfiguration, ReflectionConfiguration {

  private ReflectionConfiguration reflectionConfiguration = new ReflectionOnConfiguration();
  private RuntimeCheckConfiguration runtimeCheckConfiguration = new RuntimeCheckOffConfiguration();

  /**
   * Performs many runtime checks. This configuration
   * reduces performance. It should be used only during development.
   * The checks performed are:
   * <ul>
   * <li>cycle detection: check that not 2 classes depend on each other. Note that if of them uses a Lazy instance
   * of the other or a Producer, then there is no such cycle.</li>
   * <li>illegal binding detection: check no scope annotated class is used as the target of a binding.</li>
   * </ul>
   *
   * @return a development configuration.
   */
  public static Configuration forDevelopment() {
    final Configuration configuration = new Configuration();
    configuration.runtimeCheckConfiguration = new RuntimeCheckOnConfiguration();
    return configuration;
  }

  /**
   * Performs no runtime checks. This configuration
   * is faster than {@link #forDevelopment()}.
   * It can be used in production.
   *
   * @return a production configuration.
   */
  public static Configuration forProduction() {
    final Configuration configuration = new Configuration();
    configuration.runtimeCheckConfiguration = new RuntimeCheckOffConfiguration();
    return configuration;
  }

  /**
   * Set enableReflection mode.
   * It is slower than {@link #disableReflection()} but it does not
   * need any additional setup of the annotation processors.
   * It can be used in production or development.
   *
   * @return a configuration set up to use reflection.
   */
  public Configuration enableReflection() {
    this.reflectionConfiguration = new ReflectionOnConfiguration();
    return this;
  }

  /**
   * Set reflection free mode.
   * It is faster than {@link #enableReflection()} but it needs
   * some additional setup of the annotation processors.
   * It can be used in production or development.
   *
   * @return an optimized reflection free configuration.
   */
  public Configuration disableReflection() {
    this.reflectionConfiguration = new ReflectionOffConfiguration();
    return this;
  }

  @Override
  public void checkIllegalBinding(Binding binding) {
    runtimeCheckConfiguration.checkIllegalBinding(binding);
  }

  @Override
  public void checkCyclesStart(Class clazz, String name) {
    runtimeCheckConfiguration.checkCyclesStart(clazz, name);
  }

  @Override
  public void checkCyclesEnd(Class clazz, String name) {
    runtimeCheckConfiguration.checkCyclesEnd(clazz, name);
  }

  @Override
  public <T> Factory<T> getFactory(Class<T> clazz) {
    return reflectionConfiguration.getFactory(clazz);
  }

  @Override
  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    return reflectionConfiguration.getMemberInjector(clazz);
  }
}
