package org.iterx.sora.tool.meta.support.asm.scope;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.support.asm.Scope;
import org.iterx.sora.tool.meta.value.Variable;

public class FrameScope implements Scope<FrameScope> {

    private MetaClassLoader metaClassLoader;
    private ClassScope classScope;
    private MethodScope methodScope;
    private StackScope stackScope;

    private FrameScope() {}

    public MetaClassLoader getMetaClassLoader() {
        return metaClassLoader;
    }

    public ClassScope getClassScope() {
        return classScope;
    }

    public MethodScope getMethodScope() {
        return methodScope;
    }

    public StackScope getStackScope() {
        return stackScope;
    }

    public static FrameScope newFrameScope(final MetaClassLoader metaClassLoader, final ClassScope classScope) {
        final FrameScope frameScope = new FrameScope();
        frameScope.metaClassLoader = metaClassLoader;
        frameScope.classScope = classScope;
        return frameScope;
    }

    public static FrameScope newFrameScope(final MetaClassLoader metaClassLoader, final ClassScope classScope, final MethodScope methodScope) {
        final FrameScope frameScope = new FrameScope();
        frameScope.metaClassLoader = metaClassLoader;
        frameScope.classScope = classScope;
        frameScope.methodScope = methodScope;
        frameScope.stackScope = newStackScope(methodScope.getArgumentTypes());
        return frameScope;
    }

    private static StackScope newStackScope(final Type<?>[] argumentTypes) {
        final StackScope stackScope = new StackScope();
        stackScope.push(Variable.THIS);
        for(int i = 0, size = argumentTypes.length; i != size; i++) stackScope.push(Variable.newVariable("arg" + i, argumentTypes[i]));
        return stackScope;
    }

}
