package com.cyl.wechat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatPayData {

    /** 商户号 */
    public static String appId;
    public static String secret;
    public static String merchantId;
    /** 商户API私钥路径 */
    public static String privateKeyPath;
    /** 商户证书序列号 */
    public static String merchantSerialNumber;
    /** 商户APIV3密钥 */
    public static String apiV3key;
    public static String notifyUrl;

    public void setAppId(String appId) {
        WechatPayData.appId = appId;
    }

    public void setSecret(String secret) {
        WechatPayData.secret = secret;
    }

    public void setMerchantId(String merchantId) {
        WechatPayData.merchantId = merchantId;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        WechatPayData.privateKeyPath = privateKeyPath;
    }

    public void setMerchantSerialNumber(String merchantSerialNumber) {
        WechatPayData.merchantSerialNumber = merchantSerialNumber;
    }

    public void setApiV3key(String apiV3key) {
        WechatPayData.apiV3key = apiV3key;
    }

    public void setNotifyUrl(String notifyUrl) {
        WechatPayData.notifyUrl = notifyUrl;
    }
}
