package rtmppusher.demo.com.rtmppusher.utils.pusher;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.Arrays;

import rtmppusher.demo.com.rtmppusher.app.app;

import static android.content.Context.CAMERA_SERVICE;

public class SrsCamera2 {

    private String mCameraId = "";
    private Size mPreviewSize;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private ImageReader mImageReader;
    private CameraManager cameraManager;
    private CommonCallback callback;
    private byte[] mBuffer;

    public SrsCamera2(){
        mImageReader = ImageReader.newInstance(640, 480,ImageFormat.YUV_420_888, 1);
        mCameraThread = new HandlerThread("CameraTextureViewThread");
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
        //获取摄像头的管理者CameraManager
        cameraManager = (CameraManager) app.getContext().getSystemService(CAMERA_SERVICE);
        mCameraId = getCameraId();
        mBuffer = new byte[640 * 480 * 3 / 2];
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera(){
        if (ActivityCompat.checkSelfPermission(app.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        try {
            cameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreView();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (mCameraDevice != null){
                mCameraDevice.close();
                camera.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (mCameraDevice !=null){
                mCameraDevice.close();
                camera.close();
                mCameraDevice = null;
            }
        }
    };

    public void open(){
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
        openCamera();
    }

    public void setPreviewCallback(CommonCallback callback){
        this.callback = callback;
    }

    public void release(){
        if (mCameraCaptureSession != null){
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public String getCameraId(){
        if (TextUtils.isEmpty(mCameraId)) {
            String frontCamId = "";
            String backCamId = "";
            try {
                //遍历所有摄像头
                for (String cameraId : cameraManager.getCameraIdList())
                {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        backCamId = cameraId;
                    } else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                        frontCamId = cameraId;
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if (!frontCamId.equals("")) {
                mCameraId = frontCamId;
            } else if (!backCamId.equals("")) {
                mCameraId = backCamId;
            } else {
                mCameraId = "";
            }
        }
        return mCameraId;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPreView(){
        Surface imageSurface = mImageReader.getSurface();
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(imageSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(imageSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureRequest = mCaptureRequestBuilder.build();
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest,null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            },mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i("app.log", "onImageAvailable");
            Image image = reader.acquireLatestImage();
            if(image == null){
                return;
            }
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer bufferY = planes[0].getBuffer();
            ByteBuffer bufferU = planes[1].getBuffer();
            ByteBuffer bufferV = planes[2].getBuffer();

            //把Y,U,V组合在一个byte数组合里
            int w = image.getWidth();
            int h = image.getHeight();
            int channelLen = w * h;
            ByteBuffer frame = ByteBuffer.allocate((int) (channelLen * 1.5));
            frame.put(bufferY); //Y
            /**
             * 在bufferU和bufferV的缓存大小为bufferY的1/2, 但是在YUV_420_888格式下,实际的有效数据为1/4
             * 而且存储的数据格式怪异,是各一byte存一个U值(V一样)
             */
            for (int r = 0; r < h / 2; ++r) {
                for (int c = 0; c < w; c+=2) { //各一个byte存一个U值和V值
                    frame.put(bufferU.get(r * w + c)); //U
                    frame.put(bufferV.get(r * w + c)); //V
                }
            }
            if(callback != null){
                callback.apply(frame.array());
            }
            image.close();
        }
    };
}
