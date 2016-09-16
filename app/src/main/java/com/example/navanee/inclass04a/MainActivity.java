package com.example.navanee.inclass04a;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    SeekBar sbCount;
    SeekBar sbLength;
    TextView tvCount;
    TextView tvLength;
    TextView tvPassword;
    static int pCount = 1;
    static int pLength = 8;
    ExecutorService threadPool;
    Handler handler;
    static ArrayList<CharSequence>  passwordsList = new ArrayList<CharSequence>();
    ProgressDialog progressDialog;
    String[] items = {};
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sbCount = (SeekBar) findViewById(R.id.sBarCount);
        sbCount.setMax(7);
        sbLength = (SeekBar) findViewById(R.id.sBarLength);
        sbLength.setMax(15);

        //sbCount.getProgressDrawable().setColorFilter("<Color you wish>", PorterDuff.Mode.MULTIPLY);
        //sbCount.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(R.color.colorPrimary, PorterDuff.Mode.MULTIPLY));

        tvCount = (TextView) findViewById(R.id.passwordCount);
        tvLength = (TextView) findViewById(R.id.passwordLength);
        tvPassword = (TextView) findViewById(R.id.passwordField);

        handler = new Handler(new Handler.Callback(){

            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case GeneratePasswords.STATUS_START:
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Generating Passwords");
                        progressDialog.setMax(pCount);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.show();
                        break;
                    case GeneratePasswords.STATUS_DONE:
                        progressDialog.dismiss();
                        System.out.println("done");
                        displayPasswordsToSelect();
                        break;
                    case GeneratePasswords.STATUS_STEP:
                        try {
                            int progVal = (Integer) msg.obj;
                            progressDialog.setProgress(progVal);
                        } catch (Exception e){

                        }
                        break;
                }
                return false;
            }
        });

        sbCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pCount = progress + 1;
                tvCount.setText("" + pCount);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pLength = progress + 8;
                tvLength.setText("" + pLength);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.buttonThread).setOnClickListener(this);
        findViewById(R.id.buttonAsync).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonThread) {
           threadPool = Executors.newFixedThreadPool(2);
           passwordsList.clear();
           threadPool.execute(new GeneratePasswords(pCount, pLength));
        } else if (v.getId() == R.id.buttonAsync) {
            new GeneratePasswordsOnAsync().execute();
        }
    }

    class GeneratePasswords implements Runnable {
        static final int STATUS_START = 1;
        static final int STATUS_STEP = 2;
        static final int STATUS_DONE = 3;

        int passwordsCnt;
        int passwordsLen;
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = STATUS_START;
            handler.sendMessage(msg);
            for(int i = 0; i < passwordsCnt; i++) {
                passwordsList.add(Util.getPassword(passwordsLen));
                msg = new Message();
                msg.what = STATUS_STEP;
                msg.obj = i + 1;
                handler.sendMessage(msg);
            }
            msg = new Message();
            msg.what = STATUS_DONE;
            msg.obj = passwordsList;
            handler.sendMessage(msg);
        }

        public GeneratePasswords(int passwordsCnt, int passwordsLen) {
            this.passwordsCnt = passwordsCnt;
            this.passwordsLen = passwordsLen;
        }
    }

    class GeneratePasswordsOnAsync extends AsyncTask<Void, Integer, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Generating Passwords");
            progressDialog.setMax(pCount);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            displayPasswordsToSelect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            passwordsList.clear();
            for(int i = 0; i < pCount; i++) {
               passwordsList.add(Util.getPassword(pLength));
               publishProgress(i + 1);
            }
            return null;
        }
    }

    public void displayPasswordsToSelect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CharSequence[] pList = new CharSequence[passwordsList.size()];
        passwordsList.toArray(pList);
        builder.setTitle("Passwords")
                .setItems(pList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ind = which;
                        CharSequence selVal = pList[ind];
                        tvPassword.setText(selVal);
                    }
                });
        builder.create().show();
    }
}

