package com.vgtech.vancloud.models;

import java.io.Serializable;
import java.util.Map;

import roboguice.util.Strings;

/**
 * @author xuanqiang
 * @date 13-7-4
 */
public class ProfileField implements Serializable {
  private static final long serialVersionUID = -6695645937640566967L;

  public String id = "";
  public String name = "";
  public String content = "";
  public Boolean status = true;//false: 不使用 true: 使用
  public String type = "C";//C-字符，N-数字，D-日期, P-电话, E-邮箱, S-单选
  public Map<String,String> spec;

  public ProfilePickerItem selectedItem;

  public static ProfileField copy(final ProfileField field) {
    ProfileField f = new ProfileField();
    f.id = field.id;
    f.name = field.name;
    f.content = field.content;
    f.status = field.status;
    f.type = field.type;
    f.spec = field.spec;
    f.selectedItem = field.selectedItem;
    return f;
  }

  @SuppressWarnings("unchecked")
  public static ProfileField build(final Map map) {
    ProfileField field = new ProfileField();
    if(map != null){
      field.id = (String)map.get("name");
      field.name = (String)map.get("label");
      field.content = (String)map.get("value");
      field.status = (Boolean)map.get("status");
      field.type = (String)map.get("type");
      field.spec = (Map<String,String>)map.get("values");
      if(field.spec != null && Strings.notEmpty(field.content)) {
        field.selectedItem = new ProfilePickerItem(field.content, field.spec.get(field.content));
        field.content = field.selectedItem.content;
      }
    }
    return field;
  }

}
