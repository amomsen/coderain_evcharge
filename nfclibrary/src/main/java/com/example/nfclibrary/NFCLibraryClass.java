package com.example.nfclibrary;


import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class  NFCLibraryClass implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter = null;

    public void enableReader() {

        //nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }


    @Override
    public void onTagDiscovered(Tag tag) {

        Log.i("onTagDiscovered : ", "start");

        IsoDep isoDep = IsoDep.get(tag);

        if (isoDep != null) {
            try {
                isoDep.connect();

                byte[] result = isoDep.transceive(HexStringToByteArray("00A4040007FF00010001000008"));

                String nfcResult = ByteArrayToHexString(result);

                //SetMyText("onTagDiscovered result " + nfcResult);

                Log.i("HannesnfcResult : ", nfcResult);

            } catch (IOException ex) {
                //SetMyText("onTagDiscovered error : " + ex.toString());
                //NFCResult = "Result Error : " + ex.toString();
                Log.i("HannesnfcError : ", ex.toString());
            }
        }
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
