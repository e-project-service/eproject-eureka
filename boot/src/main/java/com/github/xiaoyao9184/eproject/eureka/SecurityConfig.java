package com.github.xiaoyao9184.eproject.eureka;

import com.xy.spring.security.oauth2.client.OAuth2ClientAuthorizedConfigurer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by xy on 2021/3/12.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //noinspection unchecked
        http.apply(new OAuth2ClientAuthorizedConfigurer().defaultSuccessUrl("/"));
        // @formatter:off
        http
                .oauth2Client()
                    .and()
                .oauth2Login()
                    .and()
                .oauth2ResourceServer()
                    .jwt();
        http
                .authorizeRequests()
                    .requestMatchers(EndpointRequest.toAnyEndpoint())
                        .access("hasAuthority('SCOPE_actuate.admin') or hasAnyRole('DEV','ACTUATOR')")
                    .antMatchers(HttpMethod.OPTIONS).permitAll()
                    .antMatchers("/eureka/**")
                        .permitAll()
                    .antMatchers("/","/lastn")
                        .access("hasAnyAuthority('SCOPE_eureka.admin') or hasAnyRole('DEV')")
                    .anyRequest().authenticated()
                    .and()
                .cors().and()
                .csrf().disable()
                .headers().frameOptions().disable().and()
                .httpBasic();
        // @formatter:on
    }


    @Configuration
    public static class GlobalUserConfig {
        private static final String NOOP_PASSWORD_PREFIX = "{noop}";

        private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");

        private static final Log logger = LogFactory.getLog(GlobalUserConfig.class);

        @Bean
        @Lazy
        public InMemoryUserDetailsManager inMemoryUserDetailsManager(
                SecurityProperties properties,
                ObjectProvider<PasswordEncoder> passwordEncoder) {
            SecurityProperties.User user = properties.getUser();
            List<String> roles = user.getRoles();
            return new InMemoryUserDetailsManager(
                    User.withUsername(user.getName()).password(getOrDeducePassword(user, passwordEncoder.getIfAvailable()))
                            .roles(StringUtils.toStringArray(roles)).build());
        }

        private String getOrDeducePassword(SecurityProperties.User user, PasswordEncoder encoder) {
            String password = user.getPassword();
            if (user.isPasswordGenerated()) {
                logger.info(String.format("%n%nUsing generated security password: %s%n", user.getPassword()));
            }
            if (encoder != null || PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
                return password;
            }
            return NOOP_PASSWORD_PREFIX + password;
        }
    }

}
