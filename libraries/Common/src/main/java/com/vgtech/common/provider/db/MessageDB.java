package com.vgtech.common.provider.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.utils.TypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息表
 * 用于存储存储消息
 *
 * @author zhangshaofang
 */
public class MessageDB extends AbsData {
    public static final String TABLE_NAME = "message_push";
    /**
     * 未读
     */
    public static final int MESSAGE_STATE_UNREAD = 0;
    /**
     * 已读
     */
    public static final int MESSAGE_STATE_READ = 1;
    /**
     * 消息类型
     */
    private static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    private static final String OPERATIONTYPE = "OPERATIONTYPE";
    /**
     * 消息标题
     */
    private static final String MESSAGE_TITLE = "MESSAGE_TITLE";
    /**
     * 消息内容 json
     */
    private static final String MESSAGE_CONTENT = "MESSAGE_CONTENT";
    /**
     * 消息状态  0未读。。1已读
     */
    private static final String MESSAGE_STATE = "MESSAGE_STATE";
    public String type;
    public String operationType;
    public String title;
    public String content;
    public int messageState;


    public MessageDB() {
    }

    public MessageDB(String type, String title, String operationtype, String jsonContent) {
        this.type = type;
        this.title = title;
        this.content = jsonContent;
        this.operationType = operationtype;
        this.messageState = MESSAGE_STATE_UNREAD;
    }

