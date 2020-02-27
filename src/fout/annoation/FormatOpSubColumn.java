package fout.annoation;


import fout.base.FoutColumn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FormatOpSubColumn {
	String columnName();
	Class<? extends FoutColumn> cls();
}
