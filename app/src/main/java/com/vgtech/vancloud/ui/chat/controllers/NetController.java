package com.vgtech.vancloud.ui.chat.controllers;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.util.Log;

import com.vgtech.vancloud.models.ChatGroupStaffs;
import com.vgtech.vancloud.models.ChatGroupStaffsPartent;
import com.vgtech.vancloud.models.Entity;
import com.vgtech.vancloud.models.GroupListInfo;
import com.vgtech.vancloud.models.ListEntity;
import com.vgtech.vancloud.models.OrgData;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.models.StaffListInfo;
import com.vgtech.common.api.UserAccount;
import com.vgtech.vancloud.models.Vacation;
import com.vgtech.vancloud.models.Workgroup;
import com.vgtech.vancloud.models.Workgroups;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.util.Ln;
import roboguice.util.Strings;

import static com.vgtech.vancloud.models.ListEntity.StaffListEntity;
import static com.vgtech.vancloud.models.ListEntity.VacationListEntity;

/**
 * @author xuanqiang
 */
@SuppressWarnings("ALL")
public class NetController extends NetConnection {
    private PreferencesController prefController;

    public NetController(ConnectivityManager connectivityManager, PreferencesController prefController, RestTemplate restTemplate) {
        super(connectivityManager, restTemplate);
        this.prefController = prefController;
    }

    String getUrl(final String uri) {
        return prefController.getAccount().getUrl(uri);
    }

    void setHeaders(final HttpHeaders headers) {
        UserAccount acc = prefController.getAccount();
//    headers.add("Code", acc.customerCode);
//    headers.add("Lang",acc.getLanguage());
    }

    public UserAccount login(final String logname, final String pwd) {
        return post("login?login={logname}&password={pwd}", UserAccount.class, logname, pwd);
    }

    public Entity changePwd(final String originPwd, final String pwd) {
        return post("users/pwd?origin_password={origin}&new_password={pwd}", Entity.class, originPwd, pwd);
    }

    public Map profile(final String uid) {
        return get("users/show?staff_no={staff_no}", Map.class, uid);
    }

    public Map profileEdit() {
        return get("users-edit", Map.class);
    }

    public Map pwdVerify(final String pwd) {
        return post("users/pwd-verify?password={password}", Map.class, pwd);
    }

    public Map modifyProfile(final MultiValueMap<String, String> formData) {
        return postForm("users-edit", formData);
    }

    public Map uploadAvatar(final Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        Resource photoRes = new ByteArrayResource(baos.toByteArray()) {
            @Override
            public String getFilename() throws IllegalStateException {
                return System.currentTimeMillis() + ".jpg";
            }
        };
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        formData.add("avatar", photoRes);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
        return post("users/avatar", requestEntity, Map.class);
    }

    public StaffListInfo groupStaffs(final String gid) {
        return get("group-staffs?gId={gid}", StaffListInfo.class, gid);
    }

    public Map groupStaffsAdd(final String gid, final List<String> staffIds) {
        MultiValueMap<String, String> formMap = new LinkedMultiValueMap<String, String>(0);
        formMap.add("gId", gid);
        formMap.put("staffNos[]", staffIds);
        return postForm("group-staffs", formMap);
    }

    public Map groupStaffsDel(final String gid, final List<String> staffIds) {
        MultiValueMap<String, String> formMap = new LinkedMultiValueMap<String, String>(0);
        formMap.add("gId", gid);
        formMap.put("staffNos[]", staffIds);
        return postForm("group-staffs/destroy", formMap);
    }

    public Workgroups groups() {
        return get("groups", Workgroups.class);
    }

    public Workgroup groupAdd(final String name) {
        return post("groups?groupName={name}", Workgroup.class, name);
    }

    public Workgroup groupModify(final String id, final String name) {
        return post("groups/name?gId={gid}&groupName={name}", Workgroup.class, id, name);
    }

    public Entity groupDel(final String gid) {
        return post("groups/destroy?gId={gid}", Entity.class, gid);
    }

    public Map salaryYear() {
        return get("salaries/dates", Map.class);
    }

    public Map salaryProjectNameYear() {
        return get("salaries/items", Map.class);
    }

    public Map salary(final String date, final String password) {
        return get("salaries?date={date}&password={password}", Map.class, date, password);
    }

    public Map salaryProject(final String year, final String password) {
        return get("salaries/items-year-prices?y={year}&password={password}", Map.class, year, password);
    }

    public OrgData organList(final String level, final String code, final String pcodes) {
        return get("orgs?level={level}&code={code}&pcodes={pcodes}", OrgData.class, level, code, pcodes);
    }

    public OrgData organStaffs(final String code, final String pcodes, final String lastStaffNo) {
        return get("orgs/staffs?code={code}&pcodes={pcodes}&lastStaffNo={lastStaffNo}", OrgData.class, code, pcodes, lastStaffNo);
    }

