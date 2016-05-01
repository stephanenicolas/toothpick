package toothpick.concurrency.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

//from http://stackoverflow.com/a/2320465/693752
public class ClassCreator {
  private ByteClassLoader byteClassLoader = new ByteClassLoader(ClassCreator.class.getClassLoader());
  private final int CLASSES_COUNT = 1000;

  public Class[] allClasses;

  public ClassCreator() {
    try {
        allClasses = createClasses();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Class[] createClasses() throws ClassNotFoundException, IOException {
    Map<String, String> mapClassNameToJavaSource = new HashMap<>();
    for (int indexClass = 0; indexClass < CLASSES_COUNT; indexClass++) {
      String className = "Class_" + indexClass;
      String source = "public class " + className + "{}\n";
      mapClassNameToJavaSource.put(className, source);
    }
    List<Class> classes = new ArrayList<>();
    Map<String, byte[]> buffers = compile(mapClassNameToJavaSource);
    for (Map.Entry<String, byte[]> classNameToByteCodeEntry : buffers.entrySet()) {
      String className = classNameToByteCodeEntry.getKey();
      byte[] buffer = classNameToByteCodeEntry.getValue();
      Class clazz = byteClassLoader.defineClass(className, buffer);
      classes.add(clazz);
    }

    return classes.toArray(new Class[0]);
  }

  private Map<String, byte[]> compile(Map<String, String> mapClassNameToJavaSource) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

    List<JavaFileObject> compilationUnit = new ArrayList<>();
    for (Map.Entry<String, String> classNameToSourceEntry : mapClassNameToJavaSource.entrySet()) {
      JavaFileObject source = new JavaSourceFromString(classNameToSourceEntry.getKey(), classNameToSourceEntry.getValue());
      compilationUnit.add(source);
    }

    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit);
    if (!task.call()) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
      }
      return null;
    }
    System.out.println("No compile errors");
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
