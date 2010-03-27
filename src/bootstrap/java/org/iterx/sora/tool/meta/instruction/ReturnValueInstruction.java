package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractInstruction;
import org.iterx.sora.tool.meta.Value;

public final class ReturnValueInstruction extends AbstractInstruction<ReturnValueInstruction> {

    private final Value<?> value;

    private ReturnValueInstruction(final Value<?> value)  {
        this.value = value;
    }

    public static ReturnValueInstruction newReturnInstruction(final Value<?> value)  {
        return new ReturnValueInstruction(value);
    }

    public Value<?> getValue() {
        return value;
    }
}
