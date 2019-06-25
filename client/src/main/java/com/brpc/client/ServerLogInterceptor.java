package com.brpc.client;

import com.mornsnow.shared.common.basic.ThreadContext;
import io.grpc.*;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import static com.mornsnow.starter.log.Const.*;

/**
 * <strong>描述：</strong> <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 *
 * @author jianyang 2017/6/20
 */
public class ServerLogInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String companyId = metadata.get(Metadata.Key.of("companyId", Metadata.ASCII_STRING_MARSHALLER));
        if (!StringUtils.isEmpty(companyId)) {
            ThreadContext.push("companyId", companyId);
        }
        String logId = metadata.get(Metadata.Key.of("x-b3-traceid", Metadata.ASCII_STRING_MARSHALLER));
        String parentId = metadata.get(Metadata.Key.of("x-b3-spanid", Metadata.ASCII_STRING_MARSHALLER));
        if (logId == null) {
            logId = metadata.get(Metadata.Key.of("qc-logid", Metadata.ASCII_STRING_MARSHALLER));
        }
        if (parentId == null) {
            parentId = metadata.get(Metadata.Key.of("span-id", Metadata.ASCII_STRING_MARSHALLER));
        }
        String clientIp = serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString();
        ThreadContext.push(THREAD_KEY_LOG_ID, logId);
        ThreadContext.push(THREAD_KEY_CLIENT, clientIp.replace("/", "").split(":")[0]);
        if (parentId != null) {
            ThreadContext.push(THREAD_KEY_PARENT_ID, parentId);
        }
        ThreadContext.push(THREAD_KEY_SPAN_ID, initSpanId());
        ThreadContext.push(THREAD_KEY_TYPE, THREAD_KEY_TYPE_GRPC);

        return serverCallHandler.startCall(serverCall, metadata);
    }

    private String initSpanId() {
        //r开头以区分接口既为rpc接口又是http接口时  调用方式为rpc
        String id = "rpc" + UUID.randomUUID().toString().replace("-", "");
        ThreadContext.push(THREAD_KEY_SPAN_ID, id);
        return id;
    }
}
