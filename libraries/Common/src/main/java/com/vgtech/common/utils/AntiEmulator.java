package com.vgtech.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by vic on 2016/11/18.
 */
public class AntiEmulator {
    private final int REQUEST_READ_PHONE_STATE = 0x000000;
    private static String[] known_pipes = {
            "/dev/socket/qemud",
            "/dev/qemu_pipe"
    };

    private static String[] known_qemu_drivers = {
            "goldfish"
    };

    private static String[] known_files = {
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
    };

    private static String[] known_numbers = {"15555215554", "15555215556",
            "15555215558", "15555215560", "15555215562", "15555215564",
            "15555215566", "15555215568", "15555215570", "15555215572",
            "15555215574", "15555215576", "15555215578", "15555215580",
            "15555215582", "15555215584",};

    private static String[] known_device_ids = {
            "000000000000000" // 默认ID
    };

    private static String[] known_imsi_ids = {
            "310260000000000" // 默认的 imsi id
    };

    //检测“/dev/socket/qemud”，“/dev/qemu_pipe”这两个通道
    public static boolean checkPipes() {
        for (int i = 0; i < known_pipes.length; i++) {
            String pipes = known_pipes[i];
            File qemu_socket = new File(pipes);
            if (qemu_socket.exists()) {
                Log.v("Result:", "Find pipes!");
                return true;
            }
        }
        Log.i("Result:", "Not Find pipes!");
        return false;
    }

    // 检测驱动文件内容
// 读取文件内容，然后检查已知QEmu的驱动程序的列表
    public static Boolean checkQEmuDriverFile() {
        File driver_file = new File("/proc/tty/drivers");
        if (driver_file.exists() && driver_file.canRead()) {
            byte[] data = new byte[1024];  //(int)driver_file.length()
            try {
                InputStream inStream = new FileInputStream(driver_file);
                inStream.read(data);
                inStream.close();
            } catch (Exception e) {
// TODO: handle exception
                e.printStackTrace();
            }
            String driver_data = new String(data);
            for (String known_qemu_driver : AntiEmulator.known_qemu_drivers) {
                if (driver_data.indexOf(known_qemu_driver) != -1) {
                    Log.i("Result:", "Find know_qemu_drivers!");
                    return true;
                }
            }
        }
        Log.i("Result:", "Not Find known_qemu_drivers!");
        return false;
    }

    //检测模拟器上特有的几个文件
    public static Boolean CheckEmulatorFiles() {
        for (int i = 0; i < known_files.length; i++) {
            String file_name = known_files[i];
            File qemu_file = new File(file_name);
            if (qemu_file.exists()) {
                Log.v("Result:", "Find Emulator Files!");
                return true;
            }
        }
        Log.v("Result:", "Not Find Emulator Files!");
        return false;
    }

    // 检测模拟器默认的电话号码
    public static Boolean CheckPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission") String phonenumber = telephonyManager.getLine1Number();

        for (String number : known_numbers) {
            if (number.equalsIgnoreCase(phonenumber)) {
                Log.v("Result:", "Find PhoneNumber!");
                return true;
            }
        }
        Log.v("Result:", "Not Find PhoneNumber!");
        return false;
    }

    //检测设备IDS 是不是 “000000000000000”
    public static Boolean CheckDeviceIDS(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission") String device_ids = telephonyManager.getDeviceId();

        for (String know_deviceid : known_device_ids) {
            if (know_deviceid.equalsIgnoreCase(device_ids)) {
                Log.v("Result:", "Find ids: 000000000000000!");
                return true;
            }
        }
        Log.v("Result:", "Not Find ids: 000000000000000!");
        return false;
    }

    // 检测imsi id是不是“310260000000000”
    public static Boolean CheckImsiIDS(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission") String imsi_ids = telephonyManager.getSubscriberId();

        for (String know_imsi : known_imsi_ids) {
            if (know_imsi.equalsIgnoreCase(imsi_ids)) {
                Log.v("Result:", "Find imsi ids: 310260000000000!");
                return true;
            }
        }
        Log.v("Result:", "Not Find imsi ids: 310260000000000!");
        return false;
    }

    //检测手机上的一些硬件信息
    public static Boolean CheckEmulatorBuild(Context context) {
//        String BOARD = android.os.Build.BOARD;
//        String BOOTLOADER = android.os.Build.BOOTLOADER;
        String BRAND = android.os.Build.BRAND;
        String DEVICE = android.os.Build.DEVICE;
        String HARDWARE = android.os.Build.HARDWARE;
        String MODEL = android.os.Build.MODEL;
        String PRODUCT = android.os.Build.PRODUCT;
        if (BRAND == "generic" || DEVICE == "generic"
                || MODEL == "sdk" || PRODUCT == "sdk"
                || HARDWARE == "goldfish") {
            Log.v("Result:", "Find Emulator by EmulatorBuild!");
            return true;
        }
        Log.v("Result:", "Not Find Emulator by EmulatorBuild!");
        return false;
    }

    //检测手机运营商家
    public static boolean CheckOperatorNameAndroid(Context context) {
        String szOperatorName = ((TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();

        if (szOperatorName.toLowerCase().equals("android") == true) {
            Log.v("Result:", "Find Emulator by OperatorName!");
            return true;
        }
        Log.v("Result:", "Not Find Emulator by OperatorName!");
        return false;
    }

    /**
     * 判断是否模拟器。如果返回TRUE，则当前是模拟器
     *
     * @param context
     * @return
     */
    public static boolean isEmulator(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            //TODO
            boolean checkPipes = checkPipes();
            boolean checkQEmuDriverFile = checkQEmuDriverFile();
//        boolean CheckEmulatorFiles = CheckEmulatorFiles();
            boolean CheckPhoneNumber = CheckPhoneNumber(context);
            boolean CheckDeviceIDS = CheckDeviceIDS(context);
            boolean CheckImsiIDS = CheckImsiIDS(context);
            boolean CheckEmulatorBuild = CheckEmulatorBuild(context);
            boolean CheckOperatorNameAndroid = CheckOperatorNameAndroid(context);
            boolean canCallPhoneIntent = checkCallPhoneIntent(context);
//        FileUtils.writeString("checkPipes = " + checkPipes + "\r\n" +
//                "checkQEmuDriverFile = " + checkQEmuDriverFile + "\r\n" +
////                "CheckEmulatorFiles = " + CheckEmulatorFiles + "\r\n" +
//                "CheckPhoneNumber = " + CheckPhoneNumber + "\r\n" +
//                "CheckDeviceIDS = " + CheckDeviceIDS + "\r\n" +
//                "CheckImsiIDS = " + CheckImsiIDS + "\r\n" +
//                "CheckEmulatorBuild = " + CheckEmulatorBuild + "\r\n" +
//                "CheckOperatorNameAndroid = " + CheckOperatorNameAndroid + "\r\n" +
//                "canCallPhoneIntent = " + canCallPhoneIntent);
            return checkPipes || checkQEmuDriverFile /*|| CheckEmulatorFiles */ || CheckPhoneNumber
                    || CheckDeviceIDS || CheckImsiIDS || CheckEmulatorBuild || CheckOperatorNameAndroid
                    || !canCallPhoneIntent;
        }
    }

    private static boolean checkCallPhoneIntent(Context context) {
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        // 是否可以处理跳转到拨号的 Intent
        return intent.resolveActivity(context.getPackageManager()) != null;

    }
}
