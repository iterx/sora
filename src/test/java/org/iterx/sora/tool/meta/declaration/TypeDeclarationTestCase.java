package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.ClassReader;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.TypeDeclaration;
import org.iterx.sora.tool.meta.test.StubMetaClassLoader;
import org.iterx.sora.tool.meta.test.matcher.TypeMatcher;
import org.iterx.sora.tool.meta.type.ClassType;
import org.iterx.sora.tool.meta.type.InterfaceType;
import org.iterx.sora.tool.meta.util.trace.TracerTypeVisitor;
import org.iterx.sora.tool.meta.util.TypeReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TypeDeclarationTestCase {

    private static AtomicInteger CLASS_SEQUENCE = new AtomicInteger();
    private static AtomicInteger INTERFACE_SEQUENCE = new AtomicInteger();

    private StubMetaClassLoader extractMetaClassLoader;
    private StubMetaClassLoader compileMetaClassLoader;

    private final TypeDeclaration<?, ?> typeDeclaration;

    public TypeDeclarationTestCase(final TypeDeclaration<?, ?> typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }

    @Test
    public void shouldCompileDeclaration() throws Throwable {
        final Class<?> cls = compileMetaClassLoader.loadClass(typeDeclaration);
        assertCompile(typeDeclaration, cls);
    }

    @Test
    public void shouldExtractDeclaration() throws Throwable {
        extractMetaClassLoader.defineClass(typeDeclaration);
        assertExtract(typeDeclaration, extractMetaClassLoader.loadDeclaration(typeDeclaration));
    }

    @Before
    public void setUp() {
        compileMetaClassLoader = new StubMetaClassLoader(MetaClassLoader.getSystemMetaClassLoader(), false);
        extractMetaClassLoader = new StubMetaClassLoader(null, false);
    }

    public static ClassType newClassType() {
        return ClassType.newType("class" + CLASS_SEQUENCE.incrementAndGet());
    }

    public static InterfaceType newInterfaceType() {
        return InterfaceType.newType("interface" + INTERFACE_SEQUENCE.incrementAndGet());
    }

    private static void assertCompile(final TypeDeclaration expectedTypeDeclaration, final Class actualClass) throws Throwable {
        if(!TypeMatcher.newTypeMatcher(expectedTypeDeclaration).matches(actualClass)) {
            Assert.assertEquals("Compile failure",
                                toString(expectedTypeDeclaration),
                                toString(actualClass));
            throw new IllegalStateException();
        }
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
