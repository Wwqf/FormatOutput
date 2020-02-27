package fout.annoation;

import fout.attr.FoutGravity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FormatOp {
	String columnName() default "";
	FoutGravity gravity() default FoutGravity.CENTER;
	int foreColor() default 30;
	int backColor() default 49;
}

