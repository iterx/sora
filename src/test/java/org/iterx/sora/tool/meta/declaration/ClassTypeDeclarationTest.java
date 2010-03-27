package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.TypeDeclaration;
import org.iterx.sora.tool.meta.type.ClassType;
import org.iterx.sora.tool.meta.type.InterfaceType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ClassTypeDeclarationTest  extends TypeDeclarationTestCase {

    private static final ClassType SUPER_CLASS_TYPE = ClassType.newType(SuperClass.class.getName());
    private static final InterfaceType INTERFACE_TYPE = InterfaceType.newType(Interface.class.getName());

    public ClassTypeDeclarationTest(final TypeDeclaration<?, ?> typeDeclaration) {
        super(typeDeclaration);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[] {
                        ClassTypeDeclaration.newClassDeclaration(newClassType())
                },
                new Object[] {
                        ClassTypeDeclaration.newClassDeclaration(newClassType()).
                                setSuperType(SUPER_CLASS_TYPE)
                },
                new Object[] {
                        ClassTypeDeclaration.newClassDeclaration(newClassType()).
                                setSuperType(ClassTypeDeclaration.newClassDeclaration(newClassType()).
                                    setAccess(ClassTypeDeclaration.Access.PROTECTED).
                                    getClassType())
                },
                new Object[] {
                        ClassTypeDeclaration.newClassDeclaration(newClassType()).
                                setInterfaceTypes(INTERFACE_TYPE)
                },
                new Object[] {
                        ClassTypeDeclaration.newClassDeclaration(newClassType()).
                                setSuperType(SUPER_CLASS_TYPE).
                                setInterfaceTypes(INTERFACE_TYPE)
                },
                new Object[] {
                        ClassTypeDeclaration.newClassDeclaration(newClassType()).
                                addInnerType(newClassType())
                }
        );
    }

    public static class SuperClass {}

    public static interface Interface {}
}
