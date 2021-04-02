package donation.pet.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("ssafy");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
//                .httpBasic().disable()
//                .csrf().disable()
//                .cors().configurationSource(corsConfigurationSource())
//                    .and()
                .anonymous()
                    .and()
                .authorizeRequests()
                    .antMatchers("/members/test")
                        .authenticated()
                    .anyRequest()
                        .permitAll()
//                    .antMatchers("/members/signup", "/members/duplication", "/members/login",
//                            "/members/password/**", "/members/auth/**", "/members/forget")
//                        .permitAll()
//                    .anyRequest()
//                        .authenticated()
                    .and()
                .exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

//    private CorsConfigurationSource corsConfigurationSource() {
//    }
}
