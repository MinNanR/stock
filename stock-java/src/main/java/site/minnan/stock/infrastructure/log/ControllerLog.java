package site.minnan.stock.infrastructure.log;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ControllerLog {

    @Pointcut("execution(public * site.minnan.stock.userinterface.fascade..*..*(..))")
    private void controllerLog() {
    }


    @Around("controllerLog()")
    public Object logAroundController(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long time = System.currentTimeMillis();
        Object[] args = proceedingJoinPoint.getArgs();
        JSONArray jsonArray =
                Arrays.stream(args).collect(JSONArray::new, JSONArray::add, JSONArray::addAll);
        String methodFullName = proceedingJoinPoint.getTarget().getClass().getName()
                + "." + proceedingJoinPoint.getSignature().getName();
        log.info("controller调用{}，参数：{}", methodFullName, jsonArray.toJSONString(0));
        Object retValue = proceedingJoinPoint.proceed();
        time = System.currentTimeMillis() - time;
        String responseString = new JSONObject(retValue).toJSONString(0);
        log.info("controller调用{}完成，返回数据:{}，用时{}ms", methodFullName, responseString, time);
        return retValue;
    }
}
