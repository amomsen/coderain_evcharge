package com.example.nfcjavatest2;

public class EndChargeSelectDataAPDUPart {

    public String Category;
    public String AID;
    public String Version;
    public String Reserved1;

    public String GetEndChargeSelectDataAPDUPart() {

        return Category + AID + Version + Reserved1;
    }

}
