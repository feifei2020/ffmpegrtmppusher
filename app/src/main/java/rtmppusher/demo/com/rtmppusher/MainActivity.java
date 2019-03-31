package rtmppusher.demo.com.rtmppusher;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import butterknife.BindView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.util.Rotation;
import rtmppusher.demo.com.rtmppusher.utils.pusher.AudioSource;
import rtmppusher.demo.com.rtmppusher.utils.pusher.VideoSource;

public class MainActivity extends Activity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @BindView(R.id.glSurfaceView)
    GLSurfaceView glSurfaceView;

    private AudioSource audioSource;
    private VideoSource videoSource;
    private String videoUrl = "rtmp://192.168.0.105/live/1234";
    private GPUImage gpuImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GLSurfaceView预览
        glSurfaceView = findViewById(R.id.glSurfaceView);
        //音频封装
        audioSource = new AudioSource();
        audioSource.setRecordCallback(this::onRecordFrame); //回调
        //摄像头封装
        videoSource = new VideoSource();
        videoSource.setPreviewCallback(this::onPreviewFrame); //回调
        //添加gpu美颜
        gpuImage = new GPUImage(this);
        gpuImage.setRotation(Rotation.ROTATION_270, false, true); //镜像
        gpuImage.setGLSurfaceView(glSurfaceView);
        //推流初始化
        init();
        startPush(videoUrl); //开始推

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
        //音频推流
        pushAudio(data, data.length, 0);
    }

    public void onPreviewFrame(Object obj) {
        byte[] data = (byte[])obj;
        //gpu美颜色显示
        if (glSurfaceView != null && gpuImage != null) {
            gpuImage.updatePreviewFrame(data, 640, 480);
            gpuImage.requestRender();
        }
        //视频推流
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
