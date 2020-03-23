package com.vgtech.vantop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.vgtech.common.api.UserAccount;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;


/**
 * @author xuanqiang
 */
public class PreferencesController {

    public void storageAccount(final UserAccount account) {
        String name = Strings.md5(UserAccount.class.getSimpleName());
        storageObject(account, name, name);
    }

    public UserAccount getAccount() {
            String name = Strings.md5(UserAccount.class.getSimpleName());
        UserAccount account = loadObject(name, name);
            if (account == null) {
                account = new UserAccount();
            }
        return account;
    }

    public UserAccount getAccountUnCache() {
        String name = Strings.md5(UserAccount.class.getSimpleName());
        UserAccount a = loadObject(name, name);
        return a;
    }

    public SharedPreferences sharedPref(final String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public <T extends Serializable> void storageObject(final T entity, final String fileName, final String keyName) {
        try {
            ByteArrayOutputStream toByte = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(toByte);
            oos.writeObject(entity);
            String str = Base64.encodeToString(toByte.toByteArray(), Base64.DEFAULT);
            sharedPref(fileName).edit().putString(keyName, str).commit();
        } catch (IOException e) {
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> T loadObject(final String fileName, final String keyName) {
        try {
            SharedPreferences sharedPreferences = sharedPref(fileName);
            if (sharedPreferences != null) {
                String content = sharedPreferences.getString(keyName, null);
                if (content != null) {
                    byte[] base64Bytes = Base64.decode(sharedPreferences.getString(keyName, null), Base64.DEFAULT);
                    ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    return (T) ois.readObject();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public Context context;

    public static class Strings {
        public static String md5(String inStr) {
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
                return "";
            }
            char[] charArray = inStr.toCharArray();
            byte[] byteArray = new byte[charArray.length];

            for (int i = 0; i < charArray.length; i++)
                byteArray[i] = (byte) charArray[i];
            byte[] md5Bytes = md5.digest(byteArray);
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        }
    }
}
