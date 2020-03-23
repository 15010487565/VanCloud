package com.vgtech.vancloud.ui.common.group;

import android.content.Context;

import com.vgtech.common.api.Group;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.User;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/8/20.
 */
public class GroupUtils {
    public static boolean initGroups(Context context, JSONObject jsonObject) throws Exception {

        Group group = null;
        group = JsonDataFactory.getData(Group.class, jsonObject);
        List<Group> groups = new ArrayList<Group>();
        groups.add(group);
        List<Group> childGroups = generaterGroup(group);
        groups.addAll(childGroups);
       return Department.updateGroupTable(groups, context);
    }

    public static List<Group> initGroups(JSONObject jsonObject) {
        List<Group> groups = new ArrayList<Group>();

        try {
            Group group = JsonDataFactory.getData(Group.class, jsonObject);

//            groups.add(group);
            List<Group> childGroups = generaterGroup(group);
            groups.addAll(childGroups);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return groups;

    }

    public static boolean initUsers(Context context, JSONObject jsonObject) throws JSONException {
        List<User> userList = JsonDataFactory.getDataArray(User.class, jsonObject.getJSONArray("users"));
       return  com.vgtech.common.provider.db.User.updateUserTable(userList, context);
    }

    public static boolean initWorkGroupInfo(Context context, JSONObject jsonObject)throws Exception{
            JSONArray jsonArray = jsonObject.getJSONArray("workGroups");
            List<WorkGroup> workGroups = new ArrayList<WorkGroup>();
            List<WorkRelation> relations = new ArrayList<WorkRelation>();
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject groupObj = jsonArray.getJSONObject(i);

                String id = groupObj.getString("id");
                String name = groupObj.getString("name");
                WorkGroup wg = new WorkGroup();
                wg.wgtoupId = id;
                wg.name = name;
                workGroups.add(wg);
                JSONArray userArray = groupObj.getJSONArray("user");
                for (int j = 0; j < userArray.length(); j++) {
                    JSONObject userObj = userArray.getJSONObject(j);
                    String userid = userObj.getString("userid");
                    WorkRelation wr = new WorkRelation();
                    wr.wgtoupId = wg.wgtoupId;
                    wr.userId = userid;
                    relations.add(wr);
                }
            }
           return  WorkGroup.updateWorkGroupTable(workGroups, context)&WorkRelation.updateWorkGroupRelationTable(relations, context);
    }

    public static List<Group> generaterGroup(Group group) {
        List<Group> groups = new ArrayList<Group>();
        List<Group> tmpGroup = group.getArrayData(Group.class);
        if (tmpGroup != null && !tmpGroup.isEmpty()) {
            for (Group g : tmpGroup) {
                g.setPid(group.getGid());
                groups.addAll(generaterGroup(g));
            }
            groups.addAll(tmpGroup);
        }
        return groups;
    }

    public static byte[] read(InputStream inStream) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream.toByteArray();
        } catch (IOException e) {
        }
        return new byte[0];
    }

}
