package com.vgtech.common.api;

/**
 * Data:  2018/7/6
 * Auther: 陈占洋
 * Description:成本中心
 */

public class CostCenter extends AbsApiData {

    /**
     * dictionary_name_chn : 人力资源部
     * dictionary_code : 31
     */

    private String dictionary_name_chn;
    private String dictionary_code;

    public String getDictionary_name_chn() {
        return dictionary_name_chn;
    }

    public void setDictionary_name_chn(String dictionary_name_chn) {
        this.dictionary_name_chn = dictionary_name_chn;
    }

    public String getDictionary_code() {
        return dictionary_code;
    }

    public void setDictionary_code(String dictionary_code) {
        this.dictionary_code = dictionary_code;
    }
}
