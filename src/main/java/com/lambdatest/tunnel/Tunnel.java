package com.lambdatest.tunnel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.*;

/**
 * Creates and manages a secure tunnel connection to LambdaTest.
 */
public class Tunnel {

    private static final List<String> IGNORE_KEYS = Arrays.asList("key", "binarypath");

    List<String> command;
    Map<String, String> startOptions;
    String binaryPath;
    int pid = 0;

    private TunnelProcess proc = null;

    private final Map<String, String> parameters;
    private final Map<String, String> voidValueParameters;

    public Tunnel() {
        voidValueParameters = new HashMap<String, String>();
        voidValueParameters.put("v", "-v");
        voidValueParameters.put("version", "-version");

        parameters = new HashMap<String, String>();
        parameters.put("config", "-config");
        parameters.put("controller", "-controller");
        parameters.put("cui", "-cui");
        parameters.put("customSSHHost", "-customSSHHost");
        parameters.put("customSSHPort", "-customSSHPort");
        parameters.put("customSSHPrivateKey", "-customSSHPrivateKey");
        parameters.put("customSSHUser", "-customSSHUser");
        parameters.put("dir", "-dir");
        parameters.put("dns", "-dns");
        parameters.put("emulateChrome", "-emulateChrome");
        parameters.put("env", "-env");
        parameters.put("infoAPIPort", "-infoAPIPort");
        parameters.put("key", "-key");
        parameters.put("localDomains", "-local-domains");
        parameters.put("logFile", "-logFile");
        parameters.put("mode", "-mode");
        parameters.put("nows", "-nows");
        parameters.put("outputConfig", "-outputConfig");
        parameters.put("pac", "-pac");
        parameters.put("pidfile", "-pidfile");
        parameters.put("port", "-port");
        parameters.put("proxyHost", "-proxy-host");
        parameters.put("proxyPass", "-proxy-pass");
        parameters.put("proxyPort", "-proxy-port");
        parameters.put("proxyUser", "-proxy-user");
        parameters.put("remoteDebug", "-remote-debug");
        parameters.put("server", "-server");
        parameters.put("sharedTunnel", "-shared-tunnel");
        parameters.put("tunnelName", "-tunnelName");
        parameters.put("user", "-user");
        parameters.put("v", "-v");
        parameters.put("version", "-version");

    }

    /**
     * Starts Tunnel instance with options
     *
     * @param options Options for the Tunnel instance
     * @throws Exception
     */
    public void start(Map<String, String> options) throws Exception {
        startOptions = options;
        if (options.get("binarypath") != null) {
            binaryPath = options.get("binarypath");
        } else {
            TunnelBinary lb = new TunnelBinary();
            binaryPath = lb.getBinaryPath();
        }

        makeCommand(options, "start");

        if (options.get("onlyCommand") != null) return;

        if (proc == null) {
            proc = runCommand(command);
            BufferedReader stdoutbr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderrbr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String stdout="", stderr="", line;
            while ((line = stdoutbr.readLine()) != null) {
                stdout += line;
            }
            while ((line = stderrbr.readLine()) != null) {
                stderr += line;
            }
            int r = proc.waitFor();

            JSONObject obj = new JSONObject(!stdout.equals("") ? stdout : stderr);
            if(!obj.getString("state").equals("connected")){
                throw new TunnelException(obj.getJSONObject("message").getString("message"));
            }
            else {
                pid = obj.getInt("pid");
            }
        }
    }

    public void stop() throws Exception {
        if (pid != 0) {
            makeCommand(startOptions, "stop");
            proc = runCommand(command);
            proc.waitFor();
            pid = 0;
        }
    }

    /**
    * Stops the Tunnel instance specified by the given identifier
    * @param options Options supplied for the Tunnel instance
    **/
    public void stop(Map<String, String> options) throws Exception {
        if (options.get("binarypath") != null) {
            binaryPath = options.get("binarypath");
        } else {
            TunnelBinary lb = new TunnelBinary();
            binaryPath = lb.getBinaryPath();
        }
        makeCommand(options, "stop");
        proc = runCommand(command);
        proc.waitFor();
        pid = 0;
    }

    /**
     * Checks if Tunnel instance is running
     *
     * @return true if Tunnel instance is running, else false
     */
    public boolean isRunning() throws Exception {
        if (pid == 0) return false;
        return isProcessRunning(pid);
    }

    /**
     * Creates a list of command-line arguments for the Tunnel instance
     *
     * @param options Options supplied for the Tunnel instance
     */
    private void makeCommand(Map<String, String> options, String opCode) {
        command = new ArrayList<String>();
        command.add(binaryPath);
        command.add("-d");
        command.add(opCode);
        command.add("--key");
        command.add(options.get("key"));

        for (Map.Entry<String, String> opt : options.entrySet()) {
            String parameter = opt.getKey().trim();
            if (IGNORE_KEYS.contains(parameter)) {
                continue;
            }
            if (voidValueParameters.get(parameter) != null && opt.getValue().trim().toLowerCase() != "false") {
                command.add(voidValueParameters.get(parameter));
            } else {
                if (parameters.get(parameter) != null) {
                    command.add(parameters.get(parameter));
                } else {
                    command.add("-" + parameter);
                }
                if (opt.getValue() != null) {
                    command.add(opt.getValue().trim());
                }
            }
        }
    }

    /**
     * Checks if process with pid is running
     *
     * @param pid pid for the process to be checked.
     * @link http://stackoverflow.com/a/26423642/941691
     */
    private boolean isProcessRunning(int pid) throws Exception {
        ArrayList<String> cmd = new ArrayList<String>();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            //tasklist exit code is always 0. Parse output
            //findstr exit code 0 if found pid, 1 if it doesn't
            cmd.add("cmd");
            cmd.add("/c");
            cmd.add("\"tasklist /FI \"PID eq " + pid + "\" | findstr " + pid + "\"");
        }
        else {
            //ps exit code 0 if process exists, 1 if it doesn't
            cmd.add("ps");
            cmd.add("-p");
            cmd.add(String.valueOf(pid));
        }

        proc = runCommand(cmd);
        int exitValue = proc.waitFor();

        // 0 is the default exit code which means the process exists
        return exitValue == 0;
    }

    /**
     * Executes the supplied command on the shell.
     *
     * @param command Command to be executed on the shell.
     * @return {@link TunnelProcess} for managing the launched process.
     * @throws IOException
     */
    protected TunnelProcess runCommand(List<String> command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        final Process process = processBuilder.start();

        return new TunnelProcess() {
            public InputStream getInputStream() {
                return process.getInputStream();
            }

            public InputStream getErrorStream() {
                return process.getErrorStream();
            }

            public int waitFor() throws Exception {
                return process.waitFor();
            }
        };
    }

    public interface TunnelProcess {
        InputStream getInputStream();

        InputStream getErrorStream();

        int waitFor() throws Exception;
    }
}