    /**
     * 标志已读状态
     *
     * @param context
     */
    public void makeRead(Context context) {
        mContext = context;
        if (_id == -1) {
            return;
        }
        Uri uri = ContentUris.withAppendedId(
                getContentUri(getClass(), context), _id);
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, null, null);
//        messageState = MESSAGE_STATE_READ;
//        ContentValues values = putValues();
//        resolver.update(uri, values, null, null);
    }

    /**
     * 标志已读状态
     *
     * @param context
     */
    public static void makeRead(Context context, long rowId) {
        if (rowId == -1) {
            return;
        }
        Uri uri = ContentUris.withAppendedId(
                getContentUri(MessageDB.class, context), rowId);
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, null, null);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static String createTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(TABLE_NAME).append("(").append(_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(MESSAGE_TYPE).append(" STRING, ")
                .append(OPERATIONTYPE).append(" STRING, ")
                .append(MESSAGE_TITLE).append(" STRING, ")
                .append(MESSAGE_CONTENT).append(" STRING, ")
                .append(MESSAGE_STATE).append(" INTEGER, ")
                .append(USERID).append(" STRING, ")
                .append(TENANTID).append(" STRING, ")
                .append(TIMESTAMP).append(" LONG ").append(");");
        return sb.toString();
    }

    /**
     * 查询所有未读消息 by 消息类型
     *
     * @param context
     * @param type    消息类型
     * @return
     */
    public static ArrayList<MessageDB> queryUnReadMessageByTypeId(Context context, String type) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = MESSAGE_TYPE + " = '" + type + "'" + " AND " + MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD + "'" + " AND " + USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId + "'";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 统计所有未读消息 groupby 消息类型
     *
     * @param context
     * @return
     */
    public static Map<String, Integer> queryUnReadMessageGroupByType(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD + "'" + " AND " + USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId + "'" + " group by " + MESSAGE_TYPE;
        String[] CALL_LOG_PROJECTION = new String[]{
                MESSAGE_TYPE,
                "COUNT(*) AS " + "AMOUNT"
        };
        Cursor cursor = resolver.query(uri, CALL_LOG_PROJECTION, selection, null, null);
        Map<String, Integer> countMap = new HashMap<String, Integer>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            String messageType = cursor.getString(cursor.getColumnIndex(MESSAGE_TYPE));
            int count = cursor.getInt(cursor.getColumnIndex("AMOUNT"));
            countMap.put(messageType, count);
        }
        cursor.close();
        return countMap;
    }

    /**
     * 统计所有未读消息数量(消息，公告，通知)
     *
     * @param context
     * @return
     */
    public static int queryUnReadMessagee(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD +
                "' AND (" + MESSAGE_TYPE + " <> '" + TypeUtils.SYSTEMNOTIFICATION +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.NOTICE +
                "' AND (" + OPERATIONTYPE + " <> '" + "hasten" +
                "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_COMMENT +
                "' AND (" + MESSAGE_TYPE + " = '" + TypeUtils.SCHEDULE + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE +
                "') OR (" + MESSAGE_TYPE + " = '" + TypeUtils.TSAK + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE +
                "') OR (" + MESSAGE_TYPE + " = '" + TypeUtils.WORKREPORT + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE +
                "'))";
        String[] CALL_LOG_PROJECTION = new String[]{
                MESSAGE_TYPE,
                "COUNT(*) AS " + "AMOUNT"
        };
        Cursor cursor = resolver.query(uri, CALL_LOG_PROJECTION, selection, null, null);
        int count = 0;
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            String messageType = cursor.getString(cursor.getColumnIndex(MESSAGE_TYPE));
            count += cursor.getInt(cursor.getColumnIndex("AMOUNT"));
        }
        cursor.close();
        return count;
    }

    /**
     * 查询所有消息 去除系统通知、评论、公告、待办、催办、日程创建、任务创建、工作汇报创建
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryAllMessage(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.SYSTEMNOTIFICATION +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.NOTICE +
                "' AND " + OPERATIONTYPE + " <> 'hasten'"


//                + " AND " + OPERATIONTYPE + " <> 'create'" //创建
//
//                + " AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_COMMENT //去除回复
//
                + " AND (" + MESSAGE_TYPE + " = '" + TypeUtils.SCHEDULE + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.SCHEDULE
                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.APPROVAL_OVERTIME + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.APPROVAL_OVERTIME
                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.VANTOPLEAVE + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.VANTOPLEAVE

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.VANTOPSIGNCARD + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.VANTOPSIGNCARD

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.APPROVAL_FLOW + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.APPROVAL_FLOW

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.TSAK + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.TSAK

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.WORKREPORT + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.WORKREPORT
                + "')"
                ;
        String sortOrder = TIMESTAMP + " desc";
        Log.e("TAG_查询消息","selection"+selection);
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 查询所有消息 去除系统通知、评论、公告、待办、催办、日程创建、任务创建、工作汇报创建
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryLastMessage(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" +
                " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.SYSTEMNOTIFICATION +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.NOTICE +
                "' AND " + OPERATIONTYPE + " <> 'hasten'"

//                + " AND " + OPERATIONTYPE + " <> 'create'"

//                +" AND " + OPERATIONTYPE + " = '" + TypeUtils.OPERATION_COMMENT + "'"//回复
                + " AND (" + MESSAGE_TYPE + " = '" + TypeUtils.SCHEDULE + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.SCHEDULE
                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.APPROVAL_OVERTIME + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.APPROVAL_OVERTIME
                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.VANTOPLEAVE + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.VANTOPLEAVE

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.VANTOPSIGNCARD + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.VANTOPSIGNCARD

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.APPROVAL_FLOW + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.APPROVAL_FLOW

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.TSAK + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.TSAK

                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.WORKREPORT + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.WORKREPORT
                + "')"
                ;
        String sortOrder = TIMESTAMP + " desc limit 1 ";
        if (Constants.DEBUG)
        Log.e("TAG_查询消息最后一条","selection"+selection);
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 查询所有消息 去除系统通知、评论、公告、待办、催办、日程创建、任务创建、工作汇报创建
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryAllMessageUnRead(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD +
                "' AND (" + MESSAGE_TYPE + " <> '" + TypeUtils.SYSTEMNOTIFICATION +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.NOTICE +
                "' AND " + OPERATIONTYPE + " <> '" + "hasten" +
                "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_COMMENT +
                "' AND (" + MESSAGE_TYPE + " = '" + TypeUtils.SCHEDULE + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE +
                "') OR (" + MESSAGE_TYPE + " = '" + TypeUtils.TSAK + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE +
                "') OR (" + MESSAGE_TYPE + " = '" + TypeUtils.WORKREPORT + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE +
                "'))";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 查询所有消息 去除系统通知、评论、公告、待办、催办
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryPushTodoMessage(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND (" + MESSAGE_TYPE + " = '" + TypeUtils.VANTOPLEAVE +
                "' OR " + MESSAGE_TYPE + " ='" + TypeUtils.VANTOPOVERTIME +
                "' OR " + MESSAGE_TYPE + " ='" + TypeUtils.VANTOPSIGNCARD +
                "') AND " + OPERATIONTYPE + " = '" + "hasten" + "'";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 查询公告推送消息
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryNotice(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_TYPE + " = '" + TypeUtils.NOTICE + "'";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 查询公告推送消息
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryNoticeUnRead(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD +
                "' AND " + MESSAGE_TYPE + " = '" + TypeUtils.NOTICE + "'";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 查询通知推送消息
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryMyNoticeUnRead(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD +
                "' AND " + MESSAGE_TYPE + " = '" + TypeUtils.MYNOTICE + "'";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 根据TypeID查询消息（去除回复）
     *
     * @param context
     * @param typeId  业务类型ID
     * @return
     */
    public static ArrayList<MessageDB> queryAllMessageByTypeId(Context context, String typeId) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_TYPE + " = '" + typeId +
                "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_COMMENT + "'";
        // String selection = MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD + "'" + " AND " + USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId + "'";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 查询所有评论
     *
     * @param context
     * @return
     */
    public static ArrayList<MessageDB> queryAllCommonMessage(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + OPERATIONTYPE + " = '" + TypeUtils.OPERATION_COMMENT + "'";
        String sortOrder = TIMESTAMP + " desc";
        Log.e("TAG_查询评论","selection"+selection);
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    public static List<MessageDB> queryMessageByKeyword(Context context, String keyword) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);

        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.SYSTEMNOTIFICATION +
                "' AND " + MESSAGE_TYPE + " <> '" + TypeUtils.NOTICE +
                "' AND " + OPERATIONTYPE + " <> '" + "hasten" +
                "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_COMMENT +
                "' AND (" + MESSAGE_TYPE + " = '" + TypeUtils.SCHEDULE + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.SCHEDULE
                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.TSAK + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.TSAK
                + "') AND (" + MESSAGE_TYPE + " = '" + TypeUtils.WORKREPORT + "' AND " + OPERATIONTYPE + " <> '" + TypeUtils.OPERATION_CREATE + "' OR " + MESSAGE_TYPE + "<>'" + TypeUtils.WORKREPORT
                + "')"
                + " AND " + MESSAGE_TITLE + " like '%" + keyword + "%'"
                ;
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }

    /**
     * 删除消息 by 消息类型
     *
     * @param context
     * @param type
     */
    public void deleteAll(Context context, int type) {
        Uri uri = getContentUri(getClass(), context);
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = MESSAGE_TYPE + " = '" + type + "'" + " AND " + USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId + "'";
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, selection, null);
    }

    protected ContentValues putValues() {
        ContentValues values = new ContentValues();
        values.put(MESSAGE_TYPE, type);
        values.put(OPERATIONTYPE, operationType);
        values.put(MESSAGE_TITLE, title);
        values.put(MESSAGE_CONTENT, content);
        values.put(MESSAGE_STATE, messageState);
        values.put(USERID, PrfUtils.getUserId(mContext));
        values.put(TENANTID, PrfUtils.getTenantId(mContext));
        values.put(TIMESTAMP, System.currentTimeMillis());
        return values;
    }

    private static MessageDB setValues(Cursor cursor, MessageDB account) {
        account._id = cursor.getInt(cursor.getColumnIndex(_ID));
        account.type = cursor.getString(cursor.getColumnIndex(MESSAGE_TYPE));
        account.operationType = cursor.getString(cursor.getColumnIndex(OPERATIONTYPE));
        account.title = cursor.getString(cursor.getColumnIndex(MESSAGE_TITLE));
        account.content = cursor.getString(cursor.getColumnIndex(MESSAGE_CONTENT));
        account.messageState = cursor.getInt(cursor.getColumnIndex(MESSAGE_STATE));
        account.userId = cursor.getString(cursor.getColumnIndex(USERID));
        account.tenantId = cursor.getString(cursor.getColumnIndex(TENANTID));
        account.timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
        return account;
    }

    /**
     * 获取最后一条未读评论
     *
     * @param context
     * @return
     */
    public static List<MessageDB> getFirstComment(Context context) {
        Uri uri = getContentUri(MessageDB.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId +
                "' AND " + MESSAGE_STATE + " = '" + MESSAGE_STATE_UNREAD +
                "' AND " + OPERATIONTYPE + " = '" + TypeUtils.OPERATION_COMMENT + "'";
        String sortOrder = TIMESTAMP + " desc limit 1 ";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<MessageDB> messageList = new ArrayList<MessageDB>();
        while (cursor.moveToNext()) {
            MessageDB messageDB = new MessageDB();
            setValues(cursor, messageDB);
            messageList.add(messageDB);
        }
        cursor.close();
        return messageList;
    }


    /**
     * 改变已读状态
     *
     * @param context
     */
    public void changeReadState(Context context) {
        Log.e("TAG_消息已读","_id="+_id);
        mContext = context;
        if (_id == -1) {
            return;
        }
        Uri uri = ContentUris.withAppendedId(
                getContentUri(getClass(), context), _id);
        ContentResolver resolver = context.getContentResolver();
        messageState = MESSAGE_STATE_READ;
        ContentValues values = putValues();
        resolver.update(uri, values, null, null);
    }

    /**
     * 删除记录
     *
     * @param context
     */
    public void deleteThis(Context context) {
        Log.e("TAG_消息删除","_id="+_id);
        mContext = context;
        if (_id == -1) {
            return;
        }
        Uri uri = ContentUris.withAppendedId(
                getContentUri(getClass(), context), _id);
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, null, null);
    }

    /**
     * 根据处理类型删除
     *
     * @param context
     * @param operationType
     */
    public void deleteByOperationType(Context context, String operationType) {

        Uri uri = getContentUri(getClass(), context);
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = OPERATIONTYPE + " = '" + operationType + "'" + " AND " + USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId + "'";
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, selection, null);
    }

    @Override
    public String toString() {
        return "{" +
                "\"type\":" + type  +
                ", \"operationType\":" + "\""+operationType +"\""+
                ", \"title\":"   + "\""+title +"\""+
                ",\" content\":" + content  +
                ", \"messageState\":" + messageState +
                '}';
    }
}
