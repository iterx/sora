package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.type.Type;


public final class FieldDeclaration extends Declaration<FieldDeclaration> {

    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { VOLATILE, STATIC, FINAL }

    private final String fieldName;
    private Type<?> fieldType;
    private Access access;
    private Modifier[] modifiers;

    //private Modifier modifier; //Mark as volatile or transient
    //TODO: add value...

    private FieldDeclaration(final String fieldName) {
        this.access = Access.PRIVATE;
        this.modifiers = EMPTY_MODIFIERS;
        this.fieldName = fieldName;
    }

    public static FieldDeclaration newFieldDeclaration(final String fieldName, final Type<?> fieldType) {
        assertFieldName(fieldName);
        return new FieldDeclaration(fieldName).
                setFieldType(fieldType);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Type<?> getFieldType() {
        return fieldType;
    }

    public FieldDeclaration setFieldType(final Type<?> fieldType) {
        assertType(fieldType);
        this.fieldType = fieldType;
        return this;
    }

    public Access getAccess() {
        return access;
    }

    public FieldDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public FieldDeclaration setModifiers(final Modifier... modifiers) {
        assertModifiers(modifiers);
        this.modifiers = modifiers;
        return this;
    }

    @Override
    public int hashCode() {
        return fieldName.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() && fieldName.equals(((FieldDeclaration) object).fieldName));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("FieldDeclaration: ").
                append(fieldName).
                append(" ").
                append(fieldType).
                toString();
    }

    private static void assertFieldName(final String fieldName) {
        if(fieldName == null) throw new IllegalArgumentException("fieldName == null");
    }

    private static void assertType(final Type<?>... types) {
        if(types == null) throw new IllegalArgumentException("type == null");
        for(Type type : types) if(type == null) throw new IllegalArgumentException("type == null");
    }

    private static void assertAccess(final Access access) {
        if(access == null) throw new IllegalArgumentException("access == null");
    }

    private static void assertModifiers(final Modifier... modifiers) {
        if(modifiers == null) throw new IllegalArgumentException("modifiers == null");
    }


    /*
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_FINAL;

    public static class FieldDeclarationAsmCompiler extends AsmCompiler<ClassVisitor, AsmCompiler.DeclarationContext<FieldDeclaration>, FieldDeclaration> {

        public FieldDeclarationAsmCompiler() {
            super(ClassVisitor.class, FieldDeclaration.class);
        }

        public void compile(final ClassVisitor classVisitor, final DeclarationContext<FieldDeclaration> context) {
            final FieldDeclaration fieldDeclaration = context.getDeclaration();
            final FieldVisitor fieldVisitor = classVisitor.visitField(fieldDeclaration.getAccess(),
                                                                     fieldDeclaration.getFieldName(),
                                                                     fieldDeclaration.getFieldType().getDescriptor(),
                                                                     null,
                                                                     null);
            fieldVisitor.visitEnd();
        }
    }
    */
}
