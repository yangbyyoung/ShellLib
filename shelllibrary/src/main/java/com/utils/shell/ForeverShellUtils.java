package com.utils.shell;

import android.content.Context;
import android.content.SharedPreferences;

import com.superuser.Shell;
import com.superuser.internal.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * spName = setting
 * keyRootCheck = RootPermission
 */
public class ForeverShellUtils implements ShellExitCode {
    private static final Context mContext = Utils.getContext();
    private static final String settingName = "setting";
    private static final SharedPreferences setting = mContext.getSharedPreferences(settingName, Context.MODE_PRIVATE);
    private static final String keyRootPermission = "RootPermission";
    private static ForeverShellUtils foreverShellUtils;
    //private static final Context mContext = ContextUtils.getContext();

    public static ForeverShellUtils getInstance() {
        if (foreverShellUtils == null) {
            foreverShellUtils = new ForeverShellUtils();
        }
        return foreverShellUtils;

    }


    public synchronized CommandResult execCommand(String[] commands) {

        CommandResult commandResult;
        int result = -1;
        if (commands == null || commands.length == 0) {
            commandResult = new CommandResult(result, (List<String>) null, null);
        } else {
            StringBuilder commandBuffer = new StringBuilder();
            for (String command : commands) {
                commandBuffer.append(command).append("\n");
            }
            commandResult = execCommand(commandBuffer.toString());
        }
        return commandResult;

    }

    public synchronized CommandResult execCommand(List<String> commands) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}));
    }

    public synchronized CommandResult execCommand(String commands) {
        CommandResult commandResult;
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();

        if (setting.getBoolean(keyRootPermission, false)) {
            Shell.Result result = Shell.su(commands).to(stdout, stderr).exec();
            commandResult = new CommandResult(result.getCode(), result.getOut(), result.getErr());
        } else {
            commandResult = new CommandResult(SHELL_NOT_FOUND, "", "没有ROOT权限");
        }

        return commandResult;

    }

    public synchronized CommandResult execCommand(Cmd pwdCmd) {
        StringBuilder commandBuffer = new StringBuilder();
        if (pwdCmd != null) {
            File dir = pwdCmd.getDir();
            String[] envps = pwdCmd.getEnvpArray();
            List<File> dirs = pwdCmd.getDirs();

            if (dir != null) {
                commandBuffer.append("export PATH=$PATH:").append(dir.getPath()).append("\n");
            }

            if (dirs != null && !dirs.isEmpty()) {
                for (File file : dirs) {
                    commandBuffer.append("export PATH=$PATH:").append(file.getPath()).append("\n");
                }
            }
            if (envps != null && envps.length != 0) {
                for (String envp : envps) {
                    commandBuffer.append("export ").append(envp).append("\n");
                }
            }
            commandBuffer.append(pwdCmd.getShell());
        }

       /* if (isRoot) {
        } else {
            result = Shell.sh(commandBuffer.toString()).to(stdout, stderr).exec();
        }*/
        return execCommand(commandBuffer.toString());

    }

    public synchronized boolean execShell(String command) {

        return execCommand(command).result >= 0;
    }

    public synchronized boolean execShell(String[] commands) {

        return execCommand(commands).result >= 0;
    }


   /* public synchronized CommandResult execCommand(String cmd) {
        CommandResult commandResult;
        try {
            sOutStream.write(cmd.getBytes(StandardCharsets.UTF_8));
            sOutStream.writeBytes("\n");
            // exit Process
            sOutStream.writeBytes(COMMAND_EXIT);
            sOutStream.writeBytes("\n");
            sOutStream.flush();
            int code = sProcess.waitFor();
            sSuccessStream = new InputStreamReader(sProcess.getInputStream());
            sErrStream = new InputStreamReader(sProcess.getErrorStream());
            commandResult = new CommandResult(code, getString(sSuccessStream), getString(sErrStream));
        } catch (Exception e) {
            commandResult = new CommandResult(-1, null, e.getMessage());
        }
        return commandResult;
    }*/


    /*private static String getString(InputStreamReader inputStreamReader) {
        StringBuilder textBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            String readLine;
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((readLine = bufferedReader.readLine()) != null) {
                if (textBuilder.length() > 0) {
                    textBuilder.append("\n");
                }
                textBuilder.append(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return textBuilder.toString();
    }
*/


    public void close() {
        foreverShellUtils = null;
    }

    public void reset() {
        close();
        getInstance();
    }


}