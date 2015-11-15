package com.shareplaylearn;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by stu on 11/14/15.
 */
public class Exceptions {
    public static String traceToString( Throwable t ) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
