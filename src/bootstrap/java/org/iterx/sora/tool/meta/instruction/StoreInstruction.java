package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractInstruction;
import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.value.Variable;

public final class StoreInstruction extends AbstractInstruction<StoreInstruction> {

    private final Instruction<?> instruction;
    private final Variable variable;

    private StoreInstruction(final Variable variable, final Instruction<?> instruction) {
        this.instruction = instruction;
        this.variable = variable;
    }

    public static StoreInstruction newStoreInstruction(final Variable variable, final Instruction<?> instruction) {
        return new StoreInstruction(variable, instruction);
    }

    public Variable getVariable() {
        return variable;
    }

    public Instruction<?> getInstruction() {
        return instruction;
    }
}
