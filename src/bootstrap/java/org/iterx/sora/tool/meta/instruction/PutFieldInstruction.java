package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.value.Variable;

public final class PutFieldInstruction extends Instruction<PutFieldInstruction> {

    private final String fieldName;
    private final Value value;

    private PutFieldInstruction(final String fieldName, final Value value)  {
        this.fieldName = fieldName;
        this.value = value;
    }

    public static PutFieldInstruction newPutFieldInstruction(final String fieldName, final Value value)  {
        return new PutFieldInstruction(fieldName, value);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Value getValue() {
        return value;
    }
}
