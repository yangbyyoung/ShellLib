/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.utils.shell;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 单个可执行的命令，可配置环境变量和工作目录
 */
@SuppressWarnings("WeakerAccess")
public class Cmd {
    private String shell;
    private List<String> envpList;
    private File dir;
    private boolean printLog;
    private int timeout;
    private List<File> dirs;
    private final StringBuilder CommandBuffer = new StringBuilder();

    public Cmd(@NonNull String shell) {
        //noinspection ConstantConditions
        if (shell == null || "".equals(shell)) {
            throw new IllegalArgumentException("param shell is null or empty");
        }
        this.shell = shell;
    }

    public Cmd(@NonNull String[] shell) {

        //noinspection ConstantConditions
        if (shell == null || shell.length == 0) {
            throw new IllegalArgumentException("param shell is null or empty");
        }
        StringBuilder CommandBuffer = new StringBuilder();
        for (String command : shell) {
            if (command == null) {
                continue;
            }
            CommandBuffer.append(command).append("\n");
        }

        this.shell = CommandBuffer.toString();
    }

    public Cmd() {
        this.shell = "";
    }

    public Cmd addShell(String shell) {

        if (!TextUtils.isEmpty(shell)) {
            CommandBuffer.append(shell).append("\n");
        }
        this.shell = CommandBuffer.toString();
        return this;
    }

    /**
     * 打印执行过程
     */
    public Cmd printLog() {
        this.printLog = true;
        return this;
    }

    /**
     * 设置环境变量，会清空旧的
     *
     * @param envps 环境变量数据，每个元素的格式为 name=value
     * @return Cmd
     */
    @NonNull
    @SuppressWarnings("unused")
    public Cmd envp(@Nullable String[] envps) {
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        envpList.clear();

        //noinspection ConstantConditions
        if (envps != null && envps.length > 0) {
            Collections.addAll(envpList, envps);
        }
        return this;
    }

    /**
     * 设置环境变量，会清空旧的
     *
     * @param envpList 环境变量列表，每个元素的格式为 name=value
     * @return Cmd
     */
    @NonNull
    @SuppressWarnings("unused")
    public Cmd envp(@Nullable List<String> envpList) {
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.clear();

        //noinspection ConstantConditions
        if (envpList != null && !envpList.isEmpty()) {
            this.envpList.addAll(envpList);
        }
        return this;
    }

    /**
     * 添加环境变量
     *
     * @param envp 环境变量，格式为 name=value
     * @return Cmd
     */
    @NonNull
    @SuppressWarnings("unused")
    public Cmd addEnvp(@NonNull String envp) {
        if (TextUtils.isEmpty(envp)) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.add(envp);
        return this;
    }

    /**
     * 添加环境变量
     *
     * @param envpKey   环境变量 KEY
     * @param envpValue 环境变量 VALUE
     * @return Cmd
     */
    @NonNull
    @SuppressWarnings("unused")
    public Cmd addEnvp(@NonNull String envpKey, @NonNull String envpValue) {
        if (TextUtils.isEmpty(envpKey) || TextUtils.isEmpty(envpValue)) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.add(envpKey + "=" + envpValue);
        return this;
    }

    /**
     * 批量添加环境变量，每个元素的格式为 name=value
     *
     * @param envps 环境变量数据，每个元素的格式为 name=value
     * @return Cmd
     */
    @NonNull
    @SuppressWarnings("unused")
    public Cmd addEnvpAll(@NonNull String[] envps) {
        //noinspection ConstantConditions
        if (envps == null || envps.length <= 0) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        Collections.addAll(envpList, envps);
        return this;
    }

    /**
     * 添加环境变量，每个元素的格式为 name=value
     *
     * @param envpList 环境变量列表，每个元素的格式为 name=value
     * @return Cmd
     */
    @NonNull
    @SuppressWarnings("unused")
    public Cmd addEnvpAll(@NonNull List<String> envpList) {
        //noinspection ConstantConditions
        if (envpList == null || envpList.isEmpty()) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.addAll(envpList);
        return this;
    }

    /**
     * 设置当前命令的工作目录
     *
     * @param dir 工作目录
     * @return Cmd
     */
    @NonNull
    @SuppressWarnings("unused")
    public Cmd dir(@Nullable File dir) {
        this.dir = dir;
        return this;
    }

    public Cmd dirs(@Nullable List<File> dirs) {
        this.dirs = dirs;
        return this;
    }

    public Cmd addDirs(File dir) {
        if (dir == null) {
            return this;
        }
        if (this.dirs == null) {
            this.dirs = new LinkedList<>();
        }
        this.dirs.add(dir);
        return this;
    }

    /**
     * 设置超时时间
     *
     * @param timeout 超时时间，单位毫秒
     * @return Cmd
     */
    @SuppressWarnings("unused")
    public Cmd timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @NonNull
    public String getShell() {
        return shell;
    }

    public Cmd setShell(@NonNull String shell) {
        if ("".equals(shell)) {
            throw new IllegalArgumentException("param shell is null or empty");
        } else {
            this.shell = shell;
        }
        return this;
    }

    @Nullable
    @SuppressWarnings("unused")
    public List<String> getEnvpList() {
        return envpList;
    }

    @Nullable
    public String[] getEnvpArray() {
        return envpList != null && !envpList.isEmpty() ? envpList.toArray(new String[envpList.size()]) : null;
    }

    @Nullable
    public File getDir() {
        return dir;
    }

    @Nullable
    public List<File> getDirs() {
        return dirs;
    }

    public boolean isPrintLog() {
        return printLog;
    }

    public int getTimeout() {
        return timeout;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Cmd");
        builder.append("{");
        builder.append("shell=").append(shell);
        if (timeout > 0) {
            builder.append(", timeout=").append(timeout);
        }
        if (envpList != null && !envpList.isEmpty()) {
            builder.append(", envps=").append(envpList);
        }
        if (dir != null) {
            builder.append(", dir=").append(dir);
        }
        builder.append('}');
        return builder.toString();
    }
}
