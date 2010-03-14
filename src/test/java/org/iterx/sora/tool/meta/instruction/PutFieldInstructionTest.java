package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.value.Variable;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class PutFieldInstructionTest extends InstructionTestCase {

    private static final String GET_FIELD_NAME = "getField";
    private static final String PUT_FIELD_NAME = "putField";

    public PutFieldInstructionTest(final Type<?> type, final Object result) {
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
            final Variable variable = variable("<variable>", getType());
            store(variable, getField(GET_FIELD_NAME));
            putField(PUT_FIELD_NAME, variable);
            returnInstruction(getField(PUT_FIELD_NAME));
        }});
    }

    @Override
    public void setUpClassDeclaration(final ClassTypeDeclaration classTypeDeclaration) {
        classTypeDeclaration.add(new Declarations(){{
            field(GET_FIELD_NAME, getType());
            field(PUT_FIELD_NAME, getType());
        }});
    }
}