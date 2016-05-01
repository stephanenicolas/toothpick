package toothpick.concurrency.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

//from http://stackoverflow.com/a/2320465/693752
public class ClassCreator {
  ByteClassLoader byteClassLoader = new ByteClassLoader(getClass().getClassLoader());

  public Class<?> createClass(String className) throws ClassNotFoundException, IOException {
    String sourceFileName = className + ".java";
    FileWriter aWriter = new FileWriter(sourceFileName, true);
    aWriter.write("public class " + className + "{");
    aWriter.write("}\n");
    aWriter.flush();
    aWriter.close();
    compileIt(sourceFileName);
    FileInputStream fis = new FileInputStream(className+".class");
    byte[] buffer = new byte[3000];
    int read = fis.read(buffer);
    buffer = Arrays.copyOf(buffer, read);
    new File(className+ ".java").delete();
    new File(className+ ".class").delete();
    return byteClassLoader.defineClass(className, buffer);
  }

  private boolean compileIt(String sourceFileName) {
    String[] source = { new String(sourceFileName) };
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    new sun.tools.javac.Main(baos, source[0]).compile(source);
    // if using JDK >= 1.3 then use
    //   public static int com.sun.tools.javac.Main.compile(source);
    return (baos.toString().indexOf("error") == -1);
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
