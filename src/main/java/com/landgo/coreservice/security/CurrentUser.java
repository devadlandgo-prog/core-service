package com.landgo.coreservice.security;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "principal instanceof T(com.landgo.coreservice.security.UserPrincipal) ? principal.id : null")
public @interface CurrentUser {}
