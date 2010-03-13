package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.InterfaceMetaType;
import org.iterx.sora.tool.meta.Type;
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
        final ClassMetaType classType = metaClassLoader.loadType(TestClass.class);
        final ClassDeclaration classDeclaration = metaClassLoader.loadDeclaration(classType);
        Assert.assertEquals(classType, classDeclaration.getClassType());
    }

    @Test
    public void shouldLoadInterfaceDeclarationForInterfaceMetaType() throws Throwable {
        final InterfaceMetaType interfaceMetaType = metaClassLoader.loadType(TestInterface.class);
        final InterfaceDeclaration interfaceDeclaration = metaClassLoader.loadDeclaration(interfaceMetaType);
        Assert.assertEquals(interfaceMetaType, interfaceDeclaration.getInterfaceType());
    }

    @Test
    public void shouldCreateNewDeclarationForClassMetaType() throws Throwable {
        final ClassMetaType classType = ClassMetaType.newType(metaClassLoader, "test");
        final ClassDeclaration classDeclaration = ClassDeclaration.newClassDeclaration(metaClassLoader, classType);
        Assert.assertNotNull(classDeclaration);
    }

    @Test
    public void shouldCreateNewDeclarationForInterfaceMetaType() throws Throwable {
        final InterfaceMetaType interfaceType = InterfaceMetaType.newType(metaClassLoader, "test");
        final InterfaceDeclaration interfaceDeclaration = InterfaceDeclaration.newInterfaceDeclaration(metaClassLoader, interfaceType);
        Assert.assertNotNull(interfaceDeclaration);
    }

    @Test
    public void shouldLoadClassForClassType() throws Throwable {
        final Class cls = metaClassLoader.loadClass(metaClassLoader.loadType(TestClass.class));
        Assert.assertEquals(TestClass.class, cls);
    }

    @Test
    public void shouldLoadClassForInterfaceType() throws Throwable {
        final Class cls = metaClassLoader.loadClass(metaClassLoader.loadType(TestInterface.class));
        Assert.assertEquals(TestInterface.class, cls);
    }

    @Test
    public void shouldLoadClassForNewClassDeclaration() throws Throwable {
        final ClassMetaType classType = ClassMetaType.newType(metaClassLoader, "test");
        final ClassDeclaration classDeclaration = ClassDeclaration.newClassDeclaration(metaClassLoader, classType);
        final Class cls = metaClassLoader.loadClass(classType);
        Assert.assertEquals(classType, metaClassLoader.loadType(cls));
    }

    @Test
    public void shouldLoadDeclarationForClass () throws Throwable {
        final ClassDeclaration stringClassDeclaration = metaClassLoader.loadDeclaration(metaClassLoader.<ClassMetaType>loadType(String.class));
    }

    @Test
    public void shouldStoreInterface() {
        throw new UnsupportedOperationException();
        //final InterfaceDeclaration interfaceDeclaration = InterfaceDeclaration.newInterfaceDeclaration(metaClassLoader, InterfaceMetaType.newType("test"));
        //debug(metaClassLoader.storeDeclaration(interfaceDeclaration));
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
