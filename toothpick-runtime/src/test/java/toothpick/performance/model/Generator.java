package toothpick.performance.model;

import java.util.ArrayList;

public class Generator {

    public static String string() {
        return "String";
    }

    public static Integer integer() {
        return Integer.MAX_VALUE;
    }

    public static Double newDouble() {
        return Double.MAX_VALUE;
    }

    public static Object object() {
        return new ArrayList<>();
    }

}
