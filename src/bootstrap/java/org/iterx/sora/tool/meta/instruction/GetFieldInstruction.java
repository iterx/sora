package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractInstruction;
import org.iterx.sora.tool.meta.value.Variable;

public final class GetFieldInstruction extends AbstractInstruction<GetFieldInstruction> {

    private final String fieldName;
    private Variable fieldOwner;

    private GetFieldInstruction(final String fieldName)  {
        this.fieldName = fieldName;
        this.fieldOwner = Variable.THIS;
    }

    public static GetFieldInstruction newGetFieldInstruction(final String fieldName)  {
        return new GetFieldInstruction(fieldName);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Variable getFieldOwner() {
        return fieldOwner;
    }

    public void setFieldOwner(final Variable fieldOwner) {
        this.fieldOwner = fieldOwner;
    }
}
