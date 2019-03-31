package rtmppusher.demo.com.rtmppusher.utils.pusher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class AudioSource {
    Thread thread = null;
    int mSampleRate = 44100; // 采样率
    int mChannelConfig = AudioFormat.CHANNEL_IN_STEREO; // 音频通道
    int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT; // 采样的数据格式，也就是每个采样值所占空间的大小
    int mMinBufferSize = 0; // 缓冲器大小
    AudioRecord mRecord;
    int mAudioSource = MediaRecorder.AudioSource.MIC;
    byte[] buffer = null;
    CommonCallback callback;

    public AudioSource(){
        //init();
    }

    private void init(){
        mMinBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat); // 缓冲器大小
        mMinBufferSize = 4096;
        mRecord = new AudioRecord(mAudioSource,
                mSampleRate, mChannelConfig, mAudioFormat,
                mMinBufferSize);
        buffer = new byte[mMinBufferSize];
    }

    public void setRecordCallback(CommonCallback callback){
        this.callback = callback;
    }

    //开始获取音频数据
    public void start() {
        stop();
        init();
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
        thread = new Thread(() -> {
            try {
                mRecord.startRecording();
                while (!thread.isInterrupted()) {
                    int bytes = mRecord.read(buffer, 0, mMinBufferSize);
                    if (bytes > 0) {
                        //推流
                        if(this.callback != null){
                            this.callback.apply(buffer);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void stop() {
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
        if(mRecord != null){
            mRecord.stop();
            mRecord.release();
            mRecord = null;
        }
        if(buffer != null){
            buffer = null;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //噪音消除
    private AcousticEchoCanceler canceler;
    public static boolean isDeviceSupport() {
        return AcousticEchoCanceler.isAvailable();
    }

    public boolean initAEC(int audioSession) {
        if(!isDeviceSupport()){
            return false;
        }
        if (canceler != null) {
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }

    public boolean release() {
        if (null == canceler) {
            return false;
        }
        return true;
    }

    short m_pcmBufferShort[];
    public void shortToBytes(){
        for(int i=0; i<m_pcmBufferShort.length; i++){
            short s = m_pcmBufferShort[i];
            putShort(buffer, s, i * 2);
        }
    }
    public static void putShort(byte b[], short s, int index) {
        b[index + 1] = (byte) (s >> 8);
        b[index + 0] = (byte) (s >> 0);
    }
    void calc1(short[] lin,int off,int len) {
        int i,j;
        for (i = 0; i < len; i++) {
            j = lin[i+off];
            lin[i+off] = (short)(j>>2);
        }
    }
}
