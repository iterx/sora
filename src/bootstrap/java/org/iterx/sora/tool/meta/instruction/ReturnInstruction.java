package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.AbstractInstruction;
import org.iterx.sora.tool.meta.Value;

public final class ReturnInstruction extends AbstractInstruction<ReturnInstruction> {

    private final Value<?> value;

    private ReturnInstruction(final Value<?> value)  {
        this.value = value;
    }

    public static ReturnInstruction newReturnInstruction(final Value<?> value)  {
        return new ReturnInstruction(value);
    }

    public Value<?> getValue() {
        return value;
    }
}
