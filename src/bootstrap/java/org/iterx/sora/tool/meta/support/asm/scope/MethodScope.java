package org.iterx.sora.tool.meta.support.asm.scope;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.support.asm.Scope;
import org.iterx.sora.tool.meta.util.TypeVisitor;

public final class MethodScope implements Scope<MethodScope> {

    private final TypeVisitor.Access access;
    private final TypeVisitor.Modifier[] modifiers;
    private final String methodName;
    private final Type<?> returnType;
    private final Type<?>[] argumentTypes;
    private final Type<?>[] exceptionTypes;

    public MethodScope(final TypeVisitor.Access access,
                       final TypeVisitor.Modifier[] modifiers,
                       final String methodName,
                       final Type<?> returnType,
                       final Type<?>[] argumentTypes,
                       final Type<?>[] exceptionTypes) {
        this.access = access;
        this.modifiers = modifiers;
        this.methodName = methodName;
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
        this.exceptionTypes = exceptionTypes;
    }

    public TypeVisitor.Access getAccess() {
        return access;
    }

    public TypeVisitor.Modifier[] getModifiers() {
        return modifiers;
    }

    public String getMethodName() {
        return methodName;
    }

    public Type<?> getReturnType() {
        return returnType;
    }

    public Type<?>[] getArgumentTypes() {
        return argumentTypes;
    }

    public Type<?>[] getExceptionTypes() {
        return exceptionTypes;
    }
}
