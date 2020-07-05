package org.imanity.framework.config.util.annotation;

import org.imanity.framework.config.util.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a custom conversion mechanism is used to convert the
 * annotated field.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Convert {
    Class<? extends Converter<?, ?>> value();
}
