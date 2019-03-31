package rtmppusher.demo.com.rtmppusher.utils.pusher;

import android.graphics.ImageFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import rtmppusher.demo.com.rtmppusher.utils.log.log;

/**
 * Created by Administrator on 2017/4/20 0020.
 */

public class VideoSource implements Camera.PreviewCallback{
    private Camera mCamera;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT; //前置
    private int mPreviewYuvBufferSize;
    private byte[] mYuvBuffer;
    private int mPreviewWidth = 640;
    private int mPreviewHeight = 480;
    private int mFrameWidth = 0;
    private int mFrameHeight = 0;
    private boolean mIsCroper = false;
    private CommonCallback callback;
    private YuvCroper mCroper;
    private CameraUtils cameraUtils;
    private SurfaceTexture surfaceTexture;
    private int mCamId = -1;

    public interface PreviewFrameCallback {
        public void apply(byte[] data, Camera camera);
    }

    public interface DisplayCallback {
        public void apply(byte[] data);
    }

    public class PreviewSize{
        int width;
        int height;
    }

    public VideoSource(){
        cameraUtils = new CameraUtils();
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        this.surfaceTexture = surfaceTexture;
    }

    public CameraUtils getCameraUtils(){
        return cameraUtils;
    }

    //设置预览尺寸
    public void setPreviewSize(int previewWidth, int previewHeight){
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        mFrameWidth = previewWidth;
        mFrameHeight = previewHeight;
    }

    public PreviewSize getDefaultPreviewSize(){
        return null;
    }

    public PreviewSize getPreviewSize(){
        PreviewSize size = new PreviewSize();
        size.width = mPreviewWidth;
        size.height = mPreviewHeight;
        return size;
    }

    //设置实际的帧尺寸
    public void setFrameSize(int width, int height){
        mFrameWidth = width;
        mFrameHeight = height;
    }

    public int getCameraId(){
        if (mCamId < 0) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int numCameras = Camera.getNumberOfCameras();
            int frontCamId = -1;
            int backCamId = -1;
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    backCamId = i;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    frontCamId = i;
                    break;
                }
            }
            if (frontCamId != -1) {
                mCamId = frontCamId;
            } else if (backCamId != -1) {
                mCamId = backCamId;
            } else {
                mCamId = 0;
            }
        }
        return mCamId;
    }

    //cameraType 前置或后置
    private Camera createCamera(int cameraType) {
        Camera camera = null;
        try {
            int mCamId = getCameraId();
            camera = Camera.open(mCamId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    public Camera getCamera(){
        return mCamera;
    }

    public synchronized void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //初始化裁剪器
    private void initCroper(){
        mIsCroper = true;
        //数据裁剪
        int offsetY = (mPreviewHeight - mFrameHeight) / 2;
        int offsetX = (mPreviewWidth - mFrameWidth) / 2;
        RectF rectF = new RectF(0 + offsetX, 0 + offsetY, mFrameWidth + offsetX, mFrameHeight + offsetY);
        try {
            mCroper = new YuvCroper(0, mPreviewWidth, mPreviewHeight, rectF);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCameraFacing(int cameraFacing){
        mCameraFacing = cameraFacing;
    }

    public boolean isCameraUsed(){
        if(mCamera == null){
            return true;
        }else {
            return false;
        }
    }

    public boolean openCamera() {
        releaseCamera();
        mCamera = createCamera(mCameraFacing); // 根据需求选择前/后置摄像头
        if(mCamera == null){
            return false;
        }
        if (mCamera != null) {
            try {
                //设置参数
                //此方法为官方提供的旋转显示部分的方法，并不会影响onPreviewFrame方法中的原始数据; 默认坚屏
                //mCamera.setDisplayOrientation(90);
                Camera.Parameters parameters = mCamera.getParameters();
                //////////////////////////////////////////////////////////////////////
                parameters.setRecordingHint(true); //网上找的，可提高fps, 会点用cpu，10%左右
//                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
//                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                //////////////////////////////////////////////////////////////////////
                // 设置数据格式, 常用格式：NV21 / YV12
                parameters.setPreviewFormat(ImageFormat.NV21);
                // 预览尺寸设定
                if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                    //app.log("parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);");
                    parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
                    parameters.setPictureSize(mPreviewWidth, mPreviewHeight);
                } else {
                    mPreviewWidth = parameters.getPreviewSize().width;
                    mPreviewHeight = parameters.getPreviewSize().height;
                }
                // 还可以设置很多相机的参数，但是建议先遍历当前相机是否支持该配置，不然可能会导致出错；
                mCamera.setParameters(parameters);

//                if(mFrameWidth != 0 && mFrameHeight != 0) {
//                    if (mPreviewWidth != mFrameWidth && mPreviewHeight != mFrameHeight) {
//                        //预览尺寸不同于目标尺寸，则需要裁剪
//                        initCroper();
//                    }
//                }

                //设置缓冲区
                mPreviewYuvBufferSize = mPreviewWidth * mPreviewHeight * 3 / 2;
                mYuvBuffer = new byte[mPreviewYuvBufferSize];
                mCamera.setPreviewCallbackWithBuffer(this);
                mCamera.addCallbackBuffer(mYuvBuffer);
                //相机开始预览
                if (surfaceTexture == null) {
                    surfaceTexture = new SurfaceTexture(0);
                    surfaceTexture.setOnFrameAvailableListener((surfaceTexture) -> {
                    });
                }
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    long mOldTimesStamp = 0;


    public void setPreviewCallback(CommonCallback callback){
        if(callback != null){
            this.callback = callback;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(callback != null){
            byte[] yuv = null;
            if(mIsCroper) {
                yuv = mCroper.crop(data);
                callback.apply(yuv);
            }else {
                callback.apply(data);
            }
        }

        long currentTimesStamp;
        int version = android.os.Build.VERSION.SDK_INT;
        currentTimesStamp = System.currentTimeMillis();
        long temp = currentTimesStamp - mOldTimesStamp;
        if(count < 100)
        log.write("TEST 1FI=" + temp);
        mOldTimesStamp = currentTimesStamp;

        if (null != mCamera) {
            mCamera.addCallbackBuffer(mYuvBuffer);
        }
        count++;
    }
    int count;
}
