package com.example.nfcjavatest2;

public class APDU {

    public String CLA;
    public String INS;
    public String P1;
    public String P2;
    public String LC;
    public String Data;
    public String LE;

    public String GetApdu(){

       return  CLA + INS + P1 + P2 + LC + Data + LE;

    }
}

