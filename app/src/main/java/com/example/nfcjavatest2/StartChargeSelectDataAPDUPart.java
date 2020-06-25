package com.example.nfcjavatest2;

public class StartChargeSelectDataAPDUPart {

    public String Category;
    public String AID;
    public String Version;
    public String Reserved1;

    public String GetStartChargeSelectDataAPDUPart() {

        return Category + AID + Version + Reserved1;
    }

}

