package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractInstruction;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.value.Variable;

public final class PutFieldInstruction extends AbstractInstruction<PutFieldInstruction> {

    private final String fieldName;
    private final Type<?> fieldType;
    private final Value<?> value;
    private Variable owner;

    private PutFieldInstruction(final Type<?> fieldType, final String fieldName, final Value<?> value)  {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.value = value;
        this.owner = Variable.THIS;
    }

    public static PutFieldInstruction newPutFieldInstruction(final Type<?> fieldType, final String fieldName, final Value value)  {
        return new PutFieldInstruction(fieldType, fieldName, value);
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

    public Value<?> getValue() {
        return value;
    }
}
