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
public class GetFieldInstructionTest extends InstructionTestCase {

    private static final String FIELD_NAME = "field";

    public GetFieldInstructionTest(final Type<?> type, final Object result) {
        super(type, result);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[]{ Type.SHORT_TYPE, (short) 1 },
                             new Object[]{ Type.BYTE_TYPE, (byte) 2 },
                             new Object[]{ Type.CHAR_TYPE, 'a' },
                             new Object[]{ Type.BOOLEAN_TYPE, true },
                             new Object[]{ Type.INT_TYPE, 100 },
                             new Object[]{ Type.LONG_TYPE, 1000L },
                             new Object[]{ Type.FLOAT_TYPE, 10.0f },
                             new Object[]{ Type.DOUBLE_TYPE, 100.0d },
                             new Object[]{ Type.STRING_TYPE, "string"},
                             new Object[]{ Type.OBJECT_TYPE, null });
    }

    public void setUpMethodDeclaration(final MethodDeclaration methodDeclaration) {
        methodDeclaration.
                add(new Instructions() {{
                    returnValue(getField(getType(), FIELD_NAME));
                }});

    }

    @Override
    public void setUpClassDeclaration(final ClassTypeDeclaration classTypeDeclaration) {
        classTypeDeclaration.
                add(new Declarations(){{
                    field(FIELD_NAME, getType()).setFieldValue(toConstant(getType(), getResult()));
                }});
    }
}
