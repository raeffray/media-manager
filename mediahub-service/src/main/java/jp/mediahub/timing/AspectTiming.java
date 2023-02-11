package jp.mediahub.timing;

import jp.mediahub.timing.annotation.LogExecutionTime;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author Renato Raeffray
 * <p>
 * Creates an aspect driven logger
 * <p>
 * The subject method must be annotaded with @{@link LogExecutionTime}
 */

@Aspect
@Component
public class AspectTiming {

  private static final Logger LOGGER = LoggerFactory.getLogger(AspectTiming.class);

  public static final String GET_LOGGER = "getLogger";

  @Around("@annotation(jp.mediahub.timing.annotation.LogExecutionTime)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

    final Logger logger = getLogger(joinPoint);

    StopWatch stopWatch = new StopWatch();

    stopWatch.start();
    final Object result = joinPoint.proceed();

    stopWatch.stop();

    logger.debug("Execution Time of [{}]: [{}] ms",
        joinPoint.getStaticPart().getSignature().toShortString(), stopWatch.getTotalTimeMillis());

    return result;
  }

  // if the subject method implements getLogger, this logger will use the subject logger
  // otherwise it will use this LOGGER
  private Logger getLogger(JoinPoint joinPoint) {
    try {
      return (Logger) MethodUtils.invokeMethod(joinPoint.getTarget(), GET_LOGGER);
    } catch (Exception e) {
      return LOGGER;
    }
  }

}
