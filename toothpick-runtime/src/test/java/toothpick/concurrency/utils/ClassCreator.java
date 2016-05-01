package toothpick.concurrency.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

//from http://stackoverflow.com/a/2320465/693752
public class ClassCreator {
  private ByteClassLoader byteClassLoader = new ByteClassLoader(ClassCreator.class.getClassLoader());
  private final int CLASSES_COUNT = 1000;

  public final Class[] allClasses = new Class[CLASSES_COUNT];

  public ClassCreator() {
    try {
      for (int indexClass = 0; indexClass < CLASSES_COUNT; indexClass++) {
        allClasses[indexClass] = createClass("Class_" + indexClass);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Class<?> createClass(String className) throws ClassNotFoundException, IOException {
    String code = "public class " + className + "{}\n";
    Map<String, byte[]> buffers = compileIt(className, code);
    byte[] buffer = buffers.get(className);
    Class<?> aClass = byteClassLoader.defineClass(className, buffer);
    return aClass;
  }

  private Map<String, byte[]> compileIt(String className, String code) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

    JavaFileObject source = new JavaSourceFromString(className, code);
    Iterable<? extends JavaFileObject> compilationUnit = Arrays.asList(source);
    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit);
    if (!task.call()) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
      }
      return null;
    }
    System.out.println("No compile errors for " + className);
    return fileManager.getAllBuffers();
  }

  public static class ByteClassLoader extends URLClassLoader {

    public ByteClassLoader(ClassLoader parent) {
      super(new URL[0], parent);
    }

    public Class<?> defineClass(final String name, byte[] classBytes) throws ClassNotFoundException {
      if (classBytes != null) {
        return defineClass(name, classBytes, 0, classBytes.length);
      }
      return null;
    }
  }

  class JavaSourceFromString extends SimpleJavaFileObject {
    final String code;

    JavaSourceFromString(String name, String code) {
      super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }
}
