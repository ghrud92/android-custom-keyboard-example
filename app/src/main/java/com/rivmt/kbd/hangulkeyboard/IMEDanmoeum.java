package com.rivmt.kbd.hangulkeyboard;

import android.util.Log;

public class IMEDanmoeum extends IMEMaster {

    private int[] mLetters = {-1,0,0};
    public short mLetterCursor = 0;
    public char mBlankWord = '0';

    public IMEDanmoeum() {

    }

    @Override
    public void deleteOrder() {
        resetLetter();
    }

    @Override
    public boolean checkCode(int i) {
        return (i >= 12593 && i <= 12643);
    }

    @Override
    public void createLetter(int inputCode, boolean cont) {
        //Check timer
        if (!cont) {
            Log.i("Hangul", "Timer Already End");
            if (inputCode >= 12623) {//Moeum
                Log.i("Hangul", "Input is Moeum");
                if (mLetterCursor == 1) {
                    Log.i("Hangul", "Cursor Pos is Jungseong");
                    //mInfo.mDeleteLetters=1;
                    mLetters[mLetterCursor]=inputCode;
                    mInfo.deleteOneLetter();
                    mInfo.mCreateLetters.append((char) createHangul());
                    mLetterCursor++;
                } else {
                    Log.i("Hangul", "Cursor Pos is not Jungseong");
                    //Log.i("Hangul", "Input Single Letter: " + (char) inputCode);
                    mInfo.deleteOneLetter();
                    int aaa=mLetters[2];
                    mLetters[2]=0;
                    mInfo.mCreateLetters.append((char) createHangul());
                    resetLetter();
                    mLetters[0]=aaa;
                    mLetters[1]=inputCode;
                    mLetterCursor=2;
                    mInfo.mCreateLetters.append((char) createHangul());
                }
            } else {
                Log.i("Hangul", "Input is Jaeum");
                if (mLetterCursor == 0) {
                    resetLetter();
                    mLetters[0]=inputCode;
                    mInfo.mCreateLetters.append((char) createHangul());
                    mLetterCursor++;
                } else if (mLetterCursor == 1) {
                    Log.i("Hangul", "Cursor Pos is Jungseong");
                    resetLetter();
                    mLetters[1]=inputCode;
                    mInfo.mCreateLetters.append((char) createHangul());
                    mLetterCursor=2;
                } else  {
                    Log.i("Hangul","Cursor pos is jongseong");
                    mLetters[mLetterCursor]=inputCode;
                    mInfo.deleteOneLetter();
                    mInfo.mCreateLetters.append((char) createHangul());
                    //resetLetter();
                }

            }
        } else {
            Log.i("Hangul", "Timer Yet End");
            int preCursor = setCursorPos(mLetterCursor - 1);
            int[] t;
            if (inputCode >= 12623) {
                Log.i("Hangul", "Input is Moeum");
                if (preCursor == 1) {
                    Log.i("Hangul", "Pre-Cursor Pos is Jungseong");
                    t = mergeMoeum(mLetters[preCursor], inputCode);
                    if (t[0] == 4) {//Merged
                        Log.i("Hangul", "Letter Merged and fit to pos");
                        mLetterCursor=1;
                        mLetters[mLetterCursor]=t[1];
                        mInfo.deleteOneLetter();
                        mInfo.mCreateLetters.append((char) createHangul());
                        mLetterCursor=2;
                    } else {//Cannot Merge
                        Log.i("Hangul", "Letter cannot merged");
                        resetLetter();
                        Log.i("Hangul", "Input single Letter: " + (char) inputCode);
                        mInfo.mCreateLetters.append((char) inputCode);
                    }
                } else if (preCursor == 0) {
                    Log.i("Hangul", "Pre-cursor pos is choseong");
                    mLetters[1]=inputCode;
                    mInfo.deleteOneLetter();
                    mInfo.mCreateLetters.append((char) createHangul());
                    mLetterCursor++;
                } else if (preCursor == 2) {
                    Log.i("Hangul", "Pre-cursor pos is jongseong");
                    mInfo.deleteOneLetter();
                    int aaa = mLetters[2];
                    mLetters[2]=0;
                    mInfo.mCreateLetters.append((char) createHangul());
                    resetLetter();
                    mLetters[0] = aaa;
                    Log.i("Hangul","Immigrated: "+aaa);
                    mLetters[1]=inputCode;
                    mInfo.mCreateLetters.append((char) createHangul());
                    mLetterCursor=2;
                    Log.i("Hangul", "Move jongseong to choseong and insert moeum: " + (char) inputCode);

                }
            } else {
                Log.i("Hangul", "Input is Jaeum");
                if (preCursor == 1) {
                    Log.i("Hangul", "Pre-cursor pos is jungseong");
                    mLetters[2]=inputCode;
                    mInfo.deleteOneLetter();
                    mInfo.mCreateLetters.append((char) createHangul());
                    mLetterCursor=0;
                } else if (preCursor == 0) {
                    Log.i("Hangul", "Pre-cursor pos is choseong");
                    t = mergeJaeum(mLetters[preCursor], inputCode);
                    if (t[0] == 1 || t[0] == 2) {//Merge
                        Log.i("Hangul", "Letter Merged and fit to pos");
                        mLetters[0]=t[1];
                        mInfo.deleteOneLetter();
                        mInfo.mCreateLetters.append((char) createHangul());
                        mLetterCursor=1;
                    } else {//Cannot Merge
                        Log.i("Hangul", "Letter cannot merged");
                        resetLetter();
                        mLetters[0]=inputCode;
                        mInfo.mCreateLetters.append((char) createHangul());
                    }
                } else if (preCursor == 2) {
                    Log.i("Hangul", "Pre-cursor pos is jongseong");
                    t = mergeJaeum(mLetters[preCursor], inputCode);
                    if (t[0] == 2 || t[0] == 3) {//Merge
                        Log.i("Hangul", "Letter merged and fit to pos");
                        mLetters[2]=t[1];
                        mInfo.deleteOneLetter();
                        mInfo.mCreateLetters.append((char) createHangul());
                        resetLetter();
                    } else {//Cannot Merge
                        Log.i("Hangul", "Letter cannot merged");
                        resetLetter();
                        mLetters[0]=inputCode;
                        mInfo.mCreateLetters.append((char) createHangul());
                        mLetterCursor=1;

                    }
                }
            }
        }
    }

