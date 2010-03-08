package org.iterx.sora.tool.meta.instruction;

import org.iterx.sora.tool.meta.test.StubMetaClassLoader;
import org.junit.Before;

public class InstructionTestCase {

    protected StubMetaClassLoader extractMetaClassLoader;
    protected StubMetaClassLoader compileMetaClassLoader;


    @Before
    public void setUp() {
        compileMetaClassLoader = new StubMetaClassLoader(true);
        extractMetaClassLoader = new StubMetaClassLoader(true);
    }

    protected static String toName(final String... names) {
        final StringBuilder stringBuilder = new StringBuilder(names[0].toLowerCase());
        for(int i = 1, size = names.length; i < size; i++) stringBuilder.append(names[i].toUpperCase().substring(0, 1)).append(names[i].substring(1));
        return stringBuilder.toString();
    }
}
