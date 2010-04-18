package org.iterx.sora.tool.meta.support.asm.scope;

import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.support.asm.Scope;
import org.iterx.sora.tool.meta.util.TypeVisitor;

public final class ClassScope implements Scope<ClassScope> {

    private final TypeVisitor.Access access;
    private final TypeVisitor.Modifier[] modifiers;
    private final Type<?>[] interfaceTypes;
    private final Type<?> superType;
    private final Type<?> type;


    public ClassScope(final TypeVisitor.Access access,
                      final TypeVisitor.Modifier[] modifiers,
                      final Type<?> type,
                      final Type<?> superType,
                      final Type<?>[] interfaceTypes) {
        this.access = access;
        this.modifiers = modifiers;
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
    }

    public TypeVisitor.Access getAccess() {
        return access;
    }

    public TypeVisitor.Modifier[] getModifiers() {
        return modifiers;
    }

    public Type<?>[] getInterfaceTypes() {
        return interfaceTypes;
    }

    public Type<?> getSuperType() {
        return superType;
    }

    public Type<?> getType() {
        return type;
    }
}
