package com.lambdatest.tunnel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LambdaTestTunnelTest {
    private Tunnel t;
    private Map<String, String> options;

    @Before
    public void setUp() throws Exception {
        t = new Tunnel();
        options = new HashMap<String, String>();
        options.put("user", System.getenv("LT_USERNAME"));
        options.put("key", System.getenv("LT_ACCESS_KEY"));
        String varValue = System.getenv("LT_ACCESS_KEY");
    }

    @Test
    public void testIsRunning() throws Exception {
        assertFalse(t.isRunning());
        t.start(options);
        assertTrue(t.isRunning());
    }

    @Test
    public void testMultipleBinary() throws Exception {
        t.start(options);
        assertTrue(t.isRunning());
        Tunnel l2 = new Tunnel();
        try {
            l2.start(options);
        } catch (TunnelException e) {
            assertFalse(l2.isRunning());
        }
    }

    @Test
    public void testEnableVerbose() throws Exception {
        options.put("v", "true");
        t.start(options);
        assertTrue(t.command.contains("-v"));
    }

    @Test
    public void testSetFolder() throws Exception {
        options.put("dir", "/var/html");
        t.start(options);
        assertTrue(t.command.contains("-dir"));

    }



    @Test
    public void testSetTunnelIdentifier() throws Exception {
        options.put("tunnelName", "xyz");
        options.put("onlyCommand", "true");
        t.start(options);
        assertTrue(t.command.contains("-tunnelName"));
        assertTrue(t.command.contains("xyz"));
    }

    @Test
    public void testSetProxy() throws Exception {
        options.put("proxyHost", "localhost");
        options.put("proxyPort", "8080");
        options.put("proxyUser", "user");
        options.put("proxyPass", "pass");
        t.start(options);
        assertTrue(t.command.contains("-proxy-host"));
        assertTrue(t.command.contains("localhost"));
        assertTrue(t.command.contains("-proxy-port"));
        assertTrue(t.command.contains("8080"));
        assertTrue(t.command.contains("-proxy-user"));
        assertTrue(t.command.contains("user"));
        assertTrue(t.command.contains("-proxy-pass"));
        assertTrue(t.command.contains("pass"));
    }

    @Test
    public void testSetHosts() throws Exception {
        options.put("hosts", "localhost,8000,0");
        t.start(options);
        assertTrue(t.command.contains("localhost,8000,0"));
    }

    @Test
    public void testCustomArguments() throws Exception {
        options.put("customKey", "customValue");
        options.put("customKey2", "customValue2");
        options.put("onlyCommand", "true");
        t.start(options);
        assertTrue(t.command.contains("-customKey"));
        assertTrue(t.command.contains("customValue"));
        assertTrue(t.command.contains("-customKey2"));
        assertTrue(t.command.contains("customValue2"));
    }


    @Test
    public void testCustomBoolArguments() throws Exception {
        options.put("customKey1", "true");
        options.put("customKey2", "true");
        options.put("onlyCommand", "true");
        t.start(options);
        assertTrue(t.command.contains("-customKey1"));
        assertTrue(t.command.contains("-customKey2"));
    }

    @After
    public void tearDown() throws Exception {
        t.stop();
    }
}
