package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractInstruction;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.value.Variable;

public final class StoreInstruction extends AbstractInstruction<StoreInstruction> {

    private final Variable variable;
    private final Value<?> value;

    private StoreInstruction(final Variable variable, final Value<?> value) {
        this.variable = variable;
        this.value = value;
    }

    public static StoreInstruction newStoreInstruction(final Variable variable, final Value<?> value) {
        return new StoreInstruction(variable, value);
    }

    public Variable getVariable() {
        return variable;
    }

    public Value<?> getValue() {
        return value;
    }
}
