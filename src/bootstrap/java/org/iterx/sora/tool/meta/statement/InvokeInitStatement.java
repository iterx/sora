package org.iterx.sora.tool.meta.statement;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.Stack;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class InvokeInitStatement implements Statement<InvokeInitStatement> {

    private final Type initType;
    private final String[] variableNames;

    public InvokeInitStatement(final Type initType, final String... variableNames) {
        this.initType = initType;
        this.variableNames = variableNames;
    }

    public Type getInitType() {
        return initType;
    }

    public String[] getVariableNames() {
        return variableNames;
    }

    public static class InvokeSuperStatementAsmCompiler extends AsmCompiler<MethodVisitor, AsmCompiler.StatementContext<InvokeInitStatement>, InvokeInitStatement> {

        public InvokeSuperStatementAsmCompiler() {
            super(MethodVisitor.class, InvokeInitStatement.class);
        }

        public void compile(final MethodVisitor methodVisitor, final StatementContext<InvokeInitStatement> context) {
            final InvokeInitStatement invokeInitStatement = context.getStatement();
            final Stack stack = context.getStack();

            methodVisitor.visitVarInsn(ALOAD, 0);
            for(final String variableName : invokeInitStatement.getVariableNames())
                methodVisitor.visitVarInsn(stack.getType(variableName).getOpcode(ILOAD),
                                           stack.getIndex(variableName));

            methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                          invokeInitStatement.getInitType().getInternalName(),
                                          "<init>",
                                          Type.getMethodDescriptor(Type.VOID_TYPE,
                                                                   stack.getTypes(invokeInitStatement.getVariableNames())));
        }
    }

}
