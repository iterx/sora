package org.iterx.sora.tool.meta.statement;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;

public class GetFieldStatement implements Statement<GetFieldStatement> {

    private final String fieldName;

    public GetFieldStatement(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static class GetFieldStatementAsmCompiler extends AsmCompiler<MethodVisitor, AsmCompiler.StatementContext<GetFieldStatement>, GetFieldStatement> {

        public GetFieldStatementAsmCompiler() {
            super(MethodVisitor.class, GetFieldStatement.class);
        }

        public void compile(final MethodVisitor methodVisitor, final StatementContext<GetFieldStatement> context) {
            final GetFieldStatement getFieldStatement = context.getStatement();
            final ClassDeclaration classDeclaration = context.getDeclarationContext().getParent(ClassDeclaration.class).getDeclaration();
            final Type type = classDeclaration.getType();
            final String fieldName = getFieldStatement.getFieldName();
            final Type fieldType = classDeclaration.getFieldDeclaration(fieldName).getFieldType();

            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD,
                                         type.getInternalName(),
                                         fieldName,
                                         fieldType.getDescriptor());
        }
    }

}
