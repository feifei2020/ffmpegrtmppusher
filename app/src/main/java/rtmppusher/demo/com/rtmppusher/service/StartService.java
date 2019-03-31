package rtmppusher.demo.com.rtmppusher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import rtmppusher.demo.com.rtmppusher.utils.log.log;

public class StartService extends Service {
    Thread thread;

    public StartService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    /**
     * 38      * 每次通过startService()方法启动Service时都会被回调。
     * 39      * @param intent
     * 40      * @param flags
     * 41      * @param startId
     * 42      * @return
     * 43
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void registerReceiver() {
        //此处添加启动服务要执行的操作代码
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        thread = new Thread(() -> {
            while (!thread.isInterrupted()) {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onCreate() {
        registerReceiver();
    }
}
