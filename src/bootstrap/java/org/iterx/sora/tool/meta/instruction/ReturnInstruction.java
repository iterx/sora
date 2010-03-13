package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.Value;
import org.iterx.sora.tool.meta.value.Constant;

public final class ReturnInstruction extends Instruction<ReturnInstruction> {

    private final Value value;
    private final Instruction instruction;

    private ReturnInstruction(final Instruction instruction, final Value value)  {
        this.instruction = instruction;
        this.value = value;
    }

    public static ReturnInstruction newReturnInstruction() {
        return new ReturnInstruction(null, Constant.VOID);
    }

    public static ReturnInstruction newReturnInstruction(final Instruction<?> instruction) {
        return new ReturnInstruction(instruction, null);
    }

    public static ReturnInstruction newReturnInstruction(final Value value)  {
        return new ReturnInstruction(null, value);
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Value getValue() {
        return value;
    }
}
