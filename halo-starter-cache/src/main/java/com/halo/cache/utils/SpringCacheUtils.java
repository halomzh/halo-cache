package com.halo.cache.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shoufeng
 */

@Data
@Slf4j
@Component
public class SpringCacheUtils implements ApplicationContextAware, BeanPostProcessor {

	public static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringCacheUtils.applicationContext = applicationContext;
	}

	public static ConfigurableApplicationContext getConfigurableApplicationContext() {
		return (ConfigurableApplicationContext) applicationContext;
	}

	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 用于SpEL表达式解析
	 */
	private SpelExpressionParser parser = new SpelExpressionParser();

	/**
	 * 用于获取方法参数定义名字.
	 */
	private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

	public static String generateMethodKey(String beanName, Method method) {
		StringBuilder methodKeySb = new StringBuilder(beanName).append(".").append(method.getName());
		for (Class<?> parameterType : method.getParameterTypes()) {
			methodKeySb.append(".").append(parameterType.getName());
		}

		return methodKeySb.toString();
	}

	public String generateCacheKey(String nameSpace, String name, ProceedingJoinPoint point) throws IOException, NoSuchMethodException {

		Class<?> classTarget = point.getTarget().getClass();
		String fullPathClassName = classTarget.getTypeName();
		String methodName = point.getSignature().getName();
		Object[] args = point.getArgs();

		Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
		Method objMethod = classTarget.getMethod(methodName, parameterTypes);

		Map<String, Object> cacheKeyMap = new HashMap<>();

		String[] parameterNames = nameDiscoverer.getParameterNames(objMethod);

		Object nameObject = null;
		try {
			Expression expression = parser.parseExpression(name);
			EvaluationContext context = new StandardEvaluationContext();
			for (int i = 0; i < args.length; i++) {
				context.setVariable(parameterNames[i], args[i]);
			}
			nameObject = expression.getValue(context);
		} catch (Exception e) {
			log.info(fullPathClassName + "." + methodName + "的SpEL表达式转换异常", e);
		}

		String cacheNameSpace = StringUtils.isBlank(nameSpace) ? fullPathClassName + "." + methodName : nameSpace;
		Object cacheName = ObjectUtils.isEmpty(nameObject) ? args : nameObject;
		cacheKeyMap.put("nameSpace", cacheNameSpace);
		cacheKeyMap.put("name", cacheName);

		return objectMapper.writeValueAsString(cacheKeyMap);
	}

	@SneakyThrows
	public <T extends Annotation> T getAnnotation(ProceedingJoinPoint point, Class<T> clazz) {
		String methodName = point.getSignature().getName();
		Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
		Method objMethod = point.getTarget().getClass().getMethod(methodName, parameterTypes);

		return objMethod.getAnnotation(clazz);
	}

}
