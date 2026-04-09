package com.spy.server.utils;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teaopenapi.models.Config;
import com.spy.server.config.AliyunSmsProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class AliyunSmsUtil {

    private static final int CODE_LENGTH = 6;

    @Resource
    private AliyunSmsProperties aliyunSmsProperties;

    private Client client;

    @PostConstruct
    public void init() throws Exception {
        validateClientConfig();
        Config config = new Config()
                .setAccessKeyId(aliyunSmsProperties.getAccessKeyId())
                .setAccessKeySecret(aliyunSmsProperties.getAccessKeySecret());
        config.setEndpoint(aliyunSmsProperties.getEndpoint());
        this.client = new Client(config);
    }

    public String generateCode() {
        return RandomStringUtils.randomNumeric(CODE_LENGTH);
    }

    public long getCodeTtlSeconds() {
        return (long) getValidMinutes() * 60;
    }

    public void sendSms(String phone, String code) throws Exception {
        validateTemplateConfig();
        long ttlSeconds = getCodeTtlSeconds();
        String outId = UUID.randomUUID().toString().replace("-", "");
        String templateParam = String.format("{\"code\":\"%s\",\"min\":\"%d\"}", code, getValidMinutes());

        log.info("Preparing Aliyun SMS request. phone={}, code={}, validMinutes={}, ttlSeconds={}, templateCode={}, outId={}",
                phone, code, getValidMinutes(), ttlSeconds, aliyunSmsProperties.getTemplateCode(), outId);

        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setSignName(aliyunSmsProperties.getSignName())
                .setTemplateCode(aliyunSmsProperties.getTemplateCode())
                .setTemplateParam(templateParam)
                .setValidTime((long) ttlSeconds)
                .setOutId(outId);

        SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request);
        if (response == null || response.getBody() == null) {
            throw new RuntimeException("Aliyun SMS returned an empty response.");
        }

        String bizCode = response.getBody().getCode();
        String message = response.getBody().getMessage();
        Boolean success = response.getBody().getSuccess();
        String bizId = response.getBody().getModel() == null ? null : response.getBody().getModel().getBizId();
        String responseOutId = response.getBody().getModel() == null ? null : response.getBody().getModel().getOutId();
        String requestId = response.getBody().getModel() == null ? null : response.getBody().getModel().getRequestId();

        log.info("Aliyun Dypns SMS response. phone={}, code={}, success={}, code={}, message={}, requestId={}, bizId={}, outId={}",
                phone, code, success, bizCode, message, requestId, bizId, responseOutId);

        if (!"OK".equalsIgnoreCase(bizCode)) {
            throw new RuntimeException("SMS send failed: " + bizCode + " - " + message);
        }
    }

    private int getValidMinutes() {
        Integer validMinutes = aliyunSmsProperties.getValidMinutes();
        if (validMinutes == null || validMinutes <= 0) {
            return 5;
        }
        return validMinutes;
    }

    private void validateClientConfig() {
        if (StringUtils.isAnyBlank(
                aliyunSmsProperties.getAccessKeyId(),
                aliyunSmsProperties.getAccessKeySecret(),
                aliyunSmsProperties.getEndpoint())) {
            throw new IllegalStateException("Aliyun SMS accessKeyId/accessKeySecret/endpoint is missing.");
        }
    }

    private void validateTemplateConfig() {
        if (StringUtils.isBlank(aliyunSmsProperties.getSignName())) {
            throw new IllegalStateException("Aliyun SMS signName is missing.");
        }
        if (StringUtils.isBlank(aliyunSmsProperties.getTemplateCode())) {
            throw new IllegalStateException("Aliyun SMS templateCode is missing.");
        }
    }
}
