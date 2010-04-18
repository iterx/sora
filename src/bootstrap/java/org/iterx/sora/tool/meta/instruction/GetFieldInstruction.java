package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractValueInstruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.value.Variable;

public final class GetFieldInstruction extends AbstractValueInstruction<GetFieldInstruction> {

    private final Type<?> fieldType;
    private final String fieldName;
    private Variable owner;

    private GetFieldInstruction(final Type<?> fieldType, final String fieldName)  {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.owner = Variable.THIS;
    }

    public static GetFieldInstruction newGetFieldInstruction(final Type<?> fieldType, final String fieldName)  {
        return new GetFieldInstruction(fieldType, fieldName);
    }

    public Type<?> getType() {
        return fieldType;
    }

    public Variable getOwner() {
        return owner;
    }

    public void setOwner(final Variable owner) {
        this.owner = owner;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Type<?> getFieldType() {
        return fieldType;
    }
}
