package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Value;

public final class StoreInstruction extends Instruction<StoreInstruction> {

    private final Instruction<?> instruction;
    private final Value value;

    private StoreInstruction(final Value value, final Instruction<?> instruction) {
        this.instruction = instruction;
        this.value = value;
    }

    public static StoreInstruction newStoreInstruction(final Value value, final Instruction<?> instruction) {
        return new StoreInstruction(value, instruction);
    }

    public Value getValue() {
        return value;
    }

    public Instruction<?> getInstruction() {
        return instruction;
    }
}
