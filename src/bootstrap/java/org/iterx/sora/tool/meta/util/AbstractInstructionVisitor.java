package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnInstruction;
import org.iterx.sora.tool.meta.instruction.StoreInstruction;

public abstract class AbstractInstructionVisitor implements InstructionVisitor {

    public void getField(final GetFieldInstruction getFieldInstruction) {}

    public void putField(final PutFieldInstruction putFieldInstruction) {}

    public void store(final StoreInstruction storeInstruction) {}

    public void invokeSuper(final InvokeSuperInstruction invokeSuperInstruction) {}

    public void returnValue(final ReturnInstruction returnInstruction) {}
}
