package org.iterx.sora.tool.meta.statement;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.Stack;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.PUTFIELD;


public class PutFieldStatement implements Statement<GetFieldStatement> {

    private final String fieldName;
    private final String variableName;

    public PutFieldStatement(final String fieldName, final String variableName) {
        this.fieldName = fieldName;
        this.variableName = variableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getVariableName() {
        return variableName;
    }


    public static class PutFieldStatementAsmCompiler extends AsmCompiler<MethodVisitor, AsmCompiler.StatementContext<PutFieldStatement>, PutFieldStatement> {

        public PutFieldStatementAsmCompiler() {
            super(MethodVisitor.class, PutFieldStatement.class);
        }

        public void compile(final MethodVisitor methodVisitor, final StatementContext<PutFieldStatement> context) {
            final PutFieldStatement putFieldStatement = context.getStatement();
            final ClassDeclaration classDeclaration = context.getDeclarationContext().getParent(ClassDeclaration.class).getDeclaration();
            final Stack stack = context.getStack();
            final Type type = classDeclaration.getType();
            final String fieldName = putFieldStatement.getFieldName();
            final String variableName = putFieldStatement.getVariableName();
            final Type fieldType = classDeclaration.getFieldDeclaration(fieldName).getFieldType();

            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(stack.getType(variableName).getOpcode(ILOAD), stack.getIndex(variableName));
            methodVisitor.visitFieldInsn(PUTFIELD, type.getInternalName(), fieldName, fieldType.getDescriptor());
        }
    }
}
