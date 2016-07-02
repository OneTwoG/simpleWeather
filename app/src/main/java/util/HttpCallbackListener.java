package util;

/**
 * 定义一个接口，用来回调服务返回的结果
 * Created by T on 2016/4/7.
 */
public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);
}
