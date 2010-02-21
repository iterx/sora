package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.statement.Statement;
import org.iterx.sora.tool.meta.statement.Statements;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Collection;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.RETURN;


public class ConstructorDeclaration extends AbstractDeclaration<ConstructorDeclaration> {

    private final Type[] constructorTypes;
    private final Statements statements;

    public ConstructorDeclaration(final Type... constructorTypes) {
        super(ACC_PUBLIC|ACC_PROTECTED|ACC_PRIVATE, ACC_PUBLIC);
        this.statements = new Statements();
        this.constructorTypes = constructorTypes;
    }

    public Type[] getConstructorTypes() {
        return constructorTypes;
    }

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
}