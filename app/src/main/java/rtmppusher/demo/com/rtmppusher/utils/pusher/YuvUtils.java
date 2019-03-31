package rtmppusher.demo.com.rtmppusher.utils.pusher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/4/12 0012.
 */

public class YuvUtils {

    public static void NV21ToI420(byte[] dst, byte[] src, int width, int height) {
        int total = width * height;
        //byte[] des = new byte[width * height * 3 / 2];
        ByteBuffer bufferY = ByteBuffer.wrap(dst, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(dst, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(dst, total + total / 4, total / 4);

        bufferY.put(src, 0, total);

        for (int i = total; i < src.length; i += 2) {
            bufferV.put(src[i]);
            bufferU.put(src[i + 1]);
        }
        try {
            //out.write(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //NV21转换成bitmap
    public static Bitmap toBitmap(byte[] data, int previewWidth, int previewHeight) {
        try {
            byte[] rawImage;
            YuvImage image = new YuvImage(data, ImageFormat.NV21, previewWidth, previewHeight, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight), 80, stream);
                rawImage = stream.toByteArray();
                //将rawImage转换成bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bmp = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
                stream.close();
                return bmp;
            }
        } catch (Exception ex) {
            //Log.e("Sys","Error:"+ex.getMessage());
        }
        return null;
    }

    //逆时针旋转90度
    public static void setRotation(byte[] dst, byte[] src, int width, int height) {
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (width != nWidth || height != nHeight) {
            nWidth = width;
            nHeight = height;
            wh = width * height;
            uvHeight = height >> 1; //uvHeight = height / 2
        }

        //旋转Y
        int k = 0;
        for (int i = 0; i < width; i++) {
            int nPos = width - 1;
            for (int j = 0; j < height; j++) {
                dst[k] = src[nPos - i];
                k++;
                nPos += width;
            }
        }

        for (int i = 0; i < width; i += 2) {
            int nPos = wh + width - 1;
            for (int j = 0; j < uvHeight; j++) {
                dst[k] = src[nPos - i - 1];
                dst[k + 1] = src[nPos - i];
                k += 2;
                nPos += width;
            }
        }

        return;
    }

    public static void yuv420spRotate90(byte[] des, byte[] src,
                                        int width, int height) {
        int wh = width * height;
        int k = 0;
        for(int i = 0; i < width; i++) {
            for(int j = height - 1; j >= 0; j--)
            {
                des[k] = src[width * j + i];
                k++;
            }
        }
        for(int i = 0; i < width; i += 2) {
            for(int j = height / 2 - 1; j >= 0; j--)
            {
                des[k] = src[wh+ width * j + i];
                des[k+1] = src[wh + width * j + i + 1];
                k+=2;
            }
        }
    }

    public static byte[] rotateYUV420Degree270_2(byte[] data, int imageWidth, int imageHeight){
        byte[] yuv =new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i =0;
        for(int x = imageHeight-1;x >=0;x--){
            for(int y =0;y < imageWidth;y++){
                yuv[i]= data[y*imageHeight+x]; 		            i++;
            }   }// Rotate the U and V color components  	i = imageWidth*imageHeight;
        for(int x = imageWidth-1;x >0;x=x-2){
            for(int y =0;y < imageHeight/2;y++){ 		       yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)]; 		         i++; 		       yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+x]; 		            i++;
            }
        }
        return yuv;
    }

    //将Bitmap数据转成NV21数据
    public static byte[] getNV21(int width, int height, Bitmap scaled) {
        int[] argb = new int[width * height];
        scaled.getPixels(argb, 0, width, 0, 0, width, height);
        byte[] yuv = new byte[width * height * 3 / 2];
        encodeYUV420SP(yuv, argb, width, height);
        //scaled.recycle();
        return yuv;
    }

    /**
     * 将bitmap里得到的argb数据转成yuv420sp格式
     * 这个yuv420sp数据就可以直接传给MediaCodec,通过AvcEncoder间接进行编码
     *
     * @param yuv420sp 用来存放yuv429sp数据
     * @param argb     传入argb数据
     * @param width    图片width
     * @param height   图片height
     */
    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    /*
    * 旋转图片
    * @param angle
    * @param bitmap
    * @return Bitmap
    */
    public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //NV21镜像
    public static void Mirror(byte[] src, int w, int h) { //src是原始yuv数组
        int i;
        int index;
        byte temp;
        int a, b;
        //mirror y
        for (i = 0; i < h; i++) {
            a = i * w;
            b = (i + 1) * w - 1;
            while (a < b) {
                temp = src[a];
                src[a] = src[b];
                src[b] = temp;
                a++;
                b--;
            }
        }

        // mirror u and v
        index = w * h;
        for (i = 0; i < h / 2; i++) {
            a = i * w;
            b = (i + 1) * w - 2;
            while (a < b) {
                temp = src[a + index];
                src[a + index] = src[b + index];
                src[b + index] = temp;

                temp = src[a + index + 1];
                src[a + index + 1] = src[b + index + 1];
                src[b + index + 1] = temp;
                a += 2;
                b -= 2;
            }
        }
    }

    //I420镜像
    public static void MirrorI420(byte[] src, int w, int h) { //src是原始yuv数组
        int i;
        int index;
        byte temp;
        int a, b;
        //mirror y
        for (i = 0; i < h; i++) {
            a = i * w;
            b = (i + 1) * w - 1;
            while (a < b) {
                temp = src[a];
                src[a] = src[b];
                src[b] = temp;
                a++;
                b--;
            }
        }
        //mirror u
        index = w * h;//U起始位置
        for (i = 0; i < h / 2; i++) {
            a = i * w / 2;
            b = (i + 1) * w / 2 - 1;
            while (a < b) {
                temp = src[a + index];
                src[a + index] = src[b + index];
                src[b + index] = temp;
                a++;
                b--;
            }
        }
        //mirror v
        index = w * h / 4 * 5;//V起始位置
        for (i = 0; i < h / 2; i++) {
            a = i * w / 2;
            b = (i + 1) * w / 2 - 1;
            while (a < b) {
                temp = src[a + index];
                src[a + index] = src[b + index];
                src[b + index] = temp;
                a++;
                b--;
            }
        }
    }

    public static byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate and mirror the Y luma
        int i = 0;
        int maxY = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            maxY = imageWidth * (imageHeight - 1) + x * 2;
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[maxY - (y * imageWidth + x)];
                i++;
            }
        }
        // Rotate and mirror the U and V color components
        int uvSize = imageWidth * imageHeight;
        i = uvSize;
        int maxUV = 0;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize;
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)];
                i++;
                yuv[i] = data[maxUV - (y * imageWidth + x)];
                i++;
            }
        }
        return yuv;
    }
}
