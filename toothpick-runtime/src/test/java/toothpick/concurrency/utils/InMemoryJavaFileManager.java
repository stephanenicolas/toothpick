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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

// from
// https://github.com/OpenHFT/Java-Runtime-Compiler/blob/master/compiler/src/main/java/net/openhft/compiler/CachedCompiler.java
public class InMemoryJavaFileManager implements JavaFileManager {
  private final StandardJavaFileManager fileManager;
  private final Map<String, ByteArrayOutputStream> buffers =
      new LinkedHashMap<String, ByteArrayOutputStream>();

  InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
    this.fileManager = fileManager;
  }

  public ClassLoader getClassLoader(Location location) {
    return fileManager.getClassLoader(location);
  }

  public Iterable<JavaFileObject> list(
      Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
    return fileManager.list(location, packageName, kinds, recurse);
  }

  public String inferBinaryName(Location location, JavaFileObject file) {
    return fileManager.inferBinaryName(location, file);
  }

  public boolean isSameFile(FileObject a, FileObject b) {
    return fileManager.isSameFile(a, b);
  }

  public boolean handleOption(String current, Iterator<String> remaining) {
    return fileManager.handleOption(current, remaining);
  }

  public boolean hasLocation(Location location) {
    return fileManager.hasLocation(location);
  }

  public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
      throws IOException {
    if (location == StandardLocation.CLASS_OUTPUT
        && buffers.containsKey(className)
        && kind == Kind.CLASS) {
      final byte[] bytes = buffers.get(className).toByteArray();
      return new SimpleJavaFileObject(URI.create(className), kind) {
        public InputStream openInputStream() {
          return new ByteArrayInputStream(bytes);
        }
      };
    }
    return fileManager.getJavaFileForInput(location, className, kind);
  }

  public JavaFileObject getJavaFileForOutput(
      Location location, final String className, Kind kind, FileObject sibling) throws IOException {
    return new SimpleJavaFileObject(URI.create(className), kind) {
      public OutputStream openOutputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buffers.put(className, baos);
        return baos;
      }
    };
  }

  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {
    return fileManager.getFileForInput(location, packageName, relativeName);
  }

  public FileObject getFileForOutput(
      Location location, String packageName, String relativeName, FileObject sibling)
      throws IOException {
    return fileManager.getFileForOutput(location, packageName, relativeName, sibling);
  }

  public void flush() throws IOException {
    // Do nothing
  }

  public void close() throws IOException {
    fileManager.close();
  }

  public int isSupportedOption(String option) {
    return fileManager.isSupportedOption(option);
  }

  public void clearBuffers() {
    buffers.clear();
  }

  public Map<String, byte[]> getAllBuffers() {
    Map<String, byte[]> ret = new LinkedHashMap<String, byte[]>(buffers.size() * 2);
    for (Map.Entry<String, ByteArrayOutputStream> entry : buffers.entrySet()) {
      ret.put(entry.getKey(), entry.getValue().toByteArray());
    }
    return ret;
  }
}
