package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.support.asm.AsmCompiler;
import org.iterx.sora.tool.meta.support.asm.AsmExtractor;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PutFieldInstructionTest {

    private MetaClassLoader asmExtractorMetaClassLoader;
    private MetaClassLoader asmCompilerMetaClassLoader;
    private AsmExtractor asmExtractor;
    private AsmCompiler asmCompiler;

    @Test
    public void shouldCompileAndExtractGetFieldInstruction() {
        //TODO: paramertise test -> one for each type...
        final ClassDeclaration expectedClassDeclaration = newClassDeclaration(asmCompilerMetaClassLoader, Type.LONG_TYPE);

        final byte[] bytes = asmCompiler.compile(expectedClassDeclaration);
        final ClassDeclaration actualClassDeclaration = (ClassDeclaration) asmExtractor.extract(bytes);
        //TODO: write reflective matcher...
        Assert.assertEquals(expectedClassDeclaration, actualClassDeclaration);
    }

    @Before
    public void setUp() {
        asmCompilerMetaClassLoader = new MetaClassLoader();
        asmExtractorMetaClassLoader = new MetaClassLoader();
        asmCompiler = new AsmCompiler(asmCompilerMetaClassLoader);
        asmExtractor = new AsmExtractor(asmExtractorMetaClassLoader);
    }

    private ClassDeclaration newClassDeclaration(final MetaClassLoader metaClassLoader, final Type type) {
        final String className = toName(type.getName(), "Field", "Test");
        final String fieldName = toName(type.getName(), "Field");
        final String methodName = toName("put", type.getName(), "Field");
        final ClassDeclaration classDeclaration = ClassDeclaration.newClassDeclaration(metaClassLoader,
                                                                                       ClassMetaType.newType(metaClassLoader, className));

        classDeclaration.add(FieldDeclaration.newFieldDeclaration(fieldName, type));
        classDeclaration.add(MethodDeclaration.newMethodDeclaration(methodName, type).
                setReturnType(Type.VOID_TYPE).
                add(PutFieldInstruction.newPutFieldInstruction(fieldName, Value.newValue("arg0"))));
        return classDeclaration;
    }

    private static String toName(final String... names) {
        final StringBuilder stringBuilder = new StringBuilder(names[0]);
        for(int i = 1, size = names.length; i < size; i++) stringBuilder.append(names[i].toUpperCase().substring(0, 1)).append(names[i].substring(1));
        return stringBuilder.toString();
    }
}