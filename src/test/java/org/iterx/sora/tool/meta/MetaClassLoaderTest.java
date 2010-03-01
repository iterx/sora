package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.type.Type;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetaClassLoaderTest {

    private MetaClassLoader metaClassLoader;

    @Test
    public void shouldLoadClassDeclarationForClassMetaType() throws Throwable {
        final ClassDeclaration classDeclaration = metaClassLoader.loadDeclaration(Type.<Type.ClassMetaType>getType(TestClass.class));
        Assert.assertEquals(Type.getType(TestClass.class), classDeclaration.getType());
    }

    @Test
    public void shouldLoadInterfaceDeclarationForInterfaceMetaType() throws Throwable {
        final InterfaceDeclaration interfaceDeclaration = metaClassLoader.loadDeclaration(Type.<Type.InterfaceMetaType>getType(TestInterface.class));
        Assert.assertEquals(Type.getType(TestInterface.class), interfaceDeclaration.getType());
    }

    @Test
    public void shouldCreateNewDeclarationForClassMetaType() throws Throwable {
        final Type<Type.ClassMetaType> type = Type.getType("test", Type.CLASS_META_TYPE);
        final ClassDeclaration classDeclaration = metaClassLoader.newDeclaration(type);
        Assert.assertNotNull(classDeclaration);
    }

    @Test
    public void shouldCreateNewDeclarationForInterfaceMetaType() throws Throwable {
        final Type<Type.InterfaceMetaType> type = Type.getType("test", Type.INTERFACE_META_TYPE);

        final InterfaceDeclaration interfaceDeclaration = metaClassLoader.newDeclaration(type);
        Assert.assertNotNull(interfaceDeclaration);
    }


    @Test
    public void shouldLoadClassForClassType() throws Throwable {
        final Class cls = metaClassLoader.loadClass(Type.getType(TestClass.class));
        Assert.assertEquals(TestClass.class, cls);
    }

    @Test
    public void shouldLoadClassForInterfaceType() throws Throwable {
        final Class cls = metaClassLoader.loadClass(Type.getType(TestInterface.class));
        Assert.assertEquals(TestInterface.class, cls);
    }

    @Test
    public void shouldLoadClassForNewClassDeclaration() throws Throwable {
        final Type<Type.ClassMetaType> type = Type.getType("test", Type.CLASS_META_TYPE);
        final ClassDeclaration classDeclaration = metaClassLoader.newDeclaration(type);
        final Class cls = metaClassLoader.loadClass(type);
        Assert.assertEquals(type, Type.getType(cls));
    }

    @Before
    public void setUp() {
        metaClassLoader = new MetaClassLoader();
    }

    private static class TestClass {}

    private static interface TestInterface {}

    /*
    public static void main(String[] args) throws Throwable {
        //dump(Type.class);
        debug(new ClassReader(TestClass.class.getName()));
        dump(TestClass.class);
    }

    public static abstract class TestClass {

        public abstract void method();

    }

    private static void dump(final Class cls) throws Throwable {
        final Declaration declaration = new MetaClassLoader().loadClass(Type.getType(cls));
        final byte[] bytes = AsmCompiler.compile(declaration);
        debug(new ClassReader(bytes));
        load(bytes);
    }


    private static void load(final byte[] bytes) {
        new DebugClassLoader().defineClass(bytes);
    }

    private static void debug(final ClassReader classReader) {
        classReader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

    private static class DebugClassLoader extends ClassLoader {

        public Class<?> defineClass(final byte[] bytes) {
            return super.defineClass(null, bytes, 0, bytes.length);
        }
    }
*/


}
