package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Value;

public final class ReturnValueInstruction extends Instruction<ReturnValueInstruction> {

    private final Value value;
    private final Instruction instruction;

    private ReturnValueInstruction(final Instruction instruction, final Value value)  {
        this.instruction = instruction;
        this.value = value;
    }

    public static ReturnValueInstruction newReturnInstruction(final Instruction<?> instruction) {
        return new ReturnValueInstruction(instruction, null);
    }

    public static ReturnValueInstruction newReturnInstruction(final Value value)  {
        return new ReturnValueInstruction(null, value);
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Value getValue() {
        return value;
    }
}
