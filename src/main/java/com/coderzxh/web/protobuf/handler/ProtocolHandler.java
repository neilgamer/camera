package com.coderzxh.web.protobuf.handler;

import cn.jpush.api.report.MessageDetailResult;
import com.coderzxh.common.util.ComUtil;
import com.coderzxh.common.util.DateTimeUtil;
import com.coderzxh.common.util.JPushUtils;
import com.coderzxh.persistence.entity.Detection;
import com.coderzxh.persistence.entity.Device;
import com.coderzxh.service.base.IDetectionService;
import com.coderzxh.service.base.IDeviceService;
import com.coderzxh.web.controller.DetectionController;
import com.coderzxh.web.protobuf.codec.*;
import com.coderzxh.web.protobuf.entity.*;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * @author Qingxi
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class ProtocolHandler extends ChannelDuplexHandler {

//    @Getter
//    private static final ProtocolHandler instance = new ProtocolHandler();

    private static ConcurrentHashMap<String, Channel> sessionChannelMap = new ConcurrentHashMap<String, Channel>();
    private static ConcurrentHashMap<String, String> channelIdSerialNumMap = new ConcurrentHashMap<String, String>();

    @Autowired
    private IDeviceService iDeviceService;
    @Autowired
    private IDetectionService iDetectionService;
    @Value("${img-upload.dir}")
    private String imgUploadDir;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try{
        GBT32960Message message = (GBT32960Message) msg;
        FrameHeader header = message.getHeader();
        switch (header.getRequestType()) {
            case LOGIN:
                //保存，为了主动发送消息
                Channel channel = ctx.channel();
                channelIdSerialNumMap.put(channel.id().asLongText(), header.getSerialNum());
                sessionChannelMap.put(header.getSerialNum(), channel);
                Login login = (Login) message.getPayload();
                loginResponse(ctx, header, ResponseTag.SUCCESS, login);
                break;
            case DEVICE_CLOCK_CORRECT:
                deviceClockCorrectResponse(ctx,header,ResponseTag.SUCCESS);
                break;
            case HEARTBEAT:
                heartBeatResponse(ctx,header,ResponseTag.SUCCESS);
                break;
            case INFO_REPORT:
                log.info("消息上报信息接收");
                InfoReport infoReport = (InfoReport) message.getPayload();
                infoReportResponse(ctx,header,ResponseTag.SUCCESS, infoReport);
                break;
            case INFO_QUERY:
                log.info("消息查询，服务器接受返回信息并保存");
                InfoQuery infoQuery = (InfoQuery) message.getPayload();
                infoQueryResponse(ctx,header,ResponseTag.SUCCESS, infoQuery);
                break;
                default:
                    ctx.fireChannelRead(msg);
        }
        }catch (Exception e){
            log.error("",e);
        }
    }
    //参数查询
    private void infoQueryResponse(ChannelHandlerContext ctx, FrameHeader header, ResponseTag tag, InfoQuery infoQuery) {
        Device device = iDeviceService.UpdateDevice(infoQuery, header);
        log.info("{} 设备参数查询返回信息保存成功!", header.getSerialNum());
//        ctx.writeAndFlush(null);
    }
    //设备上报
    private void infoReportResponse(ChannelHandlerContext ctx, FrameHeader header, ResponseTag tag, InfoReport infoReport) {
        List<Info> infoList1 = infoReport.getInfoList();
        InfoReport.Builder builder = InfoReport.newBuilder();
        for (int i = 0; i < infoList1.size(); i++) {
            Info info = infoList1.get(i);
            Info build = Info.newBuilder().setInfoType(info.getInfoType()).build();
            builder.addInfo(i,build);
        }
        PlatformMessage message = PlatformMessage.newBuilder()
                .setDeviceSerialNum(header.getSerialNum())
                .setCommandTag(RequestType.INFO_REPORT.getValue())
                .setResponseTag(tag.getValue())
                .setInfoReport(builder.build())
                .build();
        try {
            List<Info> infoList = infoReport.getInfoList();
            for(Info info : infoList){
                if(info.getInfoType() == (int)(InfoReportType.MOTION_DETECTION.getValue())){
                    MotionDetection mo = info.getMotionDetection();
                    int pictureLength = mo.getPictureLength();
                    byte[] bytes = new byte[pictureLength];
                    mo.getPicture().copyTo(bytes,0);
                    String nowStr = DateTimeUtil.formatDateTimetoString(new Date(), DateTimeUtil.FMT_yyyyMMdd);
                    nowStr = nowStr.replaceAll("-", Matcher.quoteReplacement(File.separator));
                    String fileName = Long.toString(new Date().getTime()) + (int)(Math.random() * 100) +".jpg";
                    String path = nowStr+File.separator+fileName;
                    byte2image(bytes, imgUploadDir+path);
                    iDetectionService.insertNewDetection(mo.getPictureName(), imgUploadDir+path,header.getSerialNum());
                    log.info("设备信息上报("+InfoReportType.MOTION_DETECTION+")收到,"
                            +"设备序列号： " + header.getSerialNum());
                }
            }
        }catch (Exception e){
            log.error("", e);
        }
        ctx.writeAndFlush(message);
    }

    //byte数组到图片
    public void byte2image(byte[] data, String path){
        if(data.length<3||path.equals("")) return;
        try{
            String substring = path.substring(0, path.lastIndexOf(File.separator));
            File fileDir = new File(substring);
            if(!fileDir.exists()){
                fileDir.mkdirs();
            }
            File file = new File(path);
            if(!file.exists()){
                file.createNewFile();
            }
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
//            log.info("Make Picture success,Please find image in " + path);
        } catch(Exception ex) {
            log.error("Exception: " + ex);
        }
    }

    /**
     * 返回 登录成功应答
     * @param ctx ChannelHandlerContext
     * @param header 头部
     * @param tag 登录成功标志
     * @param loginRequest 登入消息
     */
    private void loginResponse(ChannelHandlerContext ctx, FrameHeader header, ResponseTag tag, Login loginRequest) {
        Login build = null;
        Device device = iDeviceService.updateDevice(loginRequest, header);
        if(device !=null) {
            build = loginRequest.newBuilder()
                    .setDeviceId(device.getP2pId())
                    .setP2PServerAddress(device.getP2pServerAddress())
                    .setFileServerAddress("null")
                    .setCommunicationServerAddress("null")
                    .build();
            log.info("{} 登入成功!", header.getSerialNum());
        }else{
            build = loginRequest.newBuilder().build();
            tag = ResponseTag.FAILED;
            log.info("{} 登入失败!", header.getSerialNum());
        }
        PlatformMessage message = PlatformMessage.newBuilder()
                .setDeviceSerialNum(header.getSerialNum())
                .setCommandTag(RequestType.LOGIN.getValue())
                .setResponseTag(tag.getValue())
                .setLogin(build)
                .build();
        ctx.writeAndFlush(message);
    }

    //设备校时应答
    private void deviceClockCorrectResponse(ChannelHandlerContext ctx, FrameHeader header, ResponseTag tag) {
        DeviceClockCorrect build = DeviceClockCorrect.newBuilder().setTime(new Date().getTime()).build();
        PlatformMessage message = PlatformMessage.newBuilder()
                .setDeviceSerialNum(header.getSerialNum())
                .setCommandTag(RequestType.DEVICE_CLOCK_CORRECT.getValue())
                .setResponseTag(tag.getValue())
                .setDeviceClockCorrect(build)
                .build();
        log.info("设备校时成功");
        log.info("设备序列号： " + header.getSerialNum());
        ctx.writeAndFlush(message);
    }

    private void heartBeatResponse(ChannelHandlerContext ctx, FrameHeader header, ResponseTag tag) {
        PlatformMessage message = PlatformMessage.newBuilder()
                .setDeviceSerialNum(header.getSerialNum())
                .setCommandTag(RequestType.HEARTBEAT.getValue())
                .setResponseTag(tag.getValue())
                .setHeartbeat(Heartbeat.newBuilder().build())
                .build();
        log.info("心跳检测，"+"设备序列号： " + header.getSerialNum());
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        log.info("已上线：客户端[ip:" + clientIp + "]");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        String s = channelIdSerialNumMap.get(ctx.channel().id().asLongText());
        log.info("已下线：客户端[ip:" + clientIp + ", serialNum: "+s+"]");
        super.channelInactive(ctx);
        channelIsActive(s);
    }

    public String sendInfo2Client(PlatformMessage message) throws Exception {
        String deviceSerialNum = message.getDeviceSerialNum();
        log.info("主动发送消息给客户端 序列号： " + deviceSerialNum);

        Channel channel = sessionChannelMap.get(deviceSerialNum);
        if(channel != null) {
            boolean active = channel.isActive();
            log.info("状态： " +  active);
            if(active) {
                channel.writeAndFlush(message);
                log.info("主动发送消息给客户端 发送完成");
                return "ok";
            }else{
                log.info("主动发送消息给客户端 连接断开");
                return "连接断开";
            }
        }else{
            log.info("主动发送消息给客户端 未建立连接");
            return "未建立连接";
        }
    }

    public PlatformMessage createMessage()throws Exception{
            return null;
    }

    public Boolean channelIsActive(String deviceSerialNum){
        if(ComUtil.isEmpty(deviceSerialNum)){
            return false;
        }
        Boolean b = null;
        Channel channel = sessionChannelMap.get(deviceSerialNum);
        if(channel != null) {
            boolean active = channel.isActive();
            b = active;
        }else{
            b = false;
        }
        iDeviceService.updateDeviceState(deviceSerialNum, b);
        return b;
    }

    /**
     * 关闭所有硬件
     * @return
     */
    public Boolean shutdownAllChannel(){
        Boolean b = false;
        for(Channel value : sessionChannelMap.values()){
            String s = channelIdSerialNumMap.get(value.id().asLongText());
            iDeviceService.updateDeviceState(s, b);
        }
        return !b;
    }

    public static void main(String[] args) {
        String k = "E:\\zxh\\coding\\protobuf\\img\\2020\\04\\30\\158823631496743.jpg";
        System.out.println(k.lastIndexOf(File.separator));
        String substring = k.substring(0, k.lastIndexOf(File.separator));
        System.out.println(substring);

    }
}
