package com.vgtech.vancloud.ui.chat.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.google.inject.Inject;
import com.vgtech.common.api.UserAccount;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import roboguice.inject.ContextSingleton;
import roboguice.util.Ln;
import roboguice.util.Strings;

/**
 * @author xuanqiang
 */
@ContextSingleton
public class PreferencesController {
    static UserAccount account;

    public void storageAccount(final UserAccount account) {
        String name = Strings.md5(UserAccount.class.getSimpleName());
        storageObject(account, name, name);
        PreferencesController.account = account;
    }

    public UserAccount getAccount() {
        if (account == null|| TextUtils.isEmpty(account.user_id)) {
            String name = Strings.md5(UserAccount.class.getSimpleName());
            account = loadObject(name, name);
            if (account == null) {
                account = new UserAccount();
            }
        }
        return account;
    }

    public UserAccount getAccountUnCache() {
        String name = Strings.md5(UserAccount.class.getSimpleName());
        UserAccount a = loadObject(name, name);
        if (a != null) {
            account = a;
        }
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
            Ln.e(e);
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
            Ln.e(e);
        }
        return null;
    }

    @Inject
    public Context context;
}
