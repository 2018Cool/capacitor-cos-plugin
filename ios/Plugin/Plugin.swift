import Foundation
import Capacitor
import QCloudCore
import QCloudCOSXML
/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(CosPlugin)
public class CosPlugin: CAPPlugin ,QCloudSignatureProvider{

    
    var signOptions:AnyObject!;
    var credentialFenceQueue:QCloudCredentailFenceQueue!;
    
    @objc func upload(_ call: CAPPluginCall) {
        self.signOptions = call.getObject("signOptions") as AnyObject
        let bucket = call.getString("bucket")!;
        let cosPath = call.getString("cosPath")!;
        var localPath = call.getString("localPath")!;
        localPath = localPath.replacingOccurrences(of: "file://", with: "");
        let transfer = self.initCosService();
//        self.credentialFenceQueue = QCloudCredentailFenceQueue();
//        self.credentialFenceQueue.delegate = self;
        self.startUpload(bucket:bucket, cosPath:cosPath, localPath:localPath, call:call,transfer:transfer);
        
    }
    public func signature(with fileds: QCloudSignatureFields!, request: QCloudBizHTTPRequest!, urlRequest urlRequst: NSMutableURLRequest!, compelete continueBlock: QCloudHTTPAuthentationContinueBlock!) {
        let secretId = signOptions["secretId"] ?? ""; //临时密钥 SecretId
        let secretKey = signOptions["secretKey"] ?? ""; //临时密钥 SecretKey
        let sessionToken = signOptions["sessionToken"] ?? ""; // 临时密钥 Token
        //        let sliceSize = signOptions["sliceSize"] ?? 1024;
        //        let isHttps = signOptions["isHttps"] ?? false;
        let startTime = signOptions["startTime"] ?? 0;  //临时密钥有效起始时间戳
        let expiredTime = signOptions["expiredTime"] ?? 0;//临时密钥有效截止时间戳
        
        let credential:QCloudCredential = QCloudCredential();
        credential.secretID = secretId;
        credential.secretKey = secretKey;
        credential.token = sessionToken;
        /*强烈建议返回服务器时间作为签名的开始时间，用来避免由于用户手机本地时间偏差过大导致的签名不正确*/
        credential.startDate = Date.init(timeIntervalSince1970: startTime as! TimeInterval);
        credential.experationDate = Date.init(timeIntervalSince1970: expiredTime as! TimeInterval);
        let creator:QCloudAuthentationV5Creator =  QCloudAuthentationV5Creator.init(credential: credential);
        continueBlock(creator.signature(forData: urlRequst), nil);
    }
    
    func initCosService()->QCloudCOSTransferMangerService{
        
        let region = signOptions["region"] ?? "";
        
        //初始化 config
        let config:QCloudServiceConfiguration = QCloudServiceConfiguration();
        config.signatureProvider = self;
        let endpoint:QCloudCOSXMLEndPoint = QCloudCOSXMLEndPoint();
        endpoint.regionName = region;//服务地域名称，可用的地域请参考注释
        config.endpoint = endpoint;
        //endpoint.useHTTPS = ((signOptions["isHttps"] ?? false) != nil);
        QCloudCOSXMLService.registerDefaultCOSXML(with: config);
        TACMTAConfig.getInstance()?.statEnable = false;
        return QCloudCOSTransferMangerService.registerDefaultCOSTransferManger(with: config);
       
    }
    
    func startUpload(bucket:String, cosPath:String, localPath:String, call:CAPPluginCall,transfer:QCloudCOSTransferMangerService){
        let put:QCloudCOSXMLUploadObjectRequest<AnyObject> = QCloudCOSXMLUploadObjectRequest();
        let url:NSURL = NSURL(fileURLWithPath:localPath);
        put.object = cosPath;
        put.bucket = bucket;
        put.body =  url;
        put.sliceSize = (signOptions["sliceSize"] ?? 1024) as! UInt;
        put.setFinish({(result:QCloudUploadObjectResult?,error:Error?)->(Void) in
            call.success(["url":result?.location ?? ""]);
        })
        
        put.sendProcessBlock={(bytesSent:Int64, totalBytesSent:Int64, totalBytesExpectedToSend:Int64)->(Void) in
            NSLog("upload \(bytesSent) totalSend \(totalBytesSent) aim \(totalBytesExpectedToSend)");
        }
        transfer.uploadObject(put);
    }
}

