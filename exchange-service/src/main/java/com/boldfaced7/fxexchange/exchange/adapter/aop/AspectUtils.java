package com.boldfaced7.fxexchange.exchange.adapter.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Optional;

public class AspectUtils {

    public static String parseKey(
            MethodSignature signature,
            ProceedingJoinPoint proceedingJoinPoint,
            String keyExpression,
            String prefix
    ) {
        String[] parameterNames = signature.getParameterNames();
        Object[] args = proceedingJoinPoint.getArgs();

        var parser = new SpelExpressionParser();
        var context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        var parsed = parser.parseExpression(keyExpression).getValue(context, Object.class);

        return Optional.ofNullable(parsed)
                .map(i -> prefix + i)
                .orElseThrow(() -> new IllegalArgumentException("키는 null이 될 수 없습니다."));
    }
}
