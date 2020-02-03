package com.vikingsen.inject.fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Retention(CLASS)
@Target(CONSTRUCTOR)
public @interface FragmentInject {
}
