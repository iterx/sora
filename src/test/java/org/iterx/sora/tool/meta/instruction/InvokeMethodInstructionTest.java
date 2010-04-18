package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InvokeMethodInstructionTest extends InstructionTestCase {

    private static final String TARGET_METHOD_NAME = "target";

    private ClassTypeDeclaration classTypeDeclaration;

    public InvokeMethodInstructionTest(final Type<?> type, final Object result) throws ClassNotFoundException{
        super(type, result);
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
                setReturnType(getType()).
                add(new Instructions() {{
                    RETURN(invokeMethod(classTypeDeclaration, TARGET_METHOD_NAME).setReturnType(getType()));
                }});
    }

    @Override
    public void setUpClassDeclaration(final ClassTypeDeclaration classTypeDeclaration) {
        this.classTypeDeclaration = classTypeDeclaration.
                add(new Declarations(){{
                    method(TARGET_METHOD_NAME).setAccess(MethodDeclaration.Access.PRIVATE).
                            setReturnType(getType()).
                            add(new Instructions() {{
                                RETURN(toConstant(getType(), getResult()));
                            }});
                }});
    }
}