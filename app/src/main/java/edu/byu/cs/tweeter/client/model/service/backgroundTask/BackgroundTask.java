//package edu.byu.cs.tweeter.client.model.service.backgroundTask;
//
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import java.util.List;
//
//import edu.byu.cs.tweeter.model.domain.Status;
//
//public abstract class BackgroundTask implements Runnable {
//    private static final String LOG_TAG = "BackgroundTask";
//    public static final String SUCCESS_KEY = "success";
//    public static final String MESSAGE_KEY = "message";
//    public static final String EXCEPTION_KEY = "exception";
//
//    /**
//     * Message handler that will receive task results.
//     */
//    private Handler messageHandler;
//
//    public BackgroundTask(Handler messageHandler) { this.messageHandler = messageHandler; }
//
//    @Override
//    public void run() {
//        try {
//            runTask();
//            sendSuccessMessage();
//
//        } catch (Exception ex) {
//            Log.e(LOG_TAG, ex.getMessage(), ex);
//            sendExceptionMessage(ex);
//        }
//    }
//
//    protected abstract void runTask();
//
//    private void sendSuccessMessage() {
//        Bundle msgBundle = createBundle(true);
//        msgBundle.putBoolean(SUCCESS_KEY, true);
//        loadMessageBundle(msgBundle);
//
//        Message msg = Message.obtain();
//        msg.setData(msgBundle);
//
//        messageHandler.sendMessage(msg);
//    }
//
//    @NonNull
//    private Bundle createBundle(boolean value) {
//
//    }
//
//    protected abstract void loadMessageBundle(Bundle msgBundle);
//
//
//}
