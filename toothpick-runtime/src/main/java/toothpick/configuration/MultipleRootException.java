package toothpick.configuration;

import toothpick.Scope;

public class MultipleRootException extends RuntimeException {
    public MultipleRootException(Scope scope) {
        super(String.format("Scope %s is a new root in TP scope forest. Only one root is allowed in this configuration.", scope.getName()));
    }
}
