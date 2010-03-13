package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
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
        return Arrays.asList(new Object[]{ Type.SHORT_TYPE, (short) 0 },
                             new Object[]{ Type.BYTE_TYPE, (byte) 0 },
                             new Object[]{ Type.CHAR_TYPE, '\0' },
                             new Object[]{ Type.BOOLEAN_TYPE, false },
                             new Object[]{ Type.INT_TYPE, 0 },
                             new Object[]{ Type.LONG_TYPE, 0L },
                             new Object[]{ Type.FLOAT_TYPE, 0f },
                             new Object[]{ Type.DOUBLE_TYPE, 0d },
                             new Object[]{ Type.OBJECT_TYPE, null });
    }

    public void setUpMethodDeclaration(final MethodDeclaration methodDeclaration) {
        methodDeclaration.add(new Instructions() {{
            returnInstruction(getField(FIELD_NAME));
        }});
    }

    @Override
    public void setUpClassDeclaration(final ClassDeclaration classDeclaration) {
        classDeclaration.add(new Declarations(){{
            field(FIELD_NAME, getType());
        }});
    }
}