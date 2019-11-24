package com.rivmt.kbd.hangulkeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import com.rivmt.kbd.hangulkeyboard.R;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private KeyboardView keyboardView;
    private Keyboard keyboard;

    private boolean isCaps = false;   // Caps Lock
    private short mTypeInput = 0; //0:단모음, 1:쿼티, 2:
    private int[] mLetters = {0,0,0};
    private short mLetterCursor = 0;
    private boolean mIntervalInput = false;
    private KeyTimer mKeyTimer = new KeyTimer(500, 100);

    @Override
    public View onCreateInputView() {
        //Timer Class
        mKeyTimer.start();


        //Keyboard View
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.keys_layout);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int i, int[] ints) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null)
            return;

        playClick(i);
        switch (i) {
            case Keyboard.KEYCODE_DELETE :
                deleteOneLetter();
                resetLetter();
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                keyboardView.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                confirmChar(i);
                Log.i("Hangul",(char) mLetters[0]+", "+(char) mLetters[1]+", "+(char) mLetters[2]);
        }
    }

    //Delete
    private void deleteOneLetter() {
        Log.i("Key","One Letter Delete");
        InputConnection inputConnection = getCurrentInputConnection();
        CharSequence selectedText = inputConnection.getSelectedText(0);
        if (TextUtils.isEmpty(selectedText)) {
            inputConnection.deleteSurroundingText(1, 0);
        } else {
            inputConnection.commitText("", 1);
        }
    }

    //Timer Tick
    private void startKeyTimer() {
        mKeyTimer = new KeyTimer(500, 100);
        mKeyTimer.start();
        Log.i("KEY", "Timer Began");
    }

    private void confirmChar(int i) {
        //Check Code
        if (i >= 12593 && i <= 12643) {//Kor
            //Check timer
            if (mKeyTimer.isTimerEnd) {
                Log.i("Hangul","Timer Already End");
                if (i >= 12623) {//Moeum
                    Log.i("Hangul","Input is Moeum");
                    if (mLetterCursor==1) {
                        Log.i("Hangul","Cursor Pos is Jungseong");
                        inputLetter(mLetterCursor,i,false);
                    } else {
                        Log.i("Hangul","Cursor Pos is not Jungseong");
                        //TODO: Complete
                        completePreview();

                        inputChar(i);
                        Log.i("Hangul","Input Single Letter: "+(char) i);
                    }
                } else {
                    Log.i("Hangul","Input is Jaeum");
                    if (mLetterCursor==0) {
                        resetLetter();
                    }
                    if (mLetterCursor==1 || mLetterCursor==2) {
                        Log.i("Hangul","Cursor Pos is Jungseong");
                        //TODO: Complete
                        completePreview();

                    }
                    inputLetter(mLetterCursor,i,false);

                }
            } else {
                Log.i("Hangul","Timer Yet End");
                int preCursor =setCursorPos(mLetterCursor-1);
                int[] t;
                if (i>=12623) {
                    Log.i("Hangul","Input is Moeum");
                    if (preCursor==1) {
                        Log.i("Hangul","Pre-Cursor Pos is Jungseong");
                        t=mergeMoeum(mLetters[preCursor],i);
                        if (t[0]==4) {//Merged
                            Log.i("Hangul","Letter Merged and fit to pos");
                            inputLetter(preCursor,t[1],true);
                        } else {//Cannot Merge
                            Log.i("Hangul","Letter cannot merged");
                            //TODO: Complete
                            completePreview();

                            Log.i("Hangul","Input single Letter: "+(char) i);
                            inputChar(i);
                        }
                    } else if (preCursor==0) {
                        Log.i("Hangul","Pre-cursor pos is choseong");
                        inputLetter(mLetterCursor,i,false);
                    } else if (preCursor==2) {
                        Log.i("Hangul","Pre-cursor pos is jongseong");
                        //TODO: Complete
                        completePreview();

                        Log.i("Hangul","Input single Letter: "+(char) i);
                        inputChar(i);
                    }
                } else {
                    Log.i("Hangul","Input is Jaeum");
                    if (preCursor==1) {
                        Log.i("Hangul","Pre-cursor pos is jungseong");
                        inputLetter(mLetterCursor,i,false);
                    } else if (preCursor==0) {
                        Log.i("Hangul","Pre-cursor pos is choseong");
                        t=mergeJaeum(mLetters[preCursor],i);
                        if (t[0] ==1 || t[0]==2) {//Merge
                            Log.i("Hangul","Letter Merged and fit to pos");
                            inputLetter(preCursor,t[1],true);
                            mLetterCursor=1;
                        } else {//Cannot Merge
                            Log.i("Hangul","Letter cannot merged");
                            //TODO: Complete
                            completePreview();

                            inputLetter(mLetterCursor,i,false);
                        }
                    } else if (preCursor==2) {
                        Log.i("Hangul","Pre-cursor pos is jongseong");
                        t=mergeJaeum(mLetters[preCursor],i);
                        if (t[0] ==2 || t[0]==3) {//Merge
                            Log.i("Hangul","Letter merged and fit to pos");
                            inputLetter(preCursor,t[1],true);
                            deleteOneLetter();
                            completePreview();
                        } else {//Cannot Merge
                            Log.i("Hangul","Letter cannot merged");
                            //TODO: Complete
                            completePreview();

                            resetLetter();
                            inputLetter(mLetterCursor,i,false);
                        }
                    }
                }
            }
            startKeyTimer();
        } else {//Just Letter
            inputChar(i);
        }
    }

    private void inputLetter(int pos, int i, boolean merge) {
        Log.i("Hangul","Insert Letter "+(char) i+" @ "+pos);
        mLetters[pos]=i;
        inputPreview();
        if (!merge) {
            mLetterCursor++;
        }
        if (mLetterCursor > 2) {
            mLetterCursor=0;
        }
    }

    private void inputPreview() {
        if (mLetterCursor > 0) {
            deleteOneLetter();
        }
        Log.i("Hangul","Input Preview Letter "+(char) createHangul());
        inputChar(createHangul());
    }

    private void completePreview() {
        deleteOneLetter();
        Log.i("Hangul","Complete Pre-Letter "+(char) createHangul());
        inputChar(createHangul());
        resetLetter();
    }

    private int createHangul() {
        int a=44032+getChoseongCode(mLetters[0])*21*28+(mLetters[1]-12623)*28;
        if (mLetters[1]==0) {
            return mLetters[0];
        }
        return a + getJongseongCode(mLetters[2]);
    }

    private void resetLetter() {
        Log.i("Hangul","Reset Data");
        mLetterCursor=0;
        mLetters[0]=0;
        mLetters[1]=0;
        mLetters[2]=0;
    }

    private int setCursorPos(int i) {
        if (i > 2) {
            i=0;
        } else if (i < 0) {
            i=2;
        }
        return i;
    }

    /*
    private boolean checkCorrectLetterPos(int pos, int t) {
        switch(pos) {
            case 0://Choseong
                switch(t) {
                    case 1:
                    case 2:
                        return true;
                }
                break;
            case 1://Jungseong
                if (t==4) {
                    return true;
                }
                break;
            case 2://Jonnseong
                switch(t) {
                    case 2:
                    case 3:
                        return true;
                }
                break;
        }

        return false;
    }
    */
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

    private void inputChar(int i) {

        //Input
        InputConnection inputConnection = getCurrentInputConnection();
        char code=(char) i;
        inputConnection.commitText(String.valueOf(code), 1);
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

    private void playClick(int i) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch(i){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