    public OrgData orgSearch(final String search) {
        return get("orgs/search?q={search}", OrgData.class, search);
    }

    public ListEntity<Vacation> vacations() {
        return get("vacations", VacationListEntity.class);
    }

    public Map vacationBalance(final String code) {
        return get("vacations/balances?code={code}", Map.class, code);
    }

    public Map vacationBalanceAdjust(final String code) {
        return get("vacations-by-code/adjusts?code={code}", Map.class, code);
    }

    public Map balanceUse(final String code) {
        return get("vacations-by-code/uses?code={code}", Map.class, code);
    }

    public Map balanceApplies(final String code, final String status) {
        return get("vacations-by-code/applies?code={code}&status={status}", Map.class, code, status);
    }

    public Map applies(final String lastId, final String code) {
        return get("vacations/applies?lastTaskId={lastId}&code={code}", Map.class, lastId, code);
    }

    public Map vacationTypes() {
        return get("vacations/codes", Map.class);
    }

    public Map applyDetails(final String taskId) {
        return get("vacations/apply?taskId={taskId}", Map.class, taskId);
    }

    public Entity vacationApplyDelete(final String taskId) {
        return post("vacations/destroy?taskId={taskId}", Entity.class, taskId);
    }

    public ListEntity<Staff> contacts() {
        return get("users/contacts", StaffListEntity.class);
    }

    public Entity roomOwner(final String room, String ownid, String tenantid,String token) {
        return post("vchat/xmpp/mucowner?room={room}&ownid={ownid}&tenantid={tenantid}&token={token}", Entity.class, room,ownid,tenantid,token);
    }

    public Map uploadMessageFile(final ChatGroup group, final File file) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
//    formData.add(ChatGroup.GroupTypeGroup.equals(group.type) ? "to_group" : "to_user", group.name);
        formData.add("file", new FileSystemResource(file));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
        return post("xmpp/file", requestEntity, Map.class);
    }

