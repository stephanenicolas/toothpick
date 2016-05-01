package toothpick.concurrency.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
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
    String sourceFileName = className + ".java";
    FileWriter aWriter = new FileWriter(sourceFileName, true);
    aWriter.write("public class " + className + "{\n");
    aWriter.write("}\n");
    aWriter.flush();
    aWriter.close();
    compileIt(sourceFileName);
    FileInputStream fis = new FileInputStream(className + ".class");
    byte[] buffer = new byte[3000];
    int read = fis.read(buffer);
    buffer = Arrays.copyOf(buffer, read);
    Class<?> aClass = byteClassLoader.defineClass(className, buffer);
    new File(className + ".java").delete();
    new File(className + ".class").delete();
    return aClass;
  }

  private boolean compileIt(String sourceFileName) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

    Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File(sourceFileName)));
    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit);
    if (!task.call()) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
      }
      return false;
    }
    System.out.println("No compile errors for " + sourceFileName);
    return true;
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
}
