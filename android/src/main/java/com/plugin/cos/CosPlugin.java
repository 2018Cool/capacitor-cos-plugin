package com.plugin.cos;

import android.Manifest;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.CosXmlSimpleService;
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;

import org.json.JSONException;

@NativePlugin()
public class CosPlugin extends Plugin {
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10001;

    private CosXmlSimpleService cosXmlService;
    private TransferManager transferManager;

    private void initCosService(JSObject signOptions) throws JSONException {
        String region = signOptions.getString("region");
        final String secretId = signOptions.getString("secretId"); //临时密钥 SecretId
        final String secretKey = signOptions.getString("secretKey"); //临时密钥 SecretKey
        final String sessionToken = signOptions.getString("sessionToken"); // 临时密钥 Token
        final long sliceSize = signOptions.getLong("sliceSize");
        boolean isHttps = signOptions.getBoolean("isHttps", false);
        final long startTime = signOptions.getLong("startTime");  //临时密钥有效起始时间戳
        final long expiredTime = signOptions.getLong("expiredTime");//临时密钥有效截止时间戳
        //初始化 config
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .isHttps(isHttps)  //设置 https 请求, 默认http请求
                .setRegion(region)
                .setDebuggable(true)
                .builder();
        //使用临时密钥初始化QCloudCredentialProvider
        QCloudCredentialProvider qCloudCredentialProvider = new BasicLifecycleCredentialProvider() {
            @Override
            protected QCloudLifecycleCredentials fetchNewCredentials() {
                return new SessionQCloudCredentials(secretId, secretKey, sessionToken, startTime, expiredTime);
            }
        };

        cosXmlService = new CosXmlSimpleService(getContext(), serviceConfig, qCloudCredentialProvider);

        TransferConfig transferConfig = new TransferConfig.Builder().setSliceSizeForUpload(sliceSize * 1024).build();
        transferManager = new TransferManager(cosXmlService, transferConfig);
    }

    private void requestPermissions() {

        if (!this.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            this.pluginRequestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void startUpload(String bucket,final String cosPath, String localPath, final PluginCall call) {
        final String cosUrl = call.getString("cosUrl","");
        // 开始上传，并返回生成的 COSXMLUploadTask
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(bucket, cosPath, localPath, null);


        // 设置上传进度监听
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(final long complete, final long target) {
                Log.i("progress", complete + "");
            }
        });

        // 设置结果监听
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                JSObject res = new JSObject();
                if(!cosUrl.equals("")){
                    res.put("url",cosUrl+"/"+cosPath);
                }else{
                    res.put("url",result.accessUrl);
                }
                call.success(res);

            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                call.error("fail", exception);
            }
        });
    }

    @PluginMethod()
    public void upload(PluginCall call) {
        JSObject signOptions = call.getObject("signOptions");
        try {
            initCosService(signOptions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestPermissions();
        String bucket = call.getString("bucket");
        String cosPath = call.getString("cosPath");
        String localPath = call.getString("localPath");
        if (localPath.startsWith("file://")) {
            localPath = localPath.replace("file://", "");
        }
        call.save();
        startUpload(bucket, cosPath, localPath, call);
    }
}
