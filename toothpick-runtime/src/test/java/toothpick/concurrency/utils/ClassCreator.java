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
package toothpick.concurrency.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

// from http://stackoverflow.com/a/2320465/693752

/**
 * We create classes dynamically with this class. It can seem a bit far fetched. We could also have
 * enumerated a thousand real classes from the JDK or create them by hand for real. But this is more
 * scalable and allow more testing. We also have full control on the classes.
 *
 * <p>They are used as injection parameters and bindings.
 */
public class ClassCreator {
  private ByteClassLoader byteClassLoader =
      new ByteClassLoader(ClassCreator.class.getClassLoader());
  private static final int CLASSES_COUNT = 1000;

  public Class[] allClasses;

  public ClassCreator() {
    try {
      allClasses = createClasses();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Impossible to create all classes.", e);
    }
  }

  private Class[] createClasses() throws ClassNotFoundException, IOException {
    Map<String, String> mapClassNameToJavaSource = new HashMap<>();
    for (int indexClass = 0; indexClass < CLASSES_COUNT; indexClass++) {
      String className = "Class_" + indexClass;
      String source = String.format("public class %s {}\n", className, className);
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
    InMemoryJavaFileManager fileManager =
        new InMemoryJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

    List<JavaFileObject> compilationUnit = new ArrayList<>();
    for (Map.Entry<String, String> classNameToSourceEntry : mapClassNameToJavaSource.entrySet()) {
      JavaFileObject source =
          new JavaSourceFromString(
              classNameToSourceEntry.getKey(), classNameToSourceEntry.getValue());
      compilationUnit.add(source);
    }

    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit);
    if (!task.call()) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        System.out.format(
            "Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource());
      }
      return null;
    }
    return fileManager.getAllBuffers();
  }

  public static class ByteClassLoader extends URLClassLoader {

    public ByteClassLoader(ClassLoader parent) {
      super(new URL[0], parent);
    }

    public Class<?> defineClass(final String name, byte[] classBytes)
        throws ClassNotFoundException {
      if (classBytes != null) {
        return defineClass(name, classBytes, 0, classBytes.length);
      }
      return null;
    }
  }

  class JavaSourceFromString extends SimpleJavaFileObject {
    final String code;

    JavaSourceFromString(String name, String code) {
      super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }
}
