package jp.mediahub.timing.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.mediahub.timing.AspectTiming;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)

/**
 * This annotation can be used to log the execution time of a method.
 * An aspect will intercept all method calls annotated with this annotation and
 * will log the execution time.
 */

public @interface LogExecutionTime {

}
