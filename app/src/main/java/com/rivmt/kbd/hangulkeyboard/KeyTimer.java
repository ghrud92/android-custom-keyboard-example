package com.rivmt.kbd.hangulkeyboard;

import android.os.CountDownTimer;
import android.util.Log;

public class KeyTimer extends CountDownTimer {

    public boolean isTimerEnd = false;

    public KeyTimer(long millisInFuture, long countDownInterval)
    {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //
    }

    @Override
    public void onFinish() {
        isTimerEnd = true;
        Log.i("Timer","Timer End");
    }
}
