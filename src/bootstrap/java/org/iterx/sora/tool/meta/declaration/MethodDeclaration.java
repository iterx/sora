package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.type.ClassMetaType;

import java.util.Arrays;

public final class MethodDeclaration extends Declaration<MethodDeclaration> {

    public static final Type<?>[] EMPTY_ARGUMENT_TYPES = new Type[0];
    public static final Type<ClassMetaType>[] EMPTY_EXCEPTION_TYPES = new ClassMetaType[0];
    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT, FINAL }

    private final String methodName;
    private final Type<?>[] argumentTypes;
    private Type<ClassMetaType>[] exceptionTypes;
    private Type<?> returnType;
    private Access access;
    private Modifier[] modifiers;

    private MethodDeclaration(final String methodName,
                              final Type<?>... argumentTypes) {

        this.returnType = Type.VOID_TYPE;
        this.exceptionTypes = EMPTY_EXCEPTION_TYPES;
        this.access = Access.PUBLIC;
        this.modifiers = EMPTY_MODIFIERS;
        this.methodName = methodName;
        this.argumentTypes = argumentTypes;
    }

    public static MethodDeclaration newMethodDeclaration(final String methodName, final Type<?>... argumentTypes) {
        assertMethodName(methodName);
        assertType(argumentTypes);
        return new MethodDeclaration(methodName, argumentTypes);
    }

    public String getMethodName() {
        return methodName;
    }

    public Type<?>[] getArgumentTypes() {
        return argumentTypes;
    }

    public Type<?> getReturnType() {
        return returnType;
    }

    public MethodDeclaration setReturnType(final Type<?> returnType) {
        assertType(returnType);
        this.returnType = returnType;
        return this;
    }
    public Type<ClassMetaType>[] getExceptionTypes() {
        return exceptionTypes;
    }

    public MethodDeclaration setExceptionTypes(final Type<ClassMetaType>... exceptionTypes) {
        assertType(exceptionTypes);
        this.exceptionTypes = exceptionTypes;
        return this;
    }
    
    public Access getAccess() {
        return access;
    }

    public MethodDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public MethodDeclaration setModifiers(final Modifier... modifiers) {
        assertModifiers(modifiers);
        this.modifiers = modifiers;
        return this;
    }

    @Override
    public int hashCode() {
        return 31 * methodName.hashCode() +  Arrays.hashCode(argumentTypes);

    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() &&
                methodName.equals(((MethodDeclaration) object).methodName) &&
                Arrays.equals(argumentTypes, ((MethodDeclaration) object).argumentTypes));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("MethodDeclaration: ").
                append(methodName).
                append(Arrays.toString(argumentTypes)).
                toString();
    }


    private static void assertMethodName(final String methodName) {
        if(methodName == null) throw new IllegalArgumentException("methodName == null");
    }

    private static void assertType(final Type<?>... types) {
        if(types == null) throw new IllegalArgumentException("type == null");
        for(Type<?> type : types) if(type == null) throw new IllegalArgumentException("type == null");
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

    public MethodDeclaration before(final Statements statements) {
        //TODO: append to beginning of statements
        throw new UnsupportedOperationException();
    }

    public MethodDeclaration define(final Statements statements) {
        this.statements.statements.clear();
        this.statements.statements.addAll(statements.statements);
        return this;
    }

    public MethodDeclaration after(final Statements statements) {
        //TODO: append to end of statements...
        throw new UnsupportedOperationException();
    }

    public static class MethodDeclarationAsmCompiler extends AsmCompiler<ClassVisitor, AsmCompiler.DeclarationContext<MethodDeclaration>, MethodDeclaration> {

        public MethodDeclarationAsmCompiler() {
            super(ClassVisitor.class, MethodDeclaration.class);
        }

        public void compile(final ClassVisitor classVisitor, final DeclarationContext<MethodDeclaration> context) {
            final MethodDeclaration methodDeclaration = context.getDeclaration();
            final MethodVisitor methodVisitor = classVisitor.visitMethod(methodDeclaration.getAccess(),
                                                                        methodDeclaration.getMethodName(),
                                                                        Type.getMethodDescriptor(methodDeclaration.getReturnType(),
                                                                                                 methodDeclaration.getArgumentTypes()),
                                                                        null,
                                                                        null);
            methodVisitor.visitCode();
            compile(methodVisitor, context, methodDeclaration.getStatements());
            if(methodDeclaration.getReturnType() == Type.VOID_TYPE) methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }
    }
*/
}
