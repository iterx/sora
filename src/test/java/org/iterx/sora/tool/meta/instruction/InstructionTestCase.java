package org.iterx.sora.tool.meta.instruction;

import org.hamcrest.Matchers;
import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.TypeDeclaration;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.test.StubMetaClassLoader;
import org.iterx.sora.tool.meta.test.matcher.TypeMatcher;
import org.iterx.sora.tool.meta.type.ClassType;
import org.iterx.sora.tool.meta.util.trace.TracerTypeVisitor;
import org.iterx.sora.tool.meta.util.TypeReader;
import org.iterx.sora.tool.meta.value.Constant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public abstract class InstructionTestCase {

    public static final String METHOD_NAME = "run";

    private StubMetaClassLoader extractMetaClassLoader;
    private StubMetaClassLoader compileMetaClassLoader;

    private final Type<?> type;
    private final Object result;

    public InstructionTestCase(final Type<?> type, final Object result) {
        this.type = type;
        this.result = result;
    }

    public Type<?> getType() {
        return type;
    }

    public Object getResult() {
        return result;
    }
    
    @Test
    public void shouldCompileInstruction() throws Throwable {
        final ClassTypeDeclaration classTypeDeclaration = newClassDeclaration(compileMetaClassLoader, type);
        final Class<?> cls = compileMetaClassLoader.loadClass(classTypeDeclaration.getClassType());
        assertCompile(classTypeDeclaration, result, cls, cls.getDeclaredMethod(METHOD_NAME).invoke(cls.newInstance()));
    }

    @Test
    public void shouldExtractInstruction() throws Throwable {
        final ClassTypeDeclaration expectedClassTypeDeclaration = newClassDeclaration(compileMetaClassLoader, type);
        extractMetaClassLoader.defineClass(expectedClassTypeDeclaration);

        assertExtract(expectedClassTypeDeclaration,
                      extractMetaClassLoader.loadDeclaration(expectedClassTypeDeclaration.getClassType()));
    }

    public abstract void setUpMethodDeclaration(final MethodDeclaration methodDeclaration);

    public void setUpClassDeclaration(final ClassTypeDeclaration classTypeDeclaration) {}

    @Before
    public void setUp() {
        compileMetaClassLoader = new StubMetaClassLoader(true);
        extractMetaClassLoader = new StubMetaClassLoader(true);
    }   

    public static String toName(final String... names) {
        final StringBuilder stringBuilder = new StringBuilder(names[0]);
        for(int i = 1, size = names.length; i < size; i++) stringBuilder.append(names[i].toUpperCase().substring(0, 1)).append(names[i].substring(1));
        return stringBuilder.toString();
    }

    public static String toName(final Type<?> type) {
        final String string = type.getName();
        final int index = string.lastIndexOf('.');
        return (index != -1)? string.substring(index + 1) : string;
    }

    public static Constant toConstant(final Type<?> type, final Object value) {
        if(type == Type.SHORT_TYPE) return Constant.newConstant((Short) value);
        else if(type == Type.BYTE_TYPE) return Constant.newConstant((Byte) value);
        else if(type == Type.CHAR_TYPE) return Constant.newConstant((Character) value);
        else if(type == Type.BOOLEAN_TYPE) return Constant.newConstant((Boolean) value);
        else if(type == Type.INT_TYPE) return Constant.newConstant((Integer) value);
        else if(type == Type.LONG_TYPE) return Constant.newConstant((Long) value);
        else if(type == Type.FLOAT_TYPE) return Constant.newConstant((Float) value);
        else if(type == Type.DOUBLE_TYPE) return Constant.newConstant((Double) value);
        else if(type == Type.STRING_TYPE) return Constant.newConstant((String) value);
        return Constant.NULL;
    }
    
    
    private ClassTypeDeclaration newClassDeclaration(final MetaClassLoader metaClassLoader, final Type type) {
        final String className = toName(getClass().getSimpleName(), "$", toName(type));
        final MethodDeclaration methodDeclaration = MethodDeclaration.newMethodDeclaration(METHOD_NAME).setReturnType(type);
        final ClassTypeDeclaration classTypeDeclaration = ClassTypeDeclaration.newClassDeclaration(metaClassLoader, ClassType.newType(metaClassLoader, className)).
                add(new Declarations() {{
                    constructor().
                            add(new Instructions() {{
                                returnValue(invokeSuper(Type.OBJECT_TYPE));
                            }});
                    method(methodDeclaration);
                }});
        setUpClassDeclaration(classTypeDeclaration);
        setUpMethodDeclaration(methodDeclaration);
        return classTypeDeclaration;
    }


    private static void assertCompile(final TypeDeclaration expectedTypeDeclaration,
                                      final Object expectedValue,
                                      final Class actualClass,
                                      final Object actualValue) throws Throwable {
        Assert.assertEquals("Compile failure", expectedValue, actualValue);
    }

    private static void assertExtract(final TypeDeclaration expectedTypeDeclaration, final TypeDeclaration actualDeclaration) {
        if(!TypeMatcher.newTypeMatcher(expectedTypeDeclaration).matches(actualDeclaration)) {
            Assert.assertEquals("Extract failure",
                                toString(expectedTypeDeclaration),
                                toString(actualDeclaration));
            throw new IllegalStateException();
        }
    }

    private static String toString(final Class cls) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new TypeReader(cls).accept(new TracerTypeVisitor(byteArrayOutputStream));
        return new String(byteArrayOutputStream.toByteArray());
    }

    private static String toString(final TypeDeclaration typeDeclaration) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new TypeReader(typeDeclaration).accept(new TracerTypeVisitor(byteArrayOutputStream));
        return new String(byteArrayOutputStream.toByteArray());
    }
}
