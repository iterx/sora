package org.iterx.sora.tool.meta.test.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.iterx.sora.tool.meta.Instruction;
import org.iterx.sora.tool.meta.instruction.GetFieldInstruction;
import org.iterx.sora.tool.meta.instruction.InvokeSuperInstruction;
import org.iterx.sora.tool.meta.instruction.PutFieldInstruction;
import org.iterx.sora.tool.meta.instruction.ReturnInstruction;
import org.iterx.sora.tool.meta.instruction.StoreInstruction;
import org.iterx.sora.tool.meta.util.InstructionReader;
import org.iterx.sora.tool.meta.util.InstructionVisitor;

import java.util.Arrays;

public class InstructionsMatcher extends BaseMatcher {

    private final MatchesInstructionVisitor matchesInstructionVisitor;

    public InstructionsMatcher(final Instruction[] instructions) {
        this.matchesInstructionVisitor = new MatchesInstructionVisitor(instructions);
    }

    public void describeTo(final Description description) {
        description.appendText("<instructions>");
    }

    public boolean matches(final Object object) {
        new InstructionReader((Instruction[]) object).accept(matchesInstructionVisitor);
        return matchesInstructionVisitor.matches();
    }

    private static class MatchesInstructionVisitor implements InstructionVisitor {

        private static final Instruction NULL_INSTRUCTION = new Instruction(){}; 

        private final Instruction[] expectedInstructions;
        private int index;
        private boolean matches;

        private MatchesInstructionVisitor(final Instruction[] expectedInstructions) {
            this.expectedInstructions = expectedInstructions;
            this.matches = true;
        }

        public boolean matches() {
            return matches && (index == expectedInstructions.length);
        }

        public void getField(final GetFieldInstruction getFieldInstruction) {
            throw new UnsupportedOperationException();
        }

        public void putField(final PutFieldInstruction putFieldInstruction) {
            throw new UnsupportedOperationException();
        }

        public void store(final StoreInstruction storeInstruction) {
            throw new UnsupportedOperationException();
        }

        public void invokeSuper(final InvokeSuperInstruction invokeSuperInstruction) {
            final Instruction instruction = next();
            set(invokeSuperInstruction.equals(instruction) &&
                Arrays.equals(invokeSuperInstruction.getValues(), ((InvokeSuperInstruction) instruction).getValues()));
        }

        public void returnValue(final ReturnInstruction returnInstruction) {
            final Instruction instruction = next();
            //TODO: check instructions
            set(returnInstruction.equals(instruction) &&
                ((returnInstruction.getValue() != null)?
                 returnInstruction.getValue().equals(((ReturnInstruction) instruction).getValue()) :
                 returnInstruction.getValue() == ((ReturnInstruction) instruction).getValue()));
        }

        private Instruction next() {
            return (index < expectedInstructions.length)? expectedInstructions[index++] : NULL_INSTRUCTION;
        }

        private void set(final boolean value) {
            if(!value) matches = false;
        }

    }
}
