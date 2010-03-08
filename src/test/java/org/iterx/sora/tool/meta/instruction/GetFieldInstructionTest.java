package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.Declarations;
import org.iterx.sora.tool.meta.test.matcher.ClassDeclarationMatcher;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class GetFieldInstructionTest extends InstructionTestCase {

    private final Type<?> type;
    private final Object value;

    public GetFieldInstructionTest(final Type<?> type, final Object value) {
        this.type = type;
        this.value = value;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[]{ Type.LONG_TYPE, 0L },
                             new Object[]{ Type.INT_TYPE, 0 },
                             new Object[]{ Type.OBJECT_TYPE, null });
    }

    @Test
    public void shouldCompileInstruction() throws Throwable {
        final ClassDeclaration classDeclaration = newClassDeclaration(compileMetaClassLoader, type);
        final Class<?> cls = compileMetaClassLoader.loadClass(classDeclaration.getClassType());
        
        assertCompile(cls, type, value);
    }

    @Test
    public void shouldExtractInstruction() throws Throwable {
        final ClassDeclaration expectedClassDeclaration = newClassDeclaration(compileMetaClassLoader, type);
        extractMetaClassLoader.defineClass(expectedClassDeclaration);

        assertExtract(expectedClassDeclaration, extractMetaClassLoader.loadDeclaration(expectedClassDeclaration.getClassType()));
    }

    private ClassDeclaration newClassDeclaration(final MetaClassLoader metaClassLoader, final Type type) {
        final String typeName = (type.getName().contains("."))? type.getName().substring(type.getName().lastIndexOf(".") + 1) : type.getName();
        final String className = toName(typeName, "Field", "Test");
        final String fieldName = toName(typeName, "Field");
        final String methodName = toName("get", typeName, "Field");
        return ClassDeclaration.newClassDeclaration(metaClassLoader, ClassMetaType.newType(metaClassLoader, className)).
                add(new Declarations() {{
                    field(fieldName, type);
                    constructor().
                            add(new Instructions() {{
                                invokeSuper();
                            }});
                    method(methodName).
                            setReturnType(type).
                            add(new Instructions() {{
                                returnValue(getField(fieldName));
                            }});
                }});
    }

    private static void assertCompile(final Class<?> cls, final Type type, final Object expectedValue) throws Throwable {
        final String typeName = (type.getName().contains("."))? type.getName().substring(type.getName().lastIndexOf(".") + 1) : type.getName();
        final String methodName = toName("get", typeName, "Field");
        final Object instance = cls.newInstance();

        Assert.assertThat(cls.getDeclaredMethod(methodName).invoke(instance),
                          Expectations.equal(expectedValue));
    }

    private static void assertExtract(final ClassDeclaration expectedClassDeclaration, final Declaration<?> actualDeclaration) {
        Assert.assertThat(actualDeclaration,
                          new ClassDeclarationMatcher(expectedClassDeclaration));
    }
}
