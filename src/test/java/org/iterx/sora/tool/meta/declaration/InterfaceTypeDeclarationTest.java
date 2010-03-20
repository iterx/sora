package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.TypeDeclaration;
import org.iterx.sora.tool.meta.type.InterfaceType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class InterfaceTypeDeclarationTest extends TypeDeclarationTestCase {

    private static final InterfaceType INTERFACE_TYPE = InterfaceType.newType(Interface.class.getName());

    public InterfaceTypeDeclarationTest(final TypeDeclaration<?, ?> typeDeclaration) {
        super(typeDeclaration);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[] {
                        InterfaceTypeDeclaration.newInterfaceDeclaration(newInterfaceType())
                },
                new Object[] {
                        InterfaceTypeDeclaration.newInterfaceDeclaration(newInterfaceType()).
                                setInterfaceTypes(INTERFACE_TYPE)
                });
    }

    public static interface Interface {}
}
