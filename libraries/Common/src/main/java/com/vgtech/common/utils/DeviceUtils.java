package com.vgtech.common.utils;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.vgtech.common.ui.zxing.zxing.ToastUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by vic on 2016/12/8.
 * 系统-工具类
 */
public class DeviceUtils {
    /**
     *
     * @param context
     * @param op  op 的值是 0 ~ 47，其中0代表粗略定位权限，1代表精确定位权限，24代表悬浮窗权限
     * @return  0 就代表有权限，1代表没有权限，-1函数出错啦
     */
    public static int checkOp(Context context, int op){
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19){
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            Class c = object.getClass();
            try {
                Class[] cArg = new Class[3];
                cArg[0] = int.class;
                cArg[1] = int.class;
                cArg[2] = String.class;
                Method lMethod = c.getDeclaredMethod("checkOp", cArg);
                return (Integer) lMethod.invoke(object, op, Binder.getCallingUid(), context.getPackageName());
            } catch(NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
    // dip--px
    public static int convertDipOrPx(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * Checks if the device is rooted.
     *
     * @return <code>true</code> if the device is rooted, <code>false</code> otherwise.
     *
     */
    public static boolean checkRoot() {

        if (checkRootMethod1()){
            return true;
        }
        if (checkRootMethod2()){
            return true;
        }
        if (checkRootMethod3()){
            return true;
        }
        return false;

//        // get from build info
//        String buildTags = android.os.Build.TAGS;
//        if (buildTags != null && buildTags.contains("test-keys")) {
//            return true;
//        }
//
//        // check if /system/app/Superuser.apk is present
//        try {
//            File file = new File("/system/app/Superuser.apk");
//            if (file.exists()) {
//                return true;
//            }
//        } catch (Exception e1) {
//            // ignore
//        }
//
//        // try executing commands
//        return canExecuteCommand("/system/xbin/which su")
//                || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su")
//                || canExecuteCommand("busybox which su");
    }

    private static boolean checkRootMethod3() {
        ArrayList<String> fullResponse = new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary);
        if (fullResponse != null && fullResponse.size() > 0){
            return true;
        }
        ArrayList<String> fullResponse1 = new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary1);
        if (fullResponse1 != null&& fullResponse1.size() > 0){
            return true;
        }
        ArrayList<String> fullResponse2 = new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary2);
        if (fullResponse2 != null && fullResponse2.size() > 0){
            return true;
        }
        ArrayList<String> fullResponse3 = new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary3);
        if (fullResponse3 != null && fullResponse3.size() > 0){
            return true;
        }

        return false;
    }

    private static boolean checkRootMethod2() {
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) { }

        return false;
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;

        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }
        return false;
    }

    // executes a command on the system
    private static boolean canExecuteCommand(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String info = in.readLine();
            if (info != null) return true;
            return false;
        } catch (Exception e) {
            //do noting
        } finally {
            if (process != null) process.destroy();
        }
        return false;
    }
}
