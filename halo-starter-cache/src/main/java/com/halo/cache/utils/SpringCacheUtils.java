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


	@SneakyThrows
	public boolean checkCondition(String conditionExpress, ProceedingJoinPoint point) {
		if (StringUtils.isBlank(conditionExpress)) {
			return true;
		}
		Expression expression = parser.parseExpression(conditionExpress);
		EvaluationContext evaluationContext = getEvaluationContext(point);

		return (Boolean) expression.getValue(evaluationContext);
	}

	public static String generateMethodKey(String beanName, Method method) {
		StringBuilder methodKeySb = new StringBuilder(beanName).append(".").append(method.getName());
		for (Class<?> parameterType : method.getParameterTypes()) {
			methodKeySb.append(".").append(parameterType.getName());
		}

		return methodKeySb.toString();
	}

	public String generateCacheKey(String nameSpace, String name, ProceedingJoinPoint point) throws IOException, NoSuchMethodException {

		Object[] args = getArgs(point);
		Object nameObject = null;
		try {
			Expression expression = parser.parseExpression(name);
			EvaluationContext evaluationContext = getEvaluationContext(point);
			nameObject = expression.getValue(evaluationContext);
		} catch (Exception e) {
			log.info(getFullPathClassName(point) + "." + getMethodName(point) + "的SpEL表达式转换异常", e);
		}

		String cacheNameSpace = StringUtils.isBlank(nameSpace) ? getFullPathClassName(point) + "." + getMethodName(point) : nameSpace;
		Object cacheName = ObjectUtils.isEmpty(nameObject) ? args : nameObject;

		return String.format("nameSpace:%s:name:%s", cacheNameSpace, objectMapper.writeValueAsString(cacheName));
	}

	@SneakyThrows
	public <T extends Annotation> T getAnnotation(ProceedingJoinPoint point, Class<T> clazz) {
		String methodName = point.getSignature().getName();
		Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
		Method objMethod = point.getTarget().getClass().getMethod(methodName, parameterTypes);

		return objMethod.getAnnotation(clazz);
	}

	public EvaluationContext getEvaluationContext(ProceedingJoinPoint point) {
		EvaluationContext context = new StandardEvaluationContext();
		Object[] args = getArgs(point);
		String[] parameterNames = getParameterNames(point);
		for (int i = 0; i < args.length; i++) {
			context.setVariable(parameterNames[i], args[i]);
		}
		return context;
	}

	public Class<?> getClazz(ProceedingJoinPoint point) {

		return point.getTarget().getClass();
	}

	public String getFullPathClassName(ProceedingJoinPoint point) {

		return getClazz(point).getTypeName();
	}

	public String getMethodName(ProceedingJoinPoint point) {

		return point.getSignature().getName();
	}

	public Object[] getArgs(ProceedingJoinPoint point) {

		return point.getArgs();
	}

	public Class<?>[] getParameterTypes(ProceedingJoinPoint point) {

		return ((MethodSignature) point.getSignature()).getParameterTypes();
	}

	@SneakyThrows
	public Method getMethod(ProceedingJoinPoint point) {

		return getClazz(point).getMethod(getMethodName(point), getParameterTypes(point));
	}

	public String[] getParameterNames(ProceedingJoinPoint point) {

		return nameDiscoverer.getParameterNames(getMethod(point));
	}

}
