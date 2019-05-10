package me.sherry.model;

import lombok.Data;

@Data
public class WXData {
    private String touser;
    private String chatid;
    private String toparty;
    private String msgtype;
    private int agentid;
    private Object text;
}
