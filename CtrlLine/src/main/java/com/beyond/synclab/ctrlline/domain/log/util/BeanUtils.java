package com.beyond.synclab.ctrlline.domain.log.util;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BeanUtils implements ApplicationContextAware {

    @Getter
    private static ApplicationContext context;

    @Override
    public synchronized void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
        if (BeanUtils.context == null) {
            BeanUtils.context = applicationContext;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        ApplicationContext ctx = context;
        if (ctx == null) {
            log.debug("ApplicationContext not initialized yet.");
            throw new AppException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return ctx.getBean(clazz);
    }

}