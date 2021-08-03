package com.utils.shell;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.superuser.Shell;
import com.superuser.internal.Utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * spName = setting
 * keyRootCheck = RootPermission
 */
public class ShellUtils implements ShellExitCode {
    private static final Context mContext = Utils.getContext();
    private static final String settingName = "setting";
    private static final SharedPreferences setting = mContext.getSharedPreferences(settingName,Context.MODE_PRIVATE);
    private static final String keyRootPermission = "RootPermission";
    public static final Charset UTF_8 =
            Build.VERSION.SDK_INT >= 19 ? StandardCharsets.UTF_8 : Charset.forName("UTF-8");

    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";

    public ShellUtils() {
        throw new AssertionError();
    }


    /**
     * check whether has root permission
     *
     * @return
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true).result == 0;
    }


    /**
     * execute shell command, default return result msg
     *
     * @param command command
     * @param isRoot  whether need to run with root
     * @return
     * @see #execCommand(String[], boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot);
    }

    public static CommandResult execCommand(StringBuilder commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toString(), isRoot);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands command list
     * @param isRoot   whether need to run with root
     * @return
     * @see #execCommand(String[], boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param pwdCmd command array
     * @param isRoot whether need to run with root
     * @return
     * @see #execCommand(String[], boolean)
     */

    public static CommandResult execCommand(Cmd pwdCmd, boolean isRoot) {
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
        return execCommand(pwdCmd == null ? null : commandBuffer, isRoot);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        CommandResult commandResult;
        if (commands == null || commands.length == 0) {
            commandResult = new CommandResult(WATCHDOG_EXIT, (List<String>) null, null);
        } else {
            StringBuilder commandBuffer = new StringBuilder();
            for (String command : commands) {
                commandBuffer.append(command).append("\n");
            }
            Shell.Job job = buildJob(isRoot).add(commandBuffer.toString());
            Shell.Result shellResult = job.exec();
            List<String> ErrList = shellResult.getErr();
            List<String> OutList = shellResult.getOut();
            if (!shellResult.isSuccess()) {
                if (shellResult.getCode() == COMMAND_NOT_FOUND) {
                    if (setting.getBoolean(keyRootPermission, false)) {
                        ErrList.add("错误信息：您要执行的命令不存在\n例如：当前BusyBox没有此命令");
                    } else {
                        ErrList.add("错误信息：没有Root权限");
                    }
                } else {
                    ErrList.add("错误识别码：" + shellResult.getCode());
                }
            }
            commandResult = new CommandResult(shellResult.getCode(), OutList, ErrList);
        }
        return commandResult;

    }

    /**
     * 不带任何提示，请根据喜好自定义
     */
    public static CommandResult execShell(Cmd pwdCmd, boolean isRoot) {
        CommandResult commandResult;
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
            Shell.Job job = buildJob(isRoot).add().add(commandBuffer.toString());
            Shell.Result shellResult = job.exec();
            commandResult = new CommandResult(shellResult);
        } else {
            commandResult = new CommandResult(WATCHDOG_EXIT, (List<String>) null, null);
        }
        return commandResult;

    }

    /**
     * result of command
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal, else means error, same to excute in
     * linux shell</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-16
     */

    @Deprecated
    public static boolean execShell(String command, boolean isRoot) {


        Process process = null;
        boolean Result;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            os.write(command.getBytes(UTF_8));
            os.writeBytes(COMMAND_LINE_END);
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            process.waitFor();
            Result = true;
        } catch (Exception e) {
            Log.e("测试", e.getMessage());

            Result = false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return Result;
    }

    private static Shell.Job buildJob(boolean isRoot) {
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        Shell.Builder builder = Shell.Builder.create();
        if (isRoot) {
            builder.setFlags(Shell.FLAG_REDIRECT_STDERR);
        } else {
            builder.setFlags(Shell.FLAG_NON_ROOT_SHELL);
        }

        Shell.Job job = builder.build().newJob();
        job.to(stdout, stderr);
        return job;
    }


}
