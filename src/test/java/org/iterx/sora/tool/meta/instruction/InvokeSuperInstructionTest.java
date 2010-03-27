package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.iterx.sora.tool.meta.type.ClassType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InvokeSuperInstructionTest extends InstructionTestCase {

    private static final MetaClassLoader META_CLASS_LOADER = MetaClassLoader.getSystemMetaClassLoader();

    private final ClassType superClassType;

    public InvokeSuperInstructionTest(final Type<?> type, final Object result) throws ClassNotFoundException{
        super(type, result);
        this.superClassType = newSuperClass(type);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[]{ Type.BYTE_TYPE, (byte) 1 },
                             new Object[]{ Type.SHORT_TYPE, (short) 2 },
                             new Object[]{ Type.CHAR_TYPE, 'c' },
                             new Object[]{ Type.BOOLEAN_TYPE, true },
                             new Object[]{ Type.INT_TYPE, 10 },
                             new Object[]{ Type.LONG_TYPE, 100L },
                             new Object[]{ Type.FLOAT_TYPE, 10.0f },
                             new Object[]{ Type.DOUBLE_TYPE, 100.0d },
                             new Object[]{ Type.STRING_TYPE, "string" },
                             new Object[]{ Type.OBJECT_TYPE, null });
    }

    public void setUpMethodDeclaration(final MethodDeclaration methodDeclaration) {       
        methodDeclaration.
                add(new Instructions() {{
                    returnValue(invokeSuper(superClassType).setMethodName(METHOD_NAME).setReturnType(getType()));
                }});
    }

    @Override
    public void setUpClassDeclaration(final ClassTypeDeclaration classTypeDeclaration) {
        classTypeDeclaration.
                setSuperType(superClassType).
                remove(classTypeDeclaration.getConstructorDeclaration()).
                add(new Declarations(){{
                    constructor().add(new Instructions() {{
                        returnValue(invokeSuper(superClassType));
                    }});
                }});
    }


    private ClassType newSuperClass(final Type<?> type) throws ClassNotFoundException {
        final String superClassTypeName = "superClass$" + toName(type);
        try {
            return META_CLASS_LOADER.loadType(superClassTypeName);
        }
        catch(final ClassNotFoundException e) {
            final ClassType superClassType = ClassType.newType(superClassTypeName);
            ClassTypeDeclaration.newClassDeclaration(superClassType).add(
                    new Declarations() {{
                        constructor().
                                add(new Instructions() {{
                                    returnValue(invokeSuper(Type.OBJECT_TYPE));
                            }});
                        method(METHOD_NAME).
                                setReturnType(type).
                                add(new Instructions() {{
                                    returnValue(toConstant(type, getResult()));
                                }});
                    }});
            META_CLASS_LOADER.loadClass(superClassType);
            return superClassType;
        }
    }
}
