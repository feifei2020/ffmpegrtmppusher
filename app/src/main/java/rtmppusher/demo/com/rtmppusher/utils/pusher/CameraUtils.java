package rtmppusher.demo.com.rtmppusher.utils.pusher;

import android.hardware.Camera;

/**
 * Created by Administrator on 2017/4/20 0020.
 * 主要做一些相机的额外操作,比如前置，后置，闪光灯，聚焦等
 */

public class CameraUtils {
    public enum CAMERA_FACING_ID {
        CAMERA_FACING_BACK,
        CAMERA_FACING_FRONT
    }
    private boolean isFlashOn = false;
    private int mCameraFacing = CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal();
    private VideoSource mCamera;

    public void setCamera(VideoSource camera){
        mCamera = camera;
    }

    //前置，后置切换
    public void switchFacing() {
        //1:前置,0:后置
        mCameraFacing = (CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal() == mCameraFacing) ? CAMERA_FACING_ID.CAMERA_FACING_BACK.ordinal() : CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal();
        if(mCamera != null){
            mCamera.setCameraFacing(mCameraFacing);
            mCamera.releaseCamera();
            mCamera.openCamera();
        }
    }

    public int getCameraFacing(){
        return mCameraFacing;
    }

    //打开,关闭闪光灯
    public boolean isFlashOn(){
        return isFlashOn;
    }

    public void switchFlash() {
        Camera camera = mCamera.getCamera();
        if(camera == null){
            return;
        }
        Camera.Parameters parameter = camera.getParameters();
        if (parameter.getFlashMode() == null) {
            // 判断是否支持闪光灯，部分机型在前置摄像头时不支持开启闪光灯操作
            // 不支持闪光灯.
            //ToastUtils.showShortToastSafe(app.getContext(), "不支持闪光灯");
        } else {
            if (isFlashOn) {
                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameter);
            } else {
                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameter);
            }
            isFlashOn = !isFlashOn;
        }
    }
}
