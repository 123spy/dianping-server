package com.spy.server.controller;

import com.spy.server.common.BaseResponse;
import com.spy.server.common.ErrorCode;
import com.spy.server.config.AliyunSmsProperties;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.dto.sms.SendCodeRequest;
import com.spy.server.utils.AccountUtil;
import com.spy.server.utils.AliyunSmsUtil;
import com.spy.server.utils.ResultUtil;
import com.spy.server.utils.SmsRedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private AliyunSmsUtil aliyunSmsUtil;

    @Resource
    private AliyunSmsProperties aliyunSmsProperties;

    @PostMapping("/send")
    public BaseResponse<Boolean> sendSms(@RequestBody SendCodeRequest request) {
        if (request == null || !AccountUtil.checkUserPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid phone number");
        }

        String phone = request.getPhone();
        String codeKey = SmsRedisKeyUtil.loginCodeKey(phone);
        String cooldownKey = SmsRedisKeyUtil.sendCooldownKey(phone);
        String phoneDailyKey = SmsRedisKeyUtil.phoneDailyLimitKey(phone, LocalDate.now());
        long ttlSeconds = aliyunSmsUtil.getCodeTtlSeconds();

        checkSendCooldown(phone, cooldownKey);
        checkPhoneDailyLimit(phone, phoneDailyKey);

        String code = aliyunSmsUtil.generateCode();

        try {
            aliyunSmsUtil.sendSms(phone, code);
            redisTemplate.opsForValue().set(codeKey, code, ttlSeconds, TimeUnit.SECONDS);
            incrementPhoneDailyCount(phoneDailyKey);
            Long remainSeconds = redisTemplate.getExpire(codeKey, TimeUnit.SECONDS);
            log.info("短信验证码已写入 Redis：手机号={}，验证码={}，缓存键={}，过期秒数={}，剩余秒数={}",
                    phone, code, codeKey, ttlSeconds, remainSeconds);
        } catch (Exception e) {
            redisTemplate.delete(cooldownKey);
            log.error("短信发送失败：手机号={}，验证码={}，缓存键={}，过期秒数={}", phone, code, codeKey, ttlSeconds, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to send SMS: " + e.getMessage());
        }

        return ResultUtil.success(true);
    }

    private void checkSendCooldown(String phone, String cooldownKey) {
        int cooldownSeconds = getSendCooldownSeconds();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", cooldownSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            return;
        }
        Long remainSeconds = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        log.info("短信发送被冷却限制拦截：手机号={}，冷却键={}，剩余秒数={}", phone, cooldownKey, remainSeconds);
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please wait 60 seconds before requesting another code");
    }

    private void checkPhoneDailyLimit(String phone, String phoneDailyKey) {
        String currentValue = redisTemplate.opsForValue().get(phoneDailyKey);
        int currentCount = currentValue == null ? 0 : Integer.parseInt(currentValue);
        int maxPerPhonePerDay = getMaxPerPhonePerDay();
        if (currentCount < maxPerPhonePerDay) {
            return;
        }
        Long remainSeconds = redisTemplate.getExpire(phoneDailyKey, TimeUnit.SECONDS);
        log.warn("短信发送超过手机号自然日上限：手机号={}，当前次数={}，每日上限={}，剩余秒数={}",
                phone, currentCount, maxPerPhonePerDay, remainSeconds);
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "This phone number has reached today's SMS limit");
    }

    private void incrementPhoneDailyCount(String phoneDailyKey) {
        Long count = redisTemplate.opsForValue().increment(phoneDailyKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(phoneDailyKey, getSecondsUntilTomorrow(), TimeUnit.SECONDS);
        }
    }

    private long getSecondsUntilTomorrow() {
        return Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay()).getSeconds();
    }

    private int getSendCooldownSeconds() {
        Integer sendCooldownSeconds = aliyunSmsProperties.getSendCooldownSeconds();
        return sendCooldownSeconds == null || sendCooldownSeconds <= 0 ? 60 : sendCooldownSeconds;
    }

    private int getMaxPerPhonePerDay() {
        Integer maxPerPhonePerDay = aliyunSmsProperties.getMaxPerPhonePerDay();
        return maxPerPhonePerDay == null || maxPerPhonePerDay <= 0 ? 20 : maxPerPhonePerDay;
    }
}
