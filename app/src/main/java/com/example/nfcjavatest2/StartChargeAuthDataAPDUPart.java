package com.example.nfcjavatest2;

public class StartChargeAuthDataAPDUPart {

    public String LengthInitVector;
    public String InitVector;
    public String ChargerId;
    public String Reserved1;
    public String UserId;
    public String PhoneTime;
    public String ChargeTime;
    public String KWH;
    public String CurrentTableIndex;
    public String Reserved2;
    public String TransId;
    public String TagICV;

    public String GetStartChargeAuthDataAPDUPart(){

        return  LengthInitVector + InitVector + ChargerId + Reserved1 + UserId + PhoneTime + ChargeTime + KWH +
                CurrentTableIndex + Reserved2 + TransId + TagICV;
    }
}
