package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;

public final class GetFieldInstruction extends Instruction<GetFieldInstruction> {

    private final String fieldName;

    private GetFieldInstruction(final String fieldName)  {
        this.fieldName = fieldName;
    }

    public static GetFieldInstruction newGetFieldInstruction(final String fieldName)  {
        return new GetFieldInstruction(fieldName);
    }

    public String getFieldName() {
        return fieldName;
    }
}
