package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.Type;

import java.util.Arrays;

public final class ConstructorDeclaration extends Declaration<ConstructorDeclaration> {

    public static final Type<ClassMetaType>[] EMPTY_EXCEPTION_TYPES = new Type[0];
    public static final Type[] EMPTY_CONSTRUCTOR_TYPES = new Type[0];
    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT, FINAL }

    private final Type<?>[] constructorTypes;
    private Type<ClassMetaType>[] exceptionTypes;
    private Access access;
    private Modifier[] modifiers;

    private ConstructorDeclaration(final Type... constructorTypes) {
        this.constructorTypes = constructorTypes;
        this.access = Access.PUBLIC;
        this.modifiers = EMPTY_MODIFIERS;
    }

    public static ConstructorDeclaration newConstructorDeclaration(final Type<?>... constructorTypes) {
        assertType(constructorTypes);
        return new ConstructorDeclaration(constructorTypes);
    }

    public Type<?>[] getConstructorTypes() {
        return constructorTypes;
    }

    public Type<ClassMetaType>[] getExceptionTypes() {
        return exceptionTypes;
    }

    public ConstructorDeclaration setExceptionTypes(final Type<ClassMetaType>... exceptionTypes) {
        assertType(exceptionTypes);
        this.exceptionTypes = exceptionTypes;
        return this;
    }

    public Access getAccess() {
        return access;
    }

    public ConstructorDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public ConstructorDeclaration setModifiers(final Modifier... modifiers) {
        assertModifiers(modifiers);
        this.modifiers = modifiers;
        return this;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(constructorTypes);
    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() && Arrays.equals(constructorTypes, ((ConstructorDeclaration) object).constructorTypes));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("ConstructorDeclaration: ").
                append(Arrays.toString(constructorTypes)).
                toString();
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
    public Collection<Statement> getStatements() {
        return statements.statements;
    }

    public ConstructorDeclaration define(final Statements statements) {
        this.statements.statements.clear();
        this.statements.statements.addAll(statements.statements);
        return this;
    }



    public static class ConstructorDeclarationCompiler extends AsmCompiler<ClassVisitor, AsmCompiler.DeclarationContext<ConstructorDeclaration>, ConstructorDeclaration> {

        public ConstructorDeclarationCompiler() {
            super(ClassVisitor.class, ConstructorDeclaration.class);
        }

        public void compile(final ClassVisitor classVisitor, final DeclarationContext<ConstructorDeclaration> context) {
            final ConstructorDeclaration constructorDeclaration = context.getDeclaration();
            final MethodVisitor methodVisitor = classVisitor.visitMethod(constructorDeclaration.getAccess(),
                                                                        "<init>",
                                                                        Type.getMethodDescriptor(Type.VOID_TYPE, constructorDeclaration.constructorTypes),
                                                                        null,
                                                                        null);
            methodVisitor.visitCode();
            compile(methodVisitor, context, constructorDeclaration.getStatements());
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }
    }
    */
}