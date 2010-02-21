package org.iterx.sora.tool.meta.statement;

import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.Stack;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ISTORE;

public class AssignStatement implements Statement<AssignStatement> {

    private final String variableName;
    private final Type variableType;
    private final Statement statement;

    public AssignStatement(final String variableName, final Type variableType, final Statement statement) {
        this.variableName = variableName;
        this.variableType = variableType;
        this.statement = statement;
    }

    public String getVariableName() {
        return variableName;
    }

    public Type getVariableType() {
        return variableType;
    }

    public Statement getStatement() {
        return statement;
    }

    public static class AssignStatementAsmCompiler extends AsmCompiler<MethodVisitor, AsmCompiler.StatementContext<AssignStatement>, AssignStatement> {

        public AssignStatementAsmCompiler() {
            super(MethodVisitor.class, AssignStatement.class);
        }

        public void compile(final MethodVisitor methodVisitor, final StatementContext<AssignStatement> context) {
            final AssignStatement assignStatement = context.getStatement();
            final Stack stack = context.getStack();

            compile(methodVisitor, context, assignStatement.getStatement());

            methodVisitor.visitVarInsn(assignStatement.getVariableType().getOpcode(ISTORE),
                                       stack.push(assignStatement.getVariableName(),
                                                  assignStatement.getVariableType()));
        }
    }
}
