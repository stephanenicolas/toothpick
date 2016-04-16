package toothpick.compiler;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

public class CodeGeneratorUtil {
  public static String getGeneratedFQNClassName(TypeElement typeElement) {
    return getGeneratedPackageName(typeElement) + "."+ getGeneratedSimpleClassName(typeElement);
  }

  public static String getGeneratedSimpleClassName(TypeElement typeElement) {
    String result = typeElement.getSimpleName().toString();
    //deals with inner classes
    while (typeElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      result = typeElement.getEnclosingElement().getSimpleName().toString() + "$" + result;
      typeElement = (TypeElement) typeElement.getEnclosingElement();
    }
    return result;
  }

  public static String getSimpleClassName(ClassName className) {
    String result = "";
    java.util.List<String> simpleNames = className.simpleNames();
    for (int i = 0; i < simpleNames.size(); i++) {
      String name = simpleNames.get(i);
      result += name;
      if(i!=simpleNames.size()-1) {
        result += ".";
      }
    }
    return result;
  }


  public static String getGeneratedPackageName(TypeElement typeElement) {
    //deals with inner classes
    while (typeElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      typeElement = (TypeElement) typeElement.getEnclosingElement();
    }
    return typeElement.getEnclosingElement().toString();
  }
}
