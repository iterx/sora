package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.Value;

public interface InstructionVisitor {

    void invokeSuper(final Value[] values);

    void returnValue(final Value value);
}
