package rtmppusher.demo.com.rtmppusher.utils.pusher;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class CameraView {

    public interface PreviewFrameCallback{
        public void apply(Object data);
    }

    private VideoSource videoSource;
    private AudioSource audioSource;
    private byte[] buffer;
    private GPUImage gpuImage;
    private int width = 640;
    private int height = 480;
    private PreviewFrameCallback callback;
    private CommonCallback recordCallback;
    private Context context;
    private GLSurfaceView glSurfaceView;

    public CameraView(Context context) {
        this.context = context;;
        init();
    }

    private void init() {
        videoSource = new VideoSource();
        audioSource = new AudioSource();
        videoSource.setPreviewCallback(this::onPreviewFrame);
        //添加gpu
//        gpuImage = new GPUImage(context);
//        gpuImage.setRotation(Rotation.ROTATION_270, false, true); //镜像
        audioSource.setRecordCallback(this::onRecordFrame);
    }

    public void setPreviewCallback(PreviewFrameCallback callback){
        if(callback != null){
            this.callback = callback;
        }
    }

    public void setRecordCallback(CommonCallback callback){
        if(callback != null){
            this.recordCallback = callback;
        }
    }

    public void setSurfaceView(GLSurfaceView glSurfaceView){
        gpuImage.setGLSurfaceView(glSurfaceView);
        this.glSurfaceView = glSurfaceView;
    }

    public void startCamera() {
        if(videoSource != null){
            videoSource.openCamera();
        }
    }

    public void startRecord(){
        if(audioSource != null){
            audioSource.start();
        }
    }

    public void start(){
        startCamera();
        startRecord();
    }

    public void isCameraUsed(){
        if(videoSource != null){
            videoSource.isCameraUsed();
        }
    }

    public void stopCamera(){
        if(videoSource != null){
            videoSource.releaseCamera();
        }
    }

    public VideoSource getVideoSource() {
        return videoSource;
    }

    public AudioSource getAudioSource() {
        return audioSource;
    }

    //帧处理
    public void onPreviewFrame(Object obj) {
        //使用gpu进行显示
//        if (glSurfaceView != null && gpuImage != null) {
//            gpuImage.updatePreviewFrame(data, width, height);
//            gpuImage.requestRender();
//        }
        //扩展其它功能
        if(this.callback != null){
            this.callback.apply(obj);
        }
    }

    public void onRecordFrame(Object data){
        if(recordCallback != null){
            recordCallback.apply(data);
        }
    }

    //rtmp推流
    public void startPublish(String rtmpUrl) {
    }

    public void stopPublish() {
    }
}
