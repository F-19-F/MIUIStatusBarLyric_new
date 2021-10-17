package com.byyang.choose;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.activity.result.ActivityResult;
import androidx.annotation.CallSuper;
import androidx.annotation.ContentView;
import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;


public class ChooseFileUtils {
    private static final Context mContext = ContextUtils.getContext();
    //private Intent mIntent;
    private ChooseListener chooseListener;
    private final ActivityResultLauncher activityResultLauncher;
    //private ActivityResultLauncher<Intent> activityResult;

    @ContentView
    public ChooseFileUtils(Activity activity) {
        this(new ActivityResultLauncher(activity));
    }

    @ContentView
    public ChooseFileUtils(Fragment fragment) {
        this(new ActivityResultLauncher(fragment));
    }

    private ChooseFileUtils(ActivityResultLauncher activityResultLauncher) {
        this.activityResultLauncher = activityResultLauncher.registerForActivityResult(result -> activityResult(chooseListener, result));
    }


    private static void activityResult(ChooseListener chooseListener, ActivityResult result) {
        if (result == null) {
            return;
        }
        int resultCode = result.getResultCode();
        chooseListener.onFinish();
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        Intent intent = result.getData();
        if (intent == null) {
            chooseListener.onFailed(new IOException("回调数据出现异常！\n回调返回码：" + resultCode));
        } else {
            String filePath = getDocumentPath(intent.getData());
            if (filePath != null) {
                chooseListener.onSuccess(filePath, intent.getData(), intent);
            } else {
                chooseListener.onFailed(new IOException("解密文件路径出错了！"));
            }
        }

    }

    public void chooseFolder(ChooseListener chooseListener) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        this.chooseListener = chooseListener;
        this.activityResultLauncher.launch(intent);
    }

    /*public void unregister() {
        activityResult.unregister();
    }*/

    public static abstract class ChooseListener {
        @CallSuper
        public void onFinish() {


        }

        /**
         * 只有单选的时候才能用，其他时候为null！
         */
        @CallSuper
        public void onSuccess(String filePath, Uri uri, Intent intent) {


        }

        @CallSuper
        public void onFailed(Exception e) {

        }

    }


    /**
     * @param uri
     * @return 路径解密，自己实现
     */
    private static String getDocumentPath(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(mContext, uri);
        assert documentFile != null;
//        return Environment.getExternalStorageDirectory() + "/" + documentFile.getUri().getPath().split(":")[2];
        String[] pathList = documentFile.getUri().getPath().split(":");

        if (pathList[pathList.length - 1].equals("/document/primary")) {
            return Environment.getExternalStorageDirectory() + "/";
        }
        return Environment.getExternalStorageDirectory() + "/" + pathList[pathList.length - 1] + "/";

    }

}
