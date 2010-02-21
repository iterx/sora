package org.iterx.sora.tool.meta.statement;

import org.iterx.sora.tool.meta.Types;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.Stack;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;

public class ReturnVariableStatement implements Statement<ReturnVariableStatement> {

    private final String variableName;

    public ReturnVariableStatement(final String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public static class ReturnStatementAsmCompiler extends AsmCompiler<MethodVisitor, AsmCompiler.StatementContext<ReturnVariableStatement>, ReturnVariableStatement> {

        public ReturnStatementAsmCompiler() {
            super(MethodVisitor.class, ReturnVariableStatement.class);
        }

        public void compile(final MethodVisitor methodVisitor, final StatementContext<ReturnVariableStatement> context) {
            final ReturnVariableStatement returnVariableStatement = context.getStatement();
            final Stack stack = context.getStack();
            final MethodDeclaration methodDeclaration = (MethodDeclaration) context.getDeclarationContext().getDeclaration();

            if(methodDeclaration.getReturnType() != Types.VOID_TYPE) {
                methodVisitor.visitVarInsn(stack.getType(returnVariableStatement.getVariableName()).getOpcode(ILOAD),
                                           stack.getIndex(returnVariableStatement.getVariableName()));
            }
            else methodVisitor.visitInsn(ACONST_NULL);

            methodVisitor.visitInsn(methodDeclaration.getReturnType().getOpcode(IRETURN));
        }
    }
}