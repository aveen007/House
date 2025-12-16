package com.medical.entity;

public enum AnalysisStatus {
    AwaitingHD(0),
    AwaitingPat(1),
    Rejected(2),
    Accepted(3),
    Finished(4);

    private final int code;

    AnalysisStatus(int code){
        this.code = code;

    }
    public int getCode(){
        return code;
    }
    public static AnalysisStatus fromCode(int code){
        for(var v : values()){
            if(v.code==code){
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown code: "+code);
    }
}
