/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy;

import org.apache.ivy.util.CacheCleaner;
import org.apache.ivy.util.cli.CommandLine;
import org.apache.ivy.util.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MainTest {

    private File cache;

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Before
    public void setUp() {
        cache = new File("build/cache");
        System.setProperty("ivy.cache.dir", cache.getAbsolutePath());
    }

    @After
    public void tearDown() {
        CacheCleaner.deleteDir(cache);
    }

    @Test
    public void testHelp() throws Exception {
        run(new String[] {"-?"});
    }

    @Test
    public void testBadOption() throws Exception {
        expExc.expect(ParseException.class);
        expExc.expectMessage("Unrecognized option: -bad");

        run(new String[] {"-bad"});
    }

    @Test
    public void testMissingParameter() throws Exception {
        expExc.expect(ParseException.class);
        expExc.expectMessage("no argument for: ivy");

        run(new String[] {"-ivy"});
    }

    @Test
    public void testResolveSimple() throws Exception {
        run(new String[] {"-settings", "test/repositories/ivysettings.xml", "-ivy",
                "test/repositories/1/org1/mod1.1/ivys/ivy-1.0.xml"});
        assertTrue(new File("build/cache/org1/mod1.2/ivy-2.0.xml").exists());
    }

    @Test
    public void testResolveSimpleWithConfs() throws Exception {
        run(new String[] {"-settings", "test/repositories/ivysettings.xml", "-ivy",
                "test/repositories/1/org1/mod1.1/ivys/ivy-1.0.xml", "-confs", "default"});
        assertTrue(new File("build/cache/org1/mod1.2/ivy-2.0.xml").exists());
    }

    @Test
    public void testResolveSimpleWithConfs2() throws Exception {
        run(new String[] {"-settings", "test/repositories/ivysettings.xml", "-confs", "default",
                "-ivy", "test/repositories/1/org1/mod1.1/ivys/ivy-1.0.xml"});
        assertTrue(new File("build/cache/org1/mod1.2/ivy-2.0.xml").exists());
    }

    @Test
    public void testExtraParams1() throws Exception {
        String[] params = new String[] {"-settings", "test/repositories/ivysettings.xml", "-confs",
                "default", "-ivy", "test/repositories/1/org1/mod1.1/ivys/ivy-1.0.xml", "foo1",
                "foo2"};
        CommandLine line = Main.getParser().parse(params);
        String[] leftOver = line.getLeftOverArgs();
        assertNotNull(leftOver);
        assertEquals(2, leftOver.length);
        assertEquals("foo1", leftOver[0]);
        assertEquals("foo2", leftOver[1]);
    }

    @Test
    public void testExtraParams2() throws Exception {
        String[] params = new String[] {"-settings", "test/repositories/ivysettings.xml", "-confs",
                "default", "-ivy", "test/repositories/1/org1/mod1.1/ivys/ivy-1.0.xml", "--",
                "foo1", "foo2"};
        CommandLine line = Main.getParser().parse(params);
        String[] leftOver = line.getLeftOverArgs();
        assertNotNull(leftOver);
        assertEquals(2, leftOver.length);
        assertEquals("foo1", leftOver[0]);
        assertEquals("foo2", leftOver[1]);
    }

    @Test
    public void testExtraParams3() throws Exception {
        String[] params = new String[] {"-settings", "test/repositories/ivysettings.xml", "-confs",
                "default", "-ivy", "test/repositories/1/org1/mod1.1/ivys/ivy-1.0.xml"};
        CommandLine line = Main.getParser().parse(params);
        String[] leftOver = line.getLeftOverArgs();
        assertNotNull(leftOver);
        assertEquals(0, leftOver.length);
    }

    /**
     * Tests that the {@code types} argument to the command line is parsed correctly when it's passed
     * more than one value for the argument
     *
     * @throws Exception
     * @see <a href="https://issues.apache.org/jira/browse/IVY-1355">IVY-1355</a>
     */
    @Test
    public void testTypes() throws Exception {
        final String[] params = new String[]{"-settings", "test/repositories/ivysettings.xml", "-retrieve",
                "build/test/main/retrieve/[module]/[conf]/[artifact]-[revision].[ext]",
                "-types", "jar", "source"};
        final CommandLine parsedCommand = Main.getParser().parse(params);
        final String[] parsedTypes = parsedCommand.getOptionValues("types");
        assertNotNull("Values for types argument is missing", parsedTypes);
        assertEquals("Unexpected number of values parsed for types argument", 2, parsedTypes.length);
        final Set<String> uniqueParsedTypes = new HashSet<>();
        uniqueParsedTypes.addAll(Arrays.asList(parsedTypes));
        assertTrue("jar type is missing from the parsed types argument", uniqueParsedTypes.contains("jar"));
        assertTrue("jar type is missing from the parsed types argument", uniqueParsedTypes.contains("source"));
    }

    private void run(String[] args) throws Exception {
        Main.run(Main.getParser(), args);
    }
}