    private void inputLetter(int i) {
        mInfo.mCreateLetters.append((char) i);
    }

    private int[] mergeJaeum(int a, int b) {
        Log.i("Hangul","Merge Jaeum ["+a+","+b+"]");
        int [] i={0,0};
        //i[0] 0:None, 1:Choseong, 2:Choseong and Jongseong, 3:Jongseong, 4:Jungseong
        switch(a) {
            case 12593://ㄱ
                switch(b) {
                    case 12593://ㄱ
                        i[0]=2;
                        i[1]=12594;
                        break;
                    case 12613://ㅅ
                        i[0]=3;
                        i[1]=12595;
                        break;
                }
                break;
            case 12596://ㄴ
                switch(b) {
                    case 12616://ㅈ
                        i[0]=3;
                        i[1]=12597;
                        break;
                    case 12622://ㅎ
                        i[0]=3;
                        i[1]=12598;
                        break;
                }
                break;
            case 12599://ㄷ
                if (b == 12599) {
                    i[0]=1;
                    i[1]=12600;
                }
                break;
            case 12601://ㄹ
                switch(b) {
                    case 12593://ㄱ
                        i[0]=3;
                        i[1]=12602;
                        break;
                    case 12609://ㅁ
                        i[0]=3;
                        i[1]=12603;
                        break;
                    case 12610://ㅂ
                        i[0]=3;
                        i[1]=12604;
                        break;
                    case 12613://ㅅ
                        i[0]=3;
                        i[1]=12605;
                        break;
                    case 12620://ㅌ
                        i[0]=3;
                        i[1]=12606;
                        break;
                    case 12621://ㅍ
                        i[0]=3;
                        i[1]=12607;
                        break;
                    case 12622://ㅎ
                        i[0]=3;
                        i[1]=12608;
                        break;
                }
                break;
            case 12610://ㅂ
                switch(b) {
                    case 12610://ㅂ
                        i[0]=2;
                        i[1]=12611;
                        break;
                    case 12613://ㅅ
                        i[0]=3;
                        i[1]=12612;
                        break;
                }
                break;
            case 12613://ㅅ
                if (b == 12613) {
                    i[0]=2;
                    i[1]=12614;
                }
                break;
            case 12616://ㅈ
                if (b == 12616) {
                    i[0]=1;
                    i[1]=12617;
                }
                break;
        }
        Log.i("Hangul","Type "+i[0]+" Text: "+(char) i[1] );
        return i;
    }
    private int[] mergeMoeum(int a, int b) {
        Log.i("Hangul","Merge Moeum ["+a+","+b+"]");
        int [] i={0,0};
        //i[0] 0:None, 1:Choseong, 2:Choseong and Jongseong, 3:Jongseong, 4:Jungseong
        switch(a) {
            case 12623://ㅏ
                if (b == 12623) {
                    i[0]=4;
                    i[1]=12625;
                }
                break;
            case 12627://ㅓ
                if (b == 12627) {
                    i[0]=4;
                    i[1]=12629;
                }
                break;
            case 12631://ㅗ
                switch(b) {
                    case 12623://ㅘ
                        i[0]=4;
                        i[1]=12632;
                        break;
                    case 12624://ㅙ
                        i[0]=4;
                        i[1]=12633;
                        break;
                    case 12631://ㅛ
                        i[0]=4;
                        i[1]=12635;
                        break;
                    case 12643://ㅚ
                        i[0]=4;
                        i[1]=12634;
                        break;
                }
                break;
            case 12636://ㅜ
                switch(b) {
                    case 12627://ㅝ
                        i[0]=4;
                        i[1]=12637;
                        break;
                    case 12628://ㅞ
                        i[0]=4;
                        i[1]=12638;
                        break;
                    case 12636://ㅠ
                        i[0]=4;
                        i[1]=12640;
                        break;
                    case 12643://ㅟ
                        i[0]=4;
                        i[1]=12639;
                        break;
                }
                break;
            case 12641://ㅡ
                if (b == 12643) {
                    i[0]=4;
                    i[1]=12642;
                }
                break;
        }
        Log.i("Hangul","Type "+i[0]+" Text: "+(char) i[1] );
        return i;
    }
    private int getChoseongCode(int i) {
        switch(i) {
            case 12593:
                return 0;
            case 12594:
                return 1;
            case 12596:
                return 2;
            case 12599:
                return 3;
            case 12600:
                return 4;
            case 12601:
                return 5;
            case 12609:
                return 6;
            case 12610:
                return 7;
            case 12611:
                return 8;
            case 12613:
                return 9;
            case 12614:
                return 10;
            case 12615:
                return 11;
            case 12616:
                return 12;
            case 12617:
                return 13;
            case 12618:
                return 14;
            case 12619:
                return 15;
            case 12620:
                return 16;
            case 12621:
                return 17;
            case 12622:
                return 18;

        }
        return 0;
    }
    private int getJongseongCode(int i) {
        switch(i) {
            case 0://Null
                return 0;
            case 12593:
                return 1;
            case 12594:
                return 2;
            case 12595:
                return 3;
            case 12596:
                return 4;
            case 12597:
                return 5;
            case 12598:
                return 6;
            case 12599:
                return 7;
            case 12601:
                return 8;
            case 12602:
                return 9;
            case 12603:
                return 10;
            case 12604:
                return 11;
            case 12605:
                return 12;
            case 12606:
                return 13;
            case 12607:
                return 14;
            case 12608:
                return 15;
            case 12609:
                return 16;
            case 12610:
                return 17;
            case 12612:
                return 18;
            case 12613:
                return 19;
            case 12614:
                return 20;
            case 12615:
                return 21;
            case 12616:
                return 22;
            case 12618:
                return 23;
            case 12619:
                return 24;
            case 12620:
                return 25;
            case 12621:
                return 26;
            case 12622:
                return 27;

        }
        return 0;
    }
    private int getJongseongUniCode(int i) {
        switch(i) {
            case 0://Null
                return 0;
            case 1:
                return 12593;
            case 2:
                return 12594;
            case 3:
                return 12595;
            case 4:
                return 12596;
            case 5:
                return 12597;
            case 6:
                return 12598;
            case 7:
                return 12599;
            case 8:
                return 12601;
            case 9:
                return 12602;
            case 10:
                return 12603;
            case 11:
                return 12604;
            case 12:
                return 12605;
            case 13:
                return 12606;
            case 14:
                return 12607;
            case 15:
                return 12608;
            case 16:
                return 12609;
            case 17:
                return 12610;
            case 18:
                return 12612;
            case 19:
                return 12613;
            case 20:
                return 12614;
            case 21:
                return 12615;
            case 22:
                return 12616;
            case 23:
                return 12618;
            case 24:
                return 12619;
            case 25:
                return 12620;
            case 26:
                return 12621;
            case 27:
                return 12622;

        }
        return 0;
    }
    private int setCursorPos(int i) {
        if (i > 2) {
            i=0;
        } else if (i < 0) {
            i=2;
        }
        return i;
    }
    private int createHangul() {
        int a=44032+getChoseongCode(mLetters[0])*21*28+(mLetters[1]-12623)*28+ getJongseongCode(mLetters[2]);
        if (mLetters[0]==-1) {
            Log.i("Hangul","Invalid Type");
            return 48;
        }
        if (mLetters[1]==0) {
            return mLetters[0];
        }
        return a;
    }
    private void resetLetter() {
        Log.i("Hangul","Reset Data");
        mLetterCursor=0;
        mLetters[0]=-1;
        mLetters[1]=0;
        mLetters[2]=0;
    }
}
