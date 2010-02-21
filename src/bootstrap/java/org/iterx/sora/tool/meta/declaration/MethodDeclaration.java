package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.statement.Statement;
import org.iterx.sora.tool.meta.statement.Statements;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.Types;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Collection;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class MethodDeclaration extends AbstractDeclaration<MethodDeclaration> {

    private final String methodName;
    private final Type returnType;
    private final Type[] argumentTypes;

    private final Statements statements;

/*
    public MethodDeclaration() {
        //TODO: reflect -> based on inlined body of extending class
        this(null, null, null);
    }
*/

    public MethodDeclaration(final Type returnType,
                             final String methodName,
                             final Type[] argumentTypes) {
        super(ACC_PUBLIC|ACC_PROTECTED|ACC_PRIVATE|ACC_FINAL, ACC_PUBLIC);
        this.statements = new Statements();
        this.methodName = methodName;
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public Type[] getArgumentTypes() {
        return argumentTypes;
    }

    public boolean isInterface() {
        return true;
    }

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
            if(methodDeclaration.getReturnType() == Types.VOID_TYPE) methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }
    }
}
