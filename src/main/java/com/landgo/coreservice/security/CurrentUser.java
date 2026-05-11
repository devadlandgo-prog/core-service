package com.landgo.coreservice.security;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "#this instanceof T(com.landgo.coreservice.security.UserPrincipal) ? id : null")
public @interface CurrentUser {}
