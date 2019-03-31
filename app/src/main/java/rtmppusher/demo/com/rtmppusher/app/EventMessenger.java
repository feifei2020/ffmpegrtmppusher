package rtmppusher.demo.com.rtmppusher.app;

/**
 * Created by Administrator on 2018/1/9 0009.
 */

public class EventMessenger {

    public int from;
    public Object obj;

    public EventMessenger(){
    }

    //不传参数
    public EventMessenger(int from) {
        this.from = from;
        obj = null;
    }

    //传递参数
    public EventMessenger(int from, Object obj) {
        this.from = from;
        this.obj = obj;
    }

}
