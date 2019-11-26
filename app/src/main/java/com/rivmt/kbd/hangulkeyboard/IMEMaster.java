package com.rivmt.kbd.hangulkeyboard;

public abstract class IMEMaster {

    public IMEInfoHandler mInfo = new IMEInfoHandler();
    public char mBlankWord = '0';

    public abstract boolean checkCode(int i);

    public abstract void createLetter(int i, boolean cont);

    public abstract void deleteOrder();
}