//  public ChatGroupStaffs chatGroupStaffs(final String groupName){
//    return get("xmpp/group-members?room={groupName}", ChatGroupStaffs.class, groupName);
//  }

    public ChatGroupStaffs chatGroupStaffssss(final String groupName, String ownid, String tenantid,String token) {
//        return post("vchat/xmpp/groupmembers?room={groupName}", ChatGroupStaffs.class, groupName);
        Log.e("ceshiliaotian", groupName);
        return post("vchat/xmpp/groupmembers?room={groupName}&ownid={ownid}&tenantid={tenantid}&token={token}", ChatGroupStaffs.class, groupName, ownid, tenantid,token);
    }

    public ChatGroupStaffsPartent chatGroupStaffsPartent(final String groupName, String ownid, String tenantid,String token) {
//        return post("vchat/xmpp/groupmembers?room={groupName}", ChatGroupStaffs.class, groupName);
        Log.e("ceshiliaotian", groupName);
        return post("vchat/xmpp/groupmembers?room={groupName}&ownid={ownid}&tenantid={tenantid}&token={token}", ChatGroupStaffsPartent.class, groupName, ownid, tenantid,token);
    }

    public GroupListInfo chatGroups() {
        return get("xmpp/roomlist", GroupListInfo.class);
    }

    public Map push(final boolean cancel, final String clientId) {
        if (cancel) {
            return post("pns/destroy", Map.class);
        }
        return post("pns?deviceType=android&clientId={clientId}", Map.class, clientId);
    }

    public Map applyNew(final String code) {
        return get("applies/new?code={code}", Map.class, code);
    }

    public ListEntity<Staff> searchStaffs(final String lastStaffNo, final String q) {
        return get("users/search?lastStaffNo={lastStaffNo}&q={q}", StaffListEntity.class, lastStaffNo, q);
    }

    public Entity apply(final String taskId, final String code, final String unit, final String startDate, final String startTime,
                        final String endDate, final String endTime, final String duration, final String supervisor,
                        final String remark, final String cc, final Bitmap pic) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        if (Strings.notEmpty(taskId)) {
            formData.set("taskId", taskId);
        }
        formData.set("code", code);
        formData.set("unit", unit);
        formData.set("startDate", startDate);
        formData.set("startTime", startTime);
        formData.set("endDate", endDate);
        formData.set("endTime", endTime);
        formData.set("duration", duration);
        formData.set("supervisor", supervisor);
        try {
            formData.set("remark", URLEncoder.encode(remark, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            Ln.e(e);
        }
        formData.set("cc", cc);
        if (pic != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pic.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            Resource photoRes = new ByteArrayResource(baos.toByteArray()) {
                @Override
                public String getFilename() throws IllegalStateException {
                    return System.currentTimeMillis() + ".jpg";
                }
            };
            formData.add("pic", photoRes);
        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
        return post("applies?", requestEntity, Entity.class);
    }

    public Map approvalPerm() {
        return get("approvals/num", Map.class);
    }

    public Map appliesDuration(final String code, final String startDate, final String startTime, final String endDate, final String endTime) {
        return get("applies/duration?code={code}&startDate={startDate}&startTime={startTime}&endDate={endDate}&endTime={endTime}", Map.class, code, startDate, startTime, endDate, endTime);
    }

    public Map approvals(final boolean approved, final String lastTaskId, final String lastStaffNo) {
        String status = approved ? "1" : "0";
        return get("approvals?status={status}&lastTaskId={lastTaskId}&lastStaffNo={lastStaffNo}", Map.class, status, lastTaskId, lastStaffNo);
    }

    public Map approvalDetails(final String taskId, final String staffNo) {
        return get("approvals/show?taskId={taskId}&staffNo={staffNo}", Map.class, taskId, staffNo);
    }

    public Entity approval(final String taskId, final String remark, final String staffNo, final boolean isAgree) {
        String status = isAgree ? "Y" : "N";
        return post("approvals?taskId={taskId}&remark={remark}&staffNo={staffNo}&status={status}", Entity.class, taskId, remark, staffNo, status);
    }

    public Map attendanceNew() {
        return get("attendances/new", Map.class);
    }

    public Entity attendanceSignIn(final String cardNo, final String termNo, final String longitude, final String latitude, final String address) {
        return post("attendances?cardNo={cardNo}&termNo={termNo}&longitude={longitude}&latitude={latitude}&address={address}", Entity.class, Strings.toString(cardNo), Strings.toString(termNo), longitude, latitude, address);
    }

    public Map attendances(final String startDate, final String endDate) {
        return get("attendances?startDate={startDate}&endDate={endDate}", Map.class, startDate, endDate);
    }


    public Entity feedback(final String content) {
        return post("account/advise&adv={adv}", Entity.class, content);
    }

    private Map convert(final Object obj) {
        Map map;
        if (obj instanceof Map) {
            map = (Map) obj;
        } else {
            Map<String, Object> dataMap = new HashMap<String, Object>(1);
            dataMap.put("data", obj);
            map = dataMap;
        }
        return map;
    }

    public Map weather(final String city) {
        return get("account/weather&city={city}", Map.class, city);
    }

    public Entity signIn(final double longitude, final double latitude, final String address) {
        return post("signin/signin&lon={longitude}&lat={latitude}&location={address}", Entity.class, longitude, latitude, address);
    }

    public Map signs(final String ym) {
        return get("signin/month&month={ym}", Map.class, ym);
    }

    public Map signsDay(final String ymd) {
        return get("signin/day&date={ymd}", Map.class, ymd);
    }

    public Map signDelete(final String date, final String time) {
        return post("signin/delsignin&date={date}&time={time}", Map.class, date, time);
    }

    public Map balanceApproving(final String leaId, final String fromDate, final String toDate) {
        return get("lea/approving&lea_id={leadId}&from_date={fromDate}&to_date={toDate}", Map.class, leaId, fromDate, toDate);
    }

    public Map vacationCalendar(final String month) {
        return get("lea/calendar&month={month}", Map.class, month);
    }

    public Map vacationApplying(final String leaId, final String period) {
        return get("lea/apply&lea_id={leaId}&period={period}", Map.class, leaId, period);
    }

    public Entity vacationApply(final String leaId, final String period, final String remark, final String approvalStaffId, final List<Bitmap> pics) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        formData.set("lea_id", leaId);
        formData.set("period", period);
//    formData.set("remark", remark);
        formData.set("approval", approvalStaffId);
        if (!CollectionUtils.isEmpty(pics)) {
            for (Bitmap pic : pics) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                pic.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                Resource photoRes = new ByteArrayResource(baos.toByteArray()) {
                    @Override
                    public String getFilename() throws IllegalStateException {
                        return System.currentTimeMillis() + ".jpg";
                    }
                };
                formData.add("pic[]", photoRes);
            }
        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
        return post("lea/doapply&remark={remark}", requestEntity, Entity.class, remark);
    }

    public Map vacationApplyChanging(final String taskId, final String period) {
        return get("lea/mod&task_id={taskId}&period={period}", Map.class, taskId, period);
    }

    public Entity vacationApplyChange(final String leaId, final String taskId, final String period, final String remark, final List<Bitmap> pics) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        formData.set("lea_id", leaId);
        formData.set("task_id", taskId);
        formData.set("period", period);
//    formData.set("remark", remark);
        if (!CollectionUtils.isEmpty(pics)) {
            for (Bitmap pic : pics) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                pic.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                Resource photoRes = new ByteArrayResource(baos.toByteArray()) {
                    @Override
                    public String getFilename() throws IllegalStateException {
                        return System.currentTimeMillis() + ".jpg";
                    }
                };
                formData.add("pic[]", photoRes);
            }
        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
        return post("lea/domod&remark={remark}", requestEntity, Entity.class, remark);
    }

    public Map ver() {
        return get("ver", Map.class);
    }

}
