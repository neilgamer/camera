package com.coderzxh.common.util;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jiguang.common.resp.DefaultResult;
import cn.jpush.api.JPushClient;
import cn.jpush.api.device.TagAliasResult;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class JPushUtils {
    private static final String appKey = "";
    private static final String masterSecret = "";
    private static final String TAG1 = "tag1";
    private static final String ALIAS1 = "alias1";
    private static final String ALIAS2 = "alias2";

    private static JPushClient jpushClient = new JPushClient(masterSecret, appKey);


    public static TagAliasResult getDeviceTagAlias(String registrationId) {
        try {
            TagAliasResult result = jpushClient.getDeviceTagAlias(registrationId);
            log.info(result.alias);
            log.info(result.tags.toString());
            return result;

        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);

        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. "
                    +"HTTP Status: " + e.getStatus()
                    +"Error Code: " + e.getErrorCode()
                    +"Error Message: " + e.getErrorMessage(), e);
        }
        return null;
    }


    public static void updateDeviceTagAlias(String registrationId, String alias,
                                                      Set<String> tagsToAdd, Set<String> tagsToRemove) {
        try {
            DefaultResult defaultResult = jpushClient.updateDeviceTagAlias(registrationId, alias, tagsToAdd, tagsToRemove);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);

        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. "
                    +"HTTP Status: " + e.getStatus()
                    +"Error Code: " + e.getErrorCode()
                    +"Error Message: " + e.getErrorMessage(), e);
        }
    }

    public static void sendPush(PushPayload payload) {
        ClientConfig clientConfig = ClientConfig.getInstance();
        final JPushClient jpushClient = new JPushClient(masterSecret, appKey, null, clientConfig);
        try {
            PushResult result = jpushClient.sendPush(payload);
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. "
                    +"Sendno: " + payload.getSendno(), e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. "
                    +"HTTP Status: " + e.getStatus()
                    +"Error Code: " + e.getErrorCode()
                    +"Error Message: " + e.getErrorMessage()
                    +"Msg ID: " + e.getMsgId()
                    +"Sendno: " + payload.getSendno(), e);
        }
    }

    public static void sendPush4Detection(String tag) {
        List<String> tags = new ArrayList<>();
        tags.add(tag);
        PushPayload payload = buildSimplePushObject_android_and_ios("您的设备发出了主动侦测预警，请打开APP查看侦测照片", "主动侦测预警", tags);
        sendPush(payload);
    }

    public static PushPayload buildSimplePushObject_android_and_ios(String content, String Atitle, List<String> tags) {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.tag(tags))
                .setNotification(Notification.newBuilder()
                        .setAlert(content)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setTitle(Atitle).build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .incrBadge(1).build())
                        .build())
                .build();
    }
}
