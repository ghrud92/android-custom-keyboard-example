package com.rivmt.kbd.hangulkeyboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Pattern;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private KeyboardView keyboardView;
    private Keyboard keyboard;

    private short mTypeInput = 0; //0:단모음, 1:쿼티, 2:
    private KeyTimer mKeyTimer = new KeyTimer(500, 100);
    private StringBuilder mCandidateString = new StringBuilder();
    private IMEMaster mIME = new IMEDanmoeum();


    @Override
    public View onCreateInputView() {
        //Timer Class
        mKeyTimer.start();

        //Candidate
        this.setCandidatesViewShown(true);

        //Keyboard View
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        EditorInfo ei = getCurrentInputEditorInfo();
        //ei.
        setKeyboardLayout(R.xml.layout_danmoeum);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    private int selectKeyboardLayout() {


        return 0;
    }

    private void setKeyboardLayout(int layout) {
        keyboard = new Keyboard(this, layout);
        keyboardView.setKeyboard(keyboard);
    }

    /*
    @Override
    public View onCreateCandidatesView() {
        return
    }
    */

    @Override
    public void onPress(int primaryCode) {
        //Play Sound
        playClick(primaryCode);

        //Preview
        keyboardView.setPreviewEnabled(true);
    }

    @Override
    public void onRelease(int primaryCode) {
        //Preview
        keyboardView.setPreviewEnabled(checkPreview(primaryCode));
        Log.i("Key","Key Released "+primaryCode);
    }

    public boolean checkPreview(int i) {
        return (i >= 12593 && i <= 12643);
    }

    @Override
    public void onKey(int i, int[] ints) {
        InputConnection inputConnection = getCurrentInputConnection();
        EditorInfo info = getCurrentInputEditorInfo();
        if (inputConnection == null) {
            return;
        }

        //Code Control
        switch (i) {
            case Keyboard.KEYCODE_DELETE :
                deleteOneLetter();
                break;
            case Keyboard.KEYCODE_SHIFT:
                break;
            case Keyboard.KEYCODE_DONE:
                completeCandidate();
                inputDone(inputConnection, info);
                break;
            case -3://Language
                //Complete Candidate
                completeCandidate();
                //Change Language
                InputMethodManager imm =(InputMethodManager) getSystemService((Context.INPUT_METHOD_SERVICE));
                boolean ttt;
                imm.showInputMethodPicker();
                /*if (android.os.Build.VERSION.SDK_INT < 28) {
                    ttt=imm.switchToNextInputMethod(keyboardView.getWindowToken(), false);
                    Log.i("Sys","Change Keyboard (SDK Lower than 28)");
                } else {
                    ttt=switchToNextInputMethod(false);
                    Log.i("Sys","Change Keyboard (SDK Bigger than 28)");
                    if (!ttt) {
                        Log.i("Sys","Changed to Previous Keyboard");
                        switchToPreviousInputMethod();
                    }
                }*/
                break;
            default:
                if (mIME.checkCode(i)) {
                    mIME.createLetter(i,!mKeyTimer.isTimerEnd);
                } else {
                    completeCandidate();
                    inputChar(i);
                }
                //Log.i("Hangul",(char) mLetters[0]+", "+(char) mLetters[1]+", "+(char) mLetters[2]);
        }

        //Others
        startKeyTimer();
        refreshCandidate();
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent e) {
        switch(i) {
            case -3://Lang
                Log.i("Sys","Lang Key Down");
                e.startTracking();
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyLongPress(int i, KeyEvent e) {
        switch(i) {
            case -3://Language
                Log.i("Sys","Language Selector due to long press");
                InputMethodManager imm =(InputMethodManager) getSystemService((Context.INPUT_METHOD_SERVICE));
                imm.showInputMethodPicker();
                return true;
        }
        return false;
    }

    private void completeCandidate() {
        //Input
        InputConnection inputConnection = getCurrentInputConnection();
        inputConnection.commitText(mCandidateString.toString(), 1);
        mIME.mInfo.mCreateLetters=new StringBuilder();
        refreshCandidate();
    }

    private void refreshCandidate() {
        mCandidateString=mIME.mInfo.mCreateLetters;
        replaceAll(mCandidateString,String.valueOf(mIME.mBlankWord),"");
        InputConnection inputConnection = getCurrentInputConnection();
        inputConnection.setComposingText(mCandidateString,1);
        Log.i("Cand","Candidate: "+mCandidateString);
    }

    public static StringBuilder replaceAll(StringBuilder sb, String find, String replace){
        return new StringBuilder(Pattern.compile(find).matcher(sb).replaceAll(replace));
    }

    private void inputDone(InputConnection ic, EditorInfo ei) {
        int type=ei.imeOptions & EditorInfo.IME_MASK_ACTION;
        switch(type) {
            case EditorInfo.IME_ACTION_UNSPECIFIED:
            case EditorInfo.IME_ACTION_NONE:
            case EditorInfo.IME_ACTION_DONE:
            case EditorInfo.IME_ACTION_GO:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SEARCH));
                break;
            default:
                if ((ei.inputType & EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0) {
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                } else {
                    ic.performEditorAction(type);
                }
                break;
        }
    }

    //Delete
    private void deleteOneLetter() {
        Log.i("Key","One Letter Delete");
        mIME.deleteOrder();
        boolean a = mIME.mInfo.deleteOneLetter();
        if (!a) {
            InputConnection inputConnection = getCurrentInputConnection();
            CharSequence selectedText = inputConnection.getSelectedText(0);
            if (TextUtils.isEmpty(selectedText)) {
                inputConnection.deleteSurroundingText(1, 0);
            } else {
                inputConnection.commitText("", 1);
            }
        }
    }

    //Timer Tick
    private void startKeyTimer() {
        mKeyTimer.cancel();
        mKeyTimer = new KeyTimer(500, 100);
        mKeyTimer.start();
        Log.i("KEY", "Timer Began");
    }

    private void inputChar(int i) {
        //Input
        InputConnection inputConnection = getCurrentInputConnection();
        char code=(char) i;
        inputConnection.commitText(String.valueOf(code), 1);
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
