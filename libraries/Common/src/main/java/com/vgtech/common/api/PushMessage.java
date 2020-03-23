package com.vgtech.common.api;

/**
 * Created by zhangshaofang on 2015/10/23.
 */
public class PushMessage extends AbsApiData {
    public String resId;
    public String title;
    public String content;
    public String msgTypeId;//推送类型:11-任务，9-日程，2-流程，5-帮帮，3-分享，1-公告，7-工作汇报，22-离职，24-组织机构发生变化，25-角色发生变化，27-简历，29-视频会议邀请，28-视频会议预约创建，修改，取消，16-个人版订阅职位推送，31-背景调查。
    public String msgTypeInfo;
    public String operationType;//处理类型：dig-点赞，comment-评论，processing-处理，update-修改，cancel-取消,hasten 催办,exam//审批通过,chexiao撤銷，create新建代办
    public String role;//角色类型：create-创建人，cc-抄送人，process-执行人


    public String tenantId;
    public String type;
    public String groupId;
    public String groupName;

    public String recruitInfoId;//招聘计划Id

    public String zoomId;
    public String userId;
    public String logo;
    public String userName;

    @Override
    public String toString() {
        return "PushMessage{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", msgTypeId='" + msgTypeId + '\'' +
                ", msgTypeInfo='" + msgTypeInfo + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", type='" + type + '\'' +
                ", operationType='" + operationType + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
