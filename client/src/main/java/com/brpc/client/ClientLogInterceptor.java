package com.brpc.client;

import com.mornsnow.shared.common.basic.ThreadContext;
import com.mornsnow.starter.log.LogUtil;
import com.mornsnow.starter.log.QcLog;
import io.grpc.*;

import java.util.UUID;

import static com.mornsnow.starter.log.Const.THREAD_KEY_LOG_ID;

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


public class ClientLogInterceptor implements ClientInterceptor {

    private static final QcLog log = LogUtil.getLogger(ClientLogInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> clientCall = next.newCall(method, callOptions);
        return new ForwardingClientCall<ReqT, RespT>() {
            @Override
            protected ClientCall<ReqT, RespT> delegate() {
                return clientCall;
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                Object companyIdObj = ThreadContext.get("companyId");
                if (companyIdObj != null) {
                    String companyId = (String) companyIdObj;
                    Metadata.Key<String> companyIdKey = Metadata.Key.of("companyId", Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(companyIdKey, companyId);
                }
                Metadata.Key<String> logId = Metadata.Key.of("qc-logid", Metadata.ASCII_STRING_MARSHALLER);
                headers.put(logId, getLogId());
                Metadata.Key<String> spanId = Metadata.Key.of("span-id", Metadata.ASCII_STRING_MARSHALLER);
                headers.put(spanId, QcLog.getSpanId());

                super.start(responseListener, headers);

            }
        };
    }

    private String getLogId() {

        Object logIdObj = ThreadContext.get(THREAD_KEY_LOG_ID);

        if (logIdObj == null || logIdObj.toString().equals("")) {
            //rpc开头以区分接口既为rpc接口又是http接口时  调用方式为rpc
            String logId = "rpc" + UUID.randomUUID().toString().replace("-", "");
            ThreadContext.push(THREAD_KEY_LOG_ID, logId);
            return logId;
        }
        return logIdObj.toString();
    }

}
