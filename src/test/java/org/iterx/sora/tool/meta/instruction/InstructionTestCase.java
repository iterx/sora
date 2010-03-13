package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.test.StubMetaClassLoader;
import org.iterx.sora.tool.meta.test.matcher.ClassDeclarationMatcher;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.util.DeclarationReader;
import org.iterx.sora.tool.meta.util.trace.TraceDeclarationVisitor;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public abstract class InstructionTestCase {

    private static final String METHOD_NAME = "run";

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
        final ClassDeclaration classDeclaration = newClassDeclaration(compileMetaClassLoader, type);
        final Class<?> cls = compileMetaClassLoader.loadClass(classDeclaration.getClassType());

        assertCompile(compileMetaClassLoader, cls, type, result);
    }

    @Test
    public void shouldExtractInstruction() throws Throwable {
        final ClassDeclaration expectedClassDeclaration = newClassDeclaration(compileMetaClassLoader, type);
        extractMetaClassLoader.defineClass(expectedClassDeclaration);

        assertExtract(expectedClassDeclaration, extractMetaClassLoader.loadDeclaration(expectedClassDeclaration.getClassType()));
    }

    public abstract void setUpMethodDeclaration(final MethodDeclaration methodDeclaration);

    public void setUpClassDeclaration(final ClassDeclaration classDeclaration) {}


    @Before
    public void setUp() {
        compileMetaClassLoader = new StubMetaClassLoader(false);
        extractMetaClassLoader = new StubMetaClassLoader(false);
    }

    private ClassDeclaration newClassDeclaration(final MetaClassLoader metaClassLoader, final Type type) {
        final String className = toName(getClass().getSimpleName(), "$", toName(type));
        final MethodDeclaration methodDeclaration = MethodDeclaration.newMethodDeclaration(METHOD_NAME).setReturnType(type);
        final ClassDeclaration classDeclaration = ClassDeclaration.newClassDeclaration(metaClassLoader, ClassMetaType.newType(metaClassLoader, className)).
                add(new Declarations() {{
                    constructor().
                            add(new Instructions() {{
                                invokeSuper();
                                returnVoid();
                            }});
                    method(methodDeclaration);
                }});
        setUpClassDeclaration(classDeclaration);
        setUpMethodDeclaration(methodDeclaration);
        return classDeclaration;
    }

    private static void assertCompile(final ClassLoader classLoader, final Class<?> cls, final Type type, final Object expectedValue) throws Throwable {
        final Object instance = cls.newInstance();

        Assert.assertTrue("Actual:\n" + toString(classLoader.getResourceAsStream(toResource(cls))),
                          Expectations.equal(expectedValue).matches(cls.getDeclaredMethod(METHOD_NAME).invoke(instance)));
    }

    private static void assertExtract(final ClassDeclaration expectedClassDeclaration, final Declaration<?> actualDeclaration) {

        Assert.assertTrue("Actual:\n" + toString(actualDeclaration),
                          new ClassDeclarationMatcher(expectedClassDeclaration).matches(actualDeclaration));
    }

    private static String toName(final String... names) {
        final StringBuilder stringBuilder = new StringBuilder(names[0]);
        for(int i = 1, size = names.length; i < size; i++) stringBuilder.append(names[i].toUpperCase().substring(0, 1)).append(names[i].substring(1));
        return stringBuilder.toString();
    }

    private static String toName(final Type<?> type) {
        final String string = type.getName();
        final int index = string.lastIndexOf('.');
        return (index != -1)? string.substring(index + 1) : string;
    }

    private static String toString(final Declaration<?> declaration) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new DeclarationReader(declaration).accept(new TraceDeclarationVisitor(byteArrayOutputStream));
        return new String(byteArrayOutputStream.toByteArray());
    }

    private static String toString(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new ClassReader(inputStream).accept(new TraceClassVisitor(new PrintWriter(byteArrayOutputStream)), 0);
        return new String(byteArrayOutputStream.toByteArray());
    }

    private static String toResource(final Class cls) {
        return cls.getName().replace('.', '/') + ".class";
    }
}
