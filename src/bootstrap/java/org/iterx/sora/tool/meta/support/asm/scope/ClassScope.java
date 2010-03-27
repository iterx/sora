package org.iterx.sora.tool.meta.support.asm.scope;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.support.asm.Scope;

public final class ClassScope implements Scope<ClassScope> {

    private final Type<?> superType;
    private final Type<?> thisType;

    public ClassScope(final Type<?> superType, final Type<?> thisType) {
        this.superType = superType;
        this.thisType = thisType;
    }

    public Type<?> getSuper() {
        return superType;
    }


    public Type<?> getThis() {
        return thisType;
    }
}
