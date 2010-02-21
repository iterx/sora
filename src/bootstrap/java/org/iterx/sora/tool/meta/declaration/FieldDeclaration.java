package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_FINAL;

public class FieldDeclaration extends AbstractDeclaration<FieldDeclaration> {

    private final Type fieldType;
    private final String fieldName;

    public FieldDeclaration(final String fieldName, final Type fieldType) {
        super(ACC_PUBLIC|ACC_PROTECTED|ACC_PRIVATE|ACC_FINAL, ACC_PRIVATE);
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

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
}
