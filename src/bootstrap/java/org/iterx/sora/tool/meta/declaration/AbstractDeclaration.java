package org.iterx.sora.tool.meta.declaration;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

abstract class AbstractDeclaration<T extends AbstractDeclaration> implements Declaration<T> {

    private final int mask;
    private int access;

    protected AbstractDeclaration(final int mask, final int access) {
        this.mask = mask;
        this.access = access;
    }

    @SuppressWarnings("unchecked")
    public T setPublic() {
        access ^= (access & (ACC_PROTECTED | ACC_PRIVATE));
        access |= ACC_PUBLIC;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setProtected() {
        access ^= (access & (ACC_PUBLIC | ACC_PRIVATE));
        access |= ACC_PROTECTED;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setPrivate() {
        access ^= (access & (ACC_PUBLIC | ACC_PROTECTED));
        access |= ACC_PRIVATE;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setFinal(final boolean isFinal) {
        access = (isFinal)? (access | ACC_FINAL) : (access ^ ACC_FINAL);
        return (T) this;
    }

    protected int getAccess() {
        return access & mask;
    }
}