package com.coderzxh.web.protobuf.codec;

import com.coderzxh.web.protobuf.entity.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import static com.coderzxh.web.protobuf.codec.GBT32960Message.*;


/**
 * @author Qingxi
 */

@Slf4j
public class GBT32960Decoder extends ReplayingDecoder<Void> {

    public GBT32960Decoder() {
        super();
    }

    private FrameHeader frameHeader;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out){
        try {
            log.debug("gbt32960 inbound");
            log.info("消息开始---------------------------------------------");
            log.info("帧消息: {}", ByteBufUtil.hexDump(internalBuffer()));

            if (in.readShort() != START_SYMBOL) {
                in.skipBytes(actualReadableBytes());
                ctx.close();
                return;
            }

            int startIndex = in.readerIndex();
            frameHeader = decodeFrameHeader(in);

            //校验位同样是0即可
            byte checkCode = (byte) 0x00;
            int payloadLength = frameHeader.getPayloadLength();
//        checkpoint();
            int i = in.readerIndex();
            log.info("header采集后的指针： " + i + "长度：" + payloadLength);
            log.info(Integer.toString(in.writerIndex()));
            byte checkCodeInMsg = in.getByte(i + payloadLength);
            ByteBuf byteBuf = in.readBytes(payloadLength + 1);
            log.info("ok");

            if (checkCode != checkCodeInMsg) {
                log.info("消息校验位验证失败: {} vs {}", String.format("%02X", checkCode),
                        String.format("%02X", checkCodeInMsg));
                return;
            }

            Object payload = decodePayload(byteBuf, frameHeader);
            GBT32960Message message = GBT32960Message.builder()
                    .header(frameHeader)
                    .payload(payload)
                    .build();
            out.add(message);
        }catch (Exception e){
            log.error("",e);
        }
    }

}
