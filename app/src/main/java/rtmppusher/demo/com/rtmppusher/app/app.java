package rtmppusher.demo.com.rtmppusher.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.blankj.utilcode.util.Utils;

/**
 * Created by Administrator on 2018/1/18 0018.
 */

public class app extends Application {

    private static app mInstance = null;
    public static String dbFileName = "application.db"; //数据库
    public static int bootstart = 0;
    public static boolean isDestroyMain = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //AppUtils组件
        Utils.init(this);
        bootstart = 0;
        isDestroyMain = false;
    }

    public static Context getContext() {
        return mInstance;
    }

    public void registerActivity() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                //app.log("start activity:" + activity.getClass().getName());
                //ActivityManagerUtils.getInstance().addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                //ActivityManagerUtils.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
//                ActivityManagerUtil.getInstance().setPreviousActivity(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                //ActivityManagerUtils.getInstance().removeActivity(activity);
            }
        });
    }
}

