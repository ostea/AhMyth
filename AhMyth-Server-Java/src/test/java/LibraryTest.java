/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class LibraryTest {
    @Test
    public void testSomeLibraryMethod() {
        //Library classUnderTest = new Library();
        //assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod());
    }

    @Test
    public void testSubArray() {
        String[] cmds = {"a", "b", "c", "d"};
        String[] subCmds = ArrayUtils.subarray(cmds, 2, cmds.length);
        System.out.println(subCmds);
    }
}