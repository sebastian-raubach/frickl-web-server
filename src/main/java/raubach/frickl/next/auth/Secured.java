package raubach.frickl.next.auth;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.*;

/**
 * Annotation used to secure server resources.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured
{
}
