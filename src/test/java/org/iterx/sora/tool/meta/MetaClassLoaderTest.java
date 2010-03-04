package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.InterfaceMetaType;
import org.iterx.sora.tool.meta.type.Type;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class MetaClassLoaderTest {

    private MetaClassLoader metaClassLoader;

    @Test
    public void shouldLoadClassDeclarationForClassMetaType() throws Throwable {
        final ClassDeclaration classDeclaration = metaClassLoader.loadDeclaration(Type.<ClassMetaType>getType(TestClass.class));
        Assert.assertEquals(Type.<ClassMetaType>getType(TestClass.class), classDeclaration.getType());
    }

    @Test
    public void shouldLoadInterfaceDeclarationForInterfaceMetaType() throws Throwable {
        final InterfaceDeclaration interfaceDeclaration = metaClassLoader.loadDeclaration(Type.<InterfaceMetaType>getType(TestInterface.class));
        Assert.assertEquals(Type.<InterfaceMetaType>getType(TestInterface.class), interfaceDeclaration.getType());
    }

    @Test
    public void shouldCreateNewDeclarationForClassMetaType() throws Throwable {
        final ClassMetaType classType = ClassMetaType.newType("test"); //TODO: give option of metaclassloader???
        final ClassDeclaration classDeclaration = metaClassLoader.newDeclaration(classType);
        Assert.assertNotNull(classDeclaration);
    }

    @Test
    public void shouldCreateNewDeclarationForInterfaceMetaType() throws Throwable {
        final InterfaceMetaType interfaceType = InterfaceMetaType.newType("test");
        final InterfaceDeclaration interfaceDeclaration = metaClassLoader.newDeclaration(interfaceType);
        Assert.assertNotNull(interfaceDeclaration);
    }


    @Test
    public void shouldLoadClassForClassType() throws Throwable {
        final Class cls = metaClassLoader.loadClass(Type.<ClassMetaType>getType(TestClass.class));
        Assert.assertEquals(TestClass.class, cls);
    }

    @Test
    public void shouldLoadClassForInterfaceType() throws Throwable {
        final Class cls = metaClassLoader.loadClass(Type.<InterfaceMetaType>getType(TestInterface.class));
        Assert.assertEquals(TestInterface.class, cls);
    }

    @Test
    public void shouldLoadClassForNewClassDeclaration() throws Throwable {
        final ClassMetaType classType = ClassMetaType.newType("test");
        final ClassDeclaration classDeclaration = metaClassLoader.newDeclaration(classType);
        final Class cls = metaClassLoader.loadClass(classType);
        Assert.assertEquals(classType, Type.<ClassMetaType>getType(cls));
    }

    @Test
    public void should() throws Throwable {
        final ClassDeclaration stringClassDeclaration = metaClassLoader.loadDeclaration(Type.<ClassMetaType>getType(String.class));
    }

    @Test
    public void shouldStoreInterface() {
        final InterfaceDeclaration interfaceDeclaration = metaClassLoader.newDeclaration(InterfaceMetaType.newType("test"));
        debug(metaClassLoader.storeDeclaration(interfaceDeclaration));
    }


    @Before
    public void setUp() {
        metaClassLoader = new MetaClassLoader();
    }

    private static class TestClass {}

    private static interface TestInterface {}

    private static void debug(final byte[] bytes) {
        new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

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
        final Declaration declaration = new MetaClassLoader().loadClass(Type.newType(cls));
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
