package sjtukc3c.smallcar.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import sjtukc3c.smallcar.Constants.MyConstants;
import sjtukc3c.smallcar.Modules.RemoteCommandManager;
import sjtukc3c.smallcar.Modules.SocketThreadMaster;
import sjtukc3c.smallcar.R;
import sjtukc3c.smallcar.Utils.JsonParser;

/**
 * Author: wenhao.zhu[weehowe.z@gmail.com]
 * Created on 7:57 PM 2016/12/26.
 */


public class VoiceFragment extends MasterFragment {

    private static String TAG = VoiceFragment.class.getSimpleName();

    private static int mPort = 15536;

    private SocketThreadMaster mSocketThreadMaster;
    private RemoteCommandManager mCommandManager;

    private TextView mResultText;

    // [IFLYTEK] VOICE OBJECT
    private com.iflytek.cloud.SpeechRecognizer mIat;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_master_voice, container, false);
        mResultText = (TextView) rootView.findViewById(R.id.voice_text);
        com.gc.materialdesign.views.Button voice = (com.gc.materialdesign.views.Button) rootView.findViewById(R.id.btn_master_voice);
        voice.setOnClickListener(this);
        voice.setOnTouchListener(this);
        ButterKnife.bind(this, rootView);

        // 初始化识别对象
        mIat = com.iflytek.cloud.SpeechRecognizer.createRecognizer(VoiceFragment.this.getActivity(), mInitListener);

        return rootView;
    }

    /**
     * used for callback, success or not
     */
    int ret = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_master_voice:

                mIatResults.clear();
                setParam();

                ret = mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    Log.e(TAG, "Failed, error code is " + ret);
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.btn_master_voice:
                if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
                    // I cant get THIS event
                    Log.e(TAG, "BUTTON_RELEASE_EVENT");
                }
                break;
            default:
                break;
        }
        return false;
    }


    //Initialize Listener
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败，错误码：" + code);
            }
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG, "volume：" + volume);
            Log.d(TAG, "data length：" + data.length);
        }

        @Override
        public void onBeginOfSpeech() {
            // this means the recorder is ready
            Log.d(TAG, "Start talking!");
            mResultText.setText("Start talking!");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "Finish talking!");
            mResultText.setText("Finish talking!");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {
            Log.d(TAG, recognizerResult.getResultString());

            String result = getResult(recognizerResult);
            mResultText.setText(result);

            switch (result) {
                case MyConstants.INSTRUCTION_FORWARD:
                    mCommandManager.sendCommand(RemoteCommandManager.CMD_FOWARD);
                    break;
                case MyConstants.INSTRUCTION_BACKWORD:
                    mCommandManager.sendCommand(RemoteCommandManager.CMD_BACK);
                    break;
                case MyConstants.INSTRUCTION_LEFT:
                    mCommandManager.sendCommand(RemoteCommandManager.CMD_LEFT);
                    break;
                case MyConstants.INSTRUCTION_RIGHT:
                    mCommandManager.sendCommand(RemoteCommandManager.CMD_RIGHT);
                    break;
                case MyConstants.INSTRUCTION_STOP:
                    mCommandManager.sendCommand(RemoteCommandManager.CMD_STOP);
                    break;
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.e(TAG, "Error: " + speechError.getPlainDescription(true));
            mResultText.setText("Error: " + speechError.getPlainDescription(true));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {
            // For asking help = =
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = bundle.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d(TAG, "session id =" + sid);
            }
        }
    };

    /**
     * Sets param.
     */
// Use default param setting
    public void setParam() {

        String mEngineType = SpeechConstant.TYPE_CLOUD;

        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // release the connection
        mIat.cancel();
        mIat.destroy();
    }

    private String getResult(RecognizerResult results) {
        String text = JsonParser.parseResult(results.getResultString());
        String sn = null;

        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        return resultBuffer.toString();
    }

    public void setRemoteCommandManager(RemoteCommandManager cmdmanager) {
        mCommandManager = cmdmanager;
    }

    private void checkThread() {
        if (mSocketThreadMaster != null) {
            mSocketThreadMaster.stop();
            mSocketThreadMaster = null;
        }
    }
}
