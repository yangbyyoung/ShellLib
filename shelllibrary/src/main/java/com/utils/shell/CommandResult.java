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

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.superuser.Shell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 命令执行结果
 */
public class CommandResult implements ShellExitCode {
    private final List<String> ListSuccessMsg;
    private final List<String> ListErrorMsg;
    public int result;
    private final int code;
    private Cmd cmd;
    private  String successText;
    private final String errorText;
    private Exception exception;
    private transient String exceptionStackTrace;
    private transient static final List<String> emptyList=new ArrayList<>();

    public CommandResult(Shell.Result result) {
        this.code = result.getCode();
        this.result = result.getCode();
        this.ListSuccessMsg = result.getOut();
        this.successText = toString(result.getOut());
        this.ListErrorMsg = result.getErr();
        this.errorText = toString(result.getErr());
    }

    @Deprecated
    public CommandResult(@NonNull Cmd cmd, int code, @Nullable String text, @Nullable String errorText, @Nullable Exception exception) {
        this.result = code;
        this.cmd = cmd;
        this.code = code;
        this.successText = text == null ? "" : text;
        this.errorText = errorText == null ? "" : errorText;
        this.ListSuccessMsg = toList(text);
        this.ListErrorMsg = toList(errorText);
        this.exception = exception;
    }

    public CommandResult(int result, List<String> listSuccessMsg, List<String> listErrorMsg) {
        this.code = result;
        this.result = result;
        this.ListSuccessMsg = listSuccessMsg==null?emptyList:listSuccessMsg;
        this.successText = toString(listSuccessMsg);
        this.ListErrorMsg = listErrorMsg==null?emptyList:listErrorMsg;
        this.errorText = toString(listErrorMsg);
    }


    public CommandResult(int result, String successMsg, String errorMsg) {
        this.code = result;
        this.result = result;
        this.ListSuccessMsg = toList(successMsg);
        this.successText = successMsg == null ? "" : successMsg;
        this.ListErrorMsg = toList(errorMsg);
        this.errorText = errorMsg == null ? "" : errorMsg;


    }




    /**
     * 是否成功，根据返回的状态判断，等于 0 即为成功
     */
    public boolean isSuccessful() {
        return this.result == SUCCESS;
    }


    /**
     * @return 精确判断每一步
     */
    private boolean isSuccess() {
        if (code == SUCCESS) {
            // 虽然 code 为 0 ，但是只有错误信息，说明还是失败了
            return !TextUtils.isEmpty(successText) || TextUtils.isEmpty(errorText);
        } else {
            return code > SUCCESS && !TextUtils.isEmpty(successText) && !TextUtils.isEmpty(errorText);
        }
    }

    /**
     * 是否是因为异常导致的失败
     */
    public boolean isException() {
        return code == -1 && exception != null;
    }

    /**
     * 是否超时
     *
     * @return 超时了
     */
    public boolean isTimeout() {
        return code == -2;
    }


    /**
     * 获取执行结果状态码
     *
     * @return 0：成功；1：失败；-1：过程异常导致失败
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取成功时返回的结果
     */
    public String getSuccessText() {
        return successText;
    }

    public List<String> getListSuccessMsg() {
        return ListSuccessMsg;
    }

    /**
     * 获取失败时返回的结果
     */
    public String getErrorText() {
        return errorText;
    }

    public List<String> getListErrorMsg() {
        return ListErrorMsg;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    /**
     * 获取异常消息
     */
    public String getExceptionMessage() {
        return exception != null ? exception.getLocalizedMessage() : null;
    }


    @Deprecated
    /**
     * 获取完整的异常栈信息
     */
    public String getExceptionStackTrace() {
        if (exception == null) {
            return null;
        }
        if (exceptionStackTrace == null) {
            synchronized (this) {
                if (exceptionStackTrace == null) {
                    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                    exception.printStackTrace(new PrintStream(arrayOutputStream));
                    exceptionStackTrace = new String(arrayOutputStream.toByteArray());
                }
            }
        }
        return exceptionStackTrace;
    }

    /**
     * @return 自动适配Html
     */
    public String getCommandHtml() {
        StringBuilder html = new StringBuilder();
        // exit status
        html.append("<p><strong>Exit Code:</strong> ");
        if (isSuccessful()) {
            html.append("<font color='green'>").append(getCode()).append("</font>");
        } else {
            html.append("<font color='red'>").append(getCode()).append("</font>");
        }
        html.append("</p>");
        // stdout


        if (!TextUtils.isEmpty(getSuccessText())) {
            html.append("<p><strong>STDOUT:</strong></p><p>")
                    .append(getSuccessText().replaceAll("\n", "<br>"))
                    .append("</p>");
        }
        // stderr
        if (!TextUtils.isEmpty(getErrorText())) {
            html.append("<p><strong>STDERR:</strong></p><p><font color='red'>")
                    .append(getErrorText().replaceAll("\n", "<br>"))
                    .append("</font></p>");
        }
        return html.toString();
    }

    /**
     * @return 自动适配可显示Html
     */
    public Spanned getHtml() {
        return Html.fromHtml(getCommandHtml());
    }

    @Deprecated
    public Cmd getCmd() {
        return cmd;
    }

    private static String toString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        if (list == null) {
            return sb.toString();
        } else if (list.isEmpty()) {
            return sb.toString();
        } else {
            String emptyOrNewLine = "";
            for (String line : list) {
                sb.append(emptyOrNewLine).append(line);
                emptyOrNewLine = "\n";
            }
            return sb.toString();
        }
    }
    private List<String> toList(String str) {
        List<String> stringList = new ArrayList<>();
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("\n");
            stringList.addAll(Arrays.asList(split));
        }
        return stringList;
    }
}
