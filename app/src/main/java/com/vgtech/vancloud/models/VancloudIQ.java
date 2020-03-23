package com.vgtech.vancloud.models;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by adm01 on 2016/7/4.
 */
public class VancloudIQ extends IQ {
    private static final String DEFAULT_QUALIFIEDNAME = "vancloud";
    private static final String DEFAULT_NAMESPACEURI = "jabber:iq:receipt";


    private String type;

    public VancloudIQ(String from, String id, String type) {
        this.type = type;
        setType(Type.SET);
        setPacketID(id);
        setFrom(from);
    }

    public String getChildElementXML(){
        String xml = "<vancloud xmlns=\"jabber:iq:receipt\" type=\""+type+"\"/>";
        return xml;

    }


}
