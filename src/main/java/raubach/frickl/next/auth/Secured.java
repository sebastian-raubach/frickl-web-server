package raubach.frickl.next.auth;

import jakarta.ws.rs.NameBinding;
import raubach.frickl.next.util.Permission;

import java.lang.annotation.*;

/**
 * Annotation used to secure server resources.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured
{
	Permission[] value() default {};
}
