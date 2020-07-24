package com.example.serenity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.serenity.utils.LockScreenService;
import com.example.serenity.utils.LockScreenUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.regex.Pattern;

public class LockApp extends Activity implements LockScreenUtils.OnLockStatusChangedListener {

    private static boolean cheated = false;
    private Button btnUnlock;
    private Button BtnLock;
    private TextView timer;
    private TextView instructions;
    TextView timeleft;
    public static boolean complete = false;

    private CountDownTimer countDownTimer;
    public long timeLeftInMilliSeconds; //= 1200000; //1200000
    android.app.AlertDialog alert;
    //TodoListTimeDialog dialog = new TodoListTimeDialog(this);

    private LockScreenUtils mLockScreenUtils;


    /*@Override
    public void onAttachedToWindow() {
        this.getWindow().setType(
                WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );
        super.onAttachedToWindow();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_app);

        timer = findViewById(R.id.countdown);
        timer.setVisibility(View.INVISIBLE);
        timeleft = findViewById(R.id.timeLeftText);
        timeleft.setVisibility(View.INVISIBLE);

        init();

// unlock screen in case of app get killed by system
        if (getIntent() != null && getIntent().hasExtra("kill")
                && getIntent().getExtras().getInt("kill") == 1) {
            enableKeyguard();
            unlockHomeButton();
        } else {

            try {
                // disable keyguard
                //disableKeyguard();

                // lock home button
                lockHomeButton();

                // start service for observing intents
                startService(new Intent(this, LockScreenService.class));

                // listen the events get fired during the call
                StateListener phoneStateListener = new StateListener();
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                telephonyManager.listen(phoneStateListener,
                        PhoneStateListener.LISTEN_CALL_STATE);

            } catch (Exception e) {
            }

        }
    }

    private void init() {
        mLockScreenUtils = new LockScreenUtils();
        instructions = findViewById(R.id.tvInstructions);
        btnUnlock = (Button) findViewById(R.id.btnUnlock);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("unlock", "onClick: " + cheated);
                // unlock home button and then screen on button press
                cheated = true;
                unlockHomeButton();
                timer.setVisibility(View.INVISIBLE);
                instructions.setText(getResources().getString(R.string.giveup));
                unlockDevice();

                //setResult(RESULT_OK, new Intent());
            }
        });

        final EditText input = findViewById(R.id.userInput);
        //input.setInputType(InputType.TYPE_CLASS_NUMBER);
        BtnLock = findViewById(R.id.btnLock);
        BtnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input.getText().toString().isEmpty()) {
                    input.setError("Please enter duration");
                    input.requestFocus();
                } else if(Pattern.matches("[a-zA-Z]+", input.getText().toString())){
                    input.setError("Please enter duration in numbers");
                    input.requestFocus();
                }  else {
                    input.requestFocus();
                    timeLeftInMilliSeconds = Long.parseLong((input.getText().toString())) * 60000;
                    instructions.setText(getResources().getString(R.string.startedtimer));
                    BtnLock.setVisibility(View.GONE);
                    timer.setVisibility(View.VISIBLE);
                    timeleft.setVisibility(View.VISIBLE);
                    input.setVisibility(View.GONE);
                    startCountDown();
                }
            }
        });
    }

    private void startCountDown() {
        disableKeyguard();
        countDownTimer = new CountDownTimer(timeLeftInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliSeconds = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                instructions.setText(getResources().getString(R.string.end));
                unlockDevice();
                ToDoListFragment.deleteData(ToDoListFragment.getParent(), FirebaseAuth.getInstance().getCurrentUser().getUid(), ToDoListFragment.getDeleted());
                setResult(RESULT_OK, new Intent());

            }
        }.start();
    }

    private void updateTimer() {
        int mins = (int) (timeLeftInMilliSeconds / 1000) / 60;
        int sec = (int) (timeLeftInMilliSeconds / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d: %02d", mins, sec);
        timer.setText(timeLeftFormatted);
    }


    // Handle events of calls and unlock screen if necessary
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    unlockHomeButton();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }

    ;

    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
        return;
    }

    // Handle button clicks
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {

            return true;
        }

        return false;

    }

    // handle the key press events here itself
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

            return true;
        }
        return false;
    }

    // Lock home button
    public void lockHomeButton() {
        mLockScreenUtils.lock(LockApp.this);
    }

    // Unlock home button and wait for its callback
    public void unlockHomeButton() {
        mLockScreenUtils.unlock();
    }

    // Simply unlock device when home button is successfully unlocked
    @Override
    public void onLockStatusChanged(boolean isLocked) {
        if (!isLocked) {
            unlockDevice();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unlockHomeButton();
    }

    @SuppressWarnings("deprecation")
    private void disableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.disableKeyguard();
    }

    @SuppressWarnings("deprecation")
    private void enableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.reenableKeyguard();
    }

    //Simply unlock device by finishing the activity
    private void unlockDevice() {
        finish();
    }

    public static boolean getCheated() {
        return cheated;
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        long value = data.getParcelableExtra("EXTRA_TIME");
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                timeLeftInMilliSeconds = value * 60000;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/
}