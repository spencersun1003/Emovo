package org.pytorch.demo.speechrecognition.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;


import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


import org.pytorch.LiteModuleLoader;

import com.chaquo.python.Python;
//import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.PyObject;
//import com.chaquo.python.Kwarg;

import org.pytorch.demo.speechrecognition.MainActivity;
import org.pytorch.demo.speechrecognition.R;
import org.pytorch.demo.speechrecognition.Wave;

public class HomeFragment extends Fragment implements Runnable {

    private static final String TAG = MainActivity.class.getName();

    private Module mModuleEncoder;
    private TextView mtvEmoState;
    private TextView mtvTest;
    private ImageButton mButton;
    private ImageView mImgvEmoState;
    private ProgressBar mprobarEmoInference;
    private boolean mButtonisPlay=false;


    private int EmoState=EMO_NEUTRAL;
    private float EmoInference=0;
    private int AngryTimes=0;

    private final static int EMO_NEUTRAL=1;
    private final static int EMO_ANGER=2;
    private final static int REQUEST_RECORD_AUDIO = 13;
    private final static int AUDIO_LEN_IN_SECOND = 5;
    private final static int SAMPLE_RATE = 22050;//16000;
    private final static int RECORDING_LENGTH = SAMPLE_RATE * AUDIO_LEN_IN_SECOND;

    private final static int HOP_LENGTH=512;
    private final static int FRAME_LENGTH=2048;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private static long test_inference_start=0;
    private static long test_inference_end=0;

    private int mStart = 1;
    private HandlerThread mTimerThread;
    private Handler mTimerHandler;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mTimerHandler.postDelayed(mRunnable, 1000);
            System.out.println("keep running");
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(
                    () -> {
                        mtvTest.setText(String.format("Listening - %ds left", AUDIO_LEN_IN_SECOND - mStart));
                        mStart += 1;
                    });
        }
    };

    @Override
    public void onDestroy() {
        //super.onDestroyView();
        System.out.println("destory View...");
        //getActivity().runOnUiThread;
        //stopTimerThread();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        //super.onDestroyView();
        System.out.println("pause View...");
        try{
            Handler handler= new Handler();
            handler.removeCallbacks(mRunnable);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        //super.onDestroyView();
        System.out.println("Resume View...");
//        try{
//            stopTimerThread();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        super.onResume();
    }

    protected void stopTimerThread() {
        mTimerThread.quitSafely();
        try {
            mTimerThread.join();
            mTimerThread = null;
            mTimerHandler = null;
            mStart = 1;
        } catch (InterruptedException e) {
            Log.e(TAG, "Error on stopping background thread", e);
        }
    }

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        mButton = root.findViewById(R.id.imgbtnSorP);
        mButton.setImageResource(R.drawable.icon2);
        mImgvEmoState=root.findViewById(R.id.imgvEmoState);
        mtvEmoState = root.findViewById(R.id.tvState);
        mtvTest=root.findViewById(R.id.tvStatisticTest);
        mprobarEmoInference=root.findViewById(R.id.probarEmoInference);
        mprobarEmoInference.setProgress(1);

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //mButton.setText(String.format("Listening - %ds left", AUDIO_LEN_IN_SECOND));
                mButton.setEnabled(false);

                Thread thread = new Thread(HomeFragment.this);
                thread.start();

                mTimerThread = new HandlerThread("Timer");
                mTimerThread.start();
                mTimerHandler = new Handler(mTimerThread.getLooper());
                mTimerHandler.postDelayed(mRunnable, 1000);
                if(mButtonisPlay){
                    ((ImageButton)v).setImageResource(R.drawable.icon2);
                    mtvTest.setText("Stop Run");
                }
                else{
                    ((ImageButton)v).setImageResource(R.drawable.icon1);
                    mtvTest.setText("Recording");
                }
                mButtonisPlay = !mButtonisPlay;

            }
        });
        requestMicrophonePermission();
        requestStoragePermission();

        return root;
    }

    //权限申请
    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        }
    }

    private String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, assetName + ": " + e.getLocalizedMessage());
        }
        return null;
    }



    private void saveFile(String data,String path,String fileName) throws IOException {


        File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fullpath=new File(dir,"/myEmovo/log");
        if (!fullpath.exists()){
            fullpath.mkdirs();
        }

        File file = new File(fullpath,fileName);
        try {
            if (!file.exists()){
                file.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        FileOutputStream fos = new FileOutputStream( file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        writer.write(data);
        writer.flush();
        writer.close();
        MediaScannerConnection.scanFile(getActivity(),
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }
    private void log(float result){
        String.valueOf(result);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" };


//    private static void verifyStoragePermissions(Activity activity) {
//        try {
//        //检测是否有写的权限
//        int permission = ActivityCompat.checkSelfPermission(activity,"android.permission.WRITE_EXTERNAL_STORAGE");
//            if (permission != PackageManager.PERMISSION_GRANTED) {
//            // 没有写的权限，去申请写的权限，会弹出对话框
//            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            }
//
//        }


    private void showTranslationResult(String result) {
        mtvTest.setText(result);
    }
    private void changeEmoState(int EmoState){
        int res;
        String EmoHint="";
        if (EmoState==EMO_NEUTRAL) {
            res = R.drawable.emoji_neutral;
            EmoHint="I am satisfied!";
        }
        else if(EmoState==EMO_ANGER){
            res=R.drawable.emoji_anger;
            EmoHint="I am angry!";

            AngryTimes+=1;
            if (AngryTimes>=2){
                showNormalDialog();
            }
        }
        else {
            res=R.drawable.emoji_anger;
            EmoHint="I am satisfied!";
        }
        mImgvEmoState.setImageResource(res);
        mtvEmoState.setText(EmoHint);

    }



    private void realtimeRecorder(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            throw new IllegalStateException();
            //return;
        }
        record.startRecording();

        long shortsRead = 0;
        int recordingOffset = 0;
        float[] audioBuffer = new float[bufferSize / 2];
        float[] recordingBuffer = new float[RECORDING_LENGTH];

        while (shortsRead < RECORDING_LENGTH/audioBuffer.length*audioBuffer.length) {
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length,AudioRecord.READ_NON_BLOCKING);
            shortsRead += numberOfShort;
            System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberOfShort);
            recordingOffset += numberOfShort;
        }

        record.stop();
        record.release();
        stopTimerThread();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtvTest.setText("Recognizing...");
            }
        });

//        int min = (int) Collections.min(Arrays.asList(recordingBuffer));
//        int max = (int) Collections.max(Arrays.asList(recordingBuffer));
//        System.out.println("最小值: " + min);
//        System.out.println("最大值: " + max);

        //send data to chaquo preprocess module
        test_inference_start= SystemClock.uptimeMillis();
        Python py=Python.getInstance();
        PyObject data=py.getModule("DataPre").callAttr("Preprocess",recordingBuffer);
        float[] inputfloat =data.toJava(float[].class);
        test_inference_end= SystemClock.uptimeMillis();
        //recognize

        final String result = recognize(inputfloat,(int)562);


        try {
            saveFile(result,"/logs","log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SetProgress(mprobarEmoInference,mprobarEmoInference.getProgress(), (int) (EmoInference*100));
                showTranslationResult(result);
                changeEmoState(EmoState);
                if(mButtonisPlay){
                    mButton.setImageResource(R.drawable.icon2);
                    //mtvTest.setText("Stop Running");
                    mtvTest.setText(String.valueOf(test_inference_end-test_inference_start));
                }
                else{
                    mButton.setImageResource(R.drawable.icon1);
                    mtvTest.setText("Recording");
                }
                mButtonisPlay = !mButtonisPlay;
                mButton.setEnabled(true);

            }
        });

    }


    private void toFileRecorder_byAR() throws FileNotFoundException {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            throw new IllegalStateException();
            //return;
        }
        record.startRecording();

        long shortsRead = 0;
        int recordingOffset = 0;
        short[] audioBuffer = new short[bufferSize / 2];
        short[] recordingBuffer = new short[RECORDING_LENGTH];

        while (shortsRead < RECORDING_LENGTH/audioBuffer.length*audioBuffer.length) {
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length,AudioRecord.READ_NON_BLOCKING);
            shortsRead += numberOfShort;
            System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberOfShort);
            recordingOffset += numberOfShort;
        }

        record.stop();
        record.release();
        stopTimerThread();

        Wave wavFile= new Wave(SAMPLE_RATE, (short) 1,recordingBuffer,0,recordingBuffer.length-1);
        File fullpath=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/myEmovo/record");
        if (!fullpath.exists()){
            fullpath.mkdirs();
        }
        File dir=new File(fullpath,"record.wav");
        //System.out.println(dir);
        //File dir=new File("/data/data/org.pytorch.demo.speechrecognition/files/chaquopy/AssetFinder/app","record.wav");
        if (!dir.exists()){
            System.out.println("warning:dir not exits!");
        }
        wavFile.wroteToFile(dir);
        FileInputStream ios=new FileInputStream(dir);

//        int min = (int) Collections.min(Arrays.asList(recordingBuffer));
//        int max = (int) Collections.max(Arrays.asList(recordingBuffer));
//        System.out.println("最小值: " + min);
//        System.out.println("最大值: " + max);

        Python py=Python.getInstance();
        PyObject data=py.getModule("DataPre").callAttr("Preprocess2",ios);
        float[] inputfloat =data.toJava(float[].class);
        //test_inference_start= SystemClock.uptimeMillis();
        final String result = recognize(inputfloat,(int)562);
//        test_inference_end=SystemClock.uptimeMillis();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTranslationResult(String.valueOf(test_inference_end-test_inference_start));
                mButton.setEnabled(true);
                //mTextView.setText("Start");
            }
        });
        //save(result,"log.txt");
        try {
            saveFile(result,"/logs","log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTranslationResult(result);
                mButton.setEnabled(true);
                //mTextView.setText("Start");
            }
        });

    }

    //
    private class MyTimerTask extends TimerTask {
        private MediaRecorder recorder=null;
        private File filedir=null;
        MyTimerTask(MediaRecorder recorder,File filedir){
            this.recorder=recorder;
            this.filedir=filedir;
        }
        public void run() {
            recorder.stop();
            recorder.release();

            Python py=Python.getInstance();
            PyObject data=py.getModule("DataPre").callAttr("Preprocess2",filedir);
            float[] inputfloat =data.toJava(float[].class);


            final String result = recognize(inputfloat,(int)562);
            //save(result,"log.txt");
            try {
                saveFile(result,"/logs","log.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showTranslationResult(result);
                    mButton.setEnabled(true);
                    mtvTest.setText("Start");
                }
            });



        }
    }
    //先将录音存为音频，再用librosa直接读取
    private void toFileRecorder_byMR(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        final MediaRecorder recorder = new MediaRecorder();
        //ContentValues values = new ContentValues(3);
        //values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //recorder.setMaxDuration(AUDIO_LEN_IN_SECOND);
        File fullpath=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/myEmovo/record");
        if (!fullpath.exists()){
            fullpath.mkdirs();
        }

        File dir=new File(fullpath,"record.m4a");
        recorder.setOutputFile(dir);
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
            while (true){
                System.out.println("prepare wrong!");
            }

        }
        recorder.start();

        Timer timer = new Timer();
        timer.schedule(new MyTimerTask(recorder,dir), 2000, 5000);
    }

    public void run() {
        //toFileRecorder_byMR();
        realtimeRecorder();

//        try {
//            toFileRecorder_byAR();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

    }

    private String recognize(float[] floatInputBuffer,int shape0) {
        if (mModuleEncoder == null) {

            mModuleEncoder = LiteModuleLoader.load(assetFilePath(getActivity().getApplicationContext(), "Model2.ptl"));
        }
        int Length=floatInputBuffer.length;
        double wav2vecinput[] = new double[Length];
        for (int n = 0; n < Length; n++)
            wav2vecinput[n] = floatInputBuffer[n];

        FloatBuffer inTensorBuffer = Tensor.allocateFloatBuffer(Length);
        for (double val : wav2vecinput)
            inTensorBuffer.put((float)val);

        Tensor inTensor = Tensor.fromBlob(inTensorBuffer, new long[]{(int)Length/shape0,(int)Length/shape0, shape0});
        Tensor ResultTensor=mModuleEncoder.forward(IValue.from(inTensor)).toTensor();
        float[] ResultFloat=ResultTensor.getDataAsFloatArray();
        float result =ResultFloat[0]/(ResultFloat[0]+ResultFloat[1]);
        this.EmoInference=result;
        if (result<0.5){
            this.EmoState=EMO_NEUTRAL;
        }
        else if (result>0.5){
            this.EmoState=EMO_ANGER;
        }


        return String.valueOf(result);
    }

    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        //LayoutInflater inflater=LayoutInflater.from( getActivity() );
        //View myview=inflater.inflate(R.layout.catalogin,null);
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity());
        normalDialog.setIcon(R.drawable.img_100793_0_9);
        normalDialog.setTitle("Have a Rest!");
        normalDialog.setMessage("Listen to a soft music?");
        normalDialog.setPositiveButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        normalDialog.setNegativeButton("Go!(Open Netease)",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StartAnotherApp();
                    }
                });
        // 显示
        normalDialog.show();
    }

    public void StartAnotherApp(){
        Intent LaunchIntent = Objects.requireNonNull(getActivity()).getPackageManager().getLaunchIntentForPackage("com.netease.cloudmusic");
        startActivity(LaunchIntent);
    }

    public void SetProgress(final ProgressBar view, int startprogress, int endprogress) {//进度条的控件，以及起始的值

        view.setVisibility(View.VISIBLE);


        ValueAnimator animator = ValueAnimator.ofInt(startprogress, endprogress).setDuration(800);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setProgress((int) valueAnimator.getAnimatedValue());
            }
        });
        animator.start();
    }
}