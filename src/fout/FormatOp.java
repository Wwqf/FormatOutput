package fout;

import log.IOColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FormatOp {
	String name() default "";
	FormatOpGravity gravity() default FormatOpGravity.CENTER;
	int color() default 30;
}

