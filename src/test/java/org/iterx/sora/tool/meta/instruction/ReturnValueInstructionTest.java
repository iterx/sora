package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instructions;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ReturnValueInstructionTest extends InstructionTestCase {

    public ReturnValueInstructionTest(final Type<?> type, final Object result) {
        super(type, result);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[]{ Type.BYTE_TYPE, (byte) 1 },
                             new Object[]{ Type.BYTE_TYPE, (byte) 11 },
                             new Object[]{ Type.SHORT_TYPE, (short) 1 },
                             new Object[]{ Type.SHORT_TYPE, (short) 11 },
                             new Object[]{ Type.CHAR_TYPE, '\0' },
                             new Object[]{ Type.CHAR_TYPE, 'c' },
                             new Object[]{ Type.BOOLEAN_TYPE, true },
                             new Object[]{ Type.INT_TYPE, -1 },
                             new Object[]{ Type.INT_TYPE, 1 },
                             new Object[]{ Type.INT_TYPE, 111 },
                             new Object[]{ Type.INT_TYPE, 1111 },
                             new Object[]{ Type.INT_TYPE, 11111111 },
                             new Object[]{ Type.LONG_TYPE, 1L },
                             new Object[]{ Type.LONG_TYPE, 11L },
                             new Object[]{ Type.FLOAT_TYPE, 1f },
                             new Object[]{ Type.FLOAT_TYPE, 11.1f },
                             new Object[]{ Type.DOUBLE_TYPE, 1d },
                             new Object[]{ Type.DOUBLE_TYPE, 11.1d },
                             new Object[]{ Type.STRING_TYPE, "string" },
                             new Object[]{ Type.OBJECT_TYPE, null });
    }



    public void setUpMethodDeclaration(final MethodDeclaration methodDeclaration) {
        methodDeclaration.add(new Instructions() {{
            if(getType() == Type.SHORT_TYPE) returnValue(constant((Short) getResult()));
            else if(getType() == Type.BYTE_TYPE) returnValue(constant((Byte) getResult()));
            else if(getType() == Type.CHAR_TYPE) returnValue(constant((Character) getResult()));
            else if(getType() == Type.BOOLEAN_TYPE) returnValue(constant((Boolean) getResult()));
            else if(getType() == Type.INT_TYPE) returnValue(constant((Integer) getResult()));
            else if(getType() == Type.LONG_TYPE) returnValue(constant((Long) getResult()));
            else if(getType() == Type.FLOAT_TYPE) returnValue(constant((Float) getResult()));
            else if(getType() == Type.DOUBLE_TYPE) returnValue(constant((Double) getResult()));
            else if(getType() == Type.STRING_TYPE) returnValue(constant((String) getResult()));
            else if(getType() == Type.OBJECT_TYPE) returnValue(NULL);
        }});
    }
}