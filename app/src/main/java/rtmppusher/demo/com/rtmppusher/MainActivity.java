package rtmppusher.demo.com.rtmppusher;

import android.app.Activity;
import android.os.Bundle;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import rtmppusher.demo.com.rtmppusher.utils.pusher.AudioSource;
import rtmppusher.demo.com.rtmppusher.utils.pusher.VideoSource;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    AudioSource audioSource;
    VideoSource videoSource;
    String videoUrl = "rtmp://192.168.0.105/live/1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioSource = new AudioSource();
        audioSource.setRecordCallback(this::onRecordFrame);
        videoSource = new VideoSource();
        videoSource.setPreviewCallback(this::onPreviewFrame);
        init();
        startPush(videoUrl);
        //权限申请
        AndPermission.with(this)
                .permission(Permission.Group.CAMERA)
                .onGranted(permissions -> {
                    videoSource.openCamera();
                })
                .onDenied(permissions -> {
                })
                .start();
        //权限申请
        AndPermission.with(this)
                .permission(Permission.Group.MICROPHONE)
                .onGranted(permissions -> {
                    audioSource.start();
                })
                .onDenied(permissions -> {
                })
                .start();
    }

    @Override
    public void onBackPressed() {
    }

    public void onResume(){
        super.onResume();
    }

    public void onDestroy(){
        super.onDestroy();
    }

    public void onRecordFrame(Object obj) {
        byte[] data = (byte[])obj;
        pushAudio(data, data.length, 0);
    }

    public void onPreviewFrame(Object obj) {
        byte[] data = (byte[])obj;
        pushVideo(data, data.length, 0);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String init();
    public native String destroy();
    public native void pushVideo(byte[] data, int len, long timestamp);
    public native void pushAudio(byte[] data, int len, long timestamp);
    public native void startPush(String url);
    public native void stopPush();
}
