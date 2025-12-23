package com.medical.entity;

public enum VisitHDStatus {
    Awaiting(0),
    Accepted(1),
    Rejected(2);

    private final int code;

    VisitHDStatus(int code){
        this.code = code;

    }
    public int getCode(){
        return code;
    }
    public static VisitHDStatus fromCode(int code){
        for(var v : values()){
            if(v.code==code){
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown code: "+code);
    }
}
