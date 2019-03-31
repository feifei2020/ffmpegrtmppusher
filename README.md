# ffmpegrtmppusher
基于ffmpeg的直播推流器，超级稳定，经过长时间稳定性测试，超低延时，可用于手机，电视，嵌入式等直播App及设备。

开发环境 安卓 android
android studio 3.2 

工程加入如下基础模块：

1、摄像头数据推流，音频数据推流，接口简单，几行代码可实现直播推流，及监控功能。

2、权限请求。

3、内有一套编译好的ffmpeg so 库可供NDK开发使用。

4、集成了Camera2的使用。

5、集成GPU美颜模块，几行代码实现集成gpu，让你的直播都是美美哒。

6、提供了YUV数据的各种转换功能，nv21,nv12,yuv420,yuv420sp等等。

7、还可在此基础上扩展帖图，水印等功能，接口都有接供，几行代码的事儿。

8、还有很多。。。自己下载看吧。



调用代码如下:

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
