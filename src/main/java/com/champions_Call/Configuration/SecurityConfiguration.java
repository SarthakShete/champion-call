package com.champions_Call.Configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.champions_Call.service.impl.SecurityCustomUserDetailsService;

@Configuration
public class SecurityConfiguration {

    
    @Autowired
    private SecurityCustomUserDetailsService userDetailsService;

    @Autowired
    private oauthAuthenticationSuccessHandler handler;

    // ⬇️ Inject the key from your application.properties file
    @Value("${app.security.remember-me.key}")
    private String rememberMeKey;

    @Autowired
    private DataSource dataSource;

    // 💡 2. Add the Token Repository Bean right here
    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);  
        provider.setPasswordEncoder(passwordEncode());        
        return provider;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
            throws Exception {

        httpSecurity
            .authorizeHttpRequests(authorize -> {
                authorize.requestMatchers(
                "/",                            
                    "/home",
                    "/login",
                    "/signUp",
                    "/do-signUp",
                    "/authenticate",
                    "/error",
                    "/test",
                    "/scorer/**",
                    "/ws/**",
                    
                    // 🔥 STATIC RESOURCES
                    "/css/**",
                    "/js/**",
                    "/Images/**"
                ).permitAll();

                // Specific public API paths BEFORE /api/organizer/**
                authorize.requestMatchers(HttpMethod.GET,
                    "/api/organizer/tournament/list",
                    "/api/organizer/tournament/search-tournament",   // option 1
                    "/api/organizer/search-tournament",              // option 2 (remove whichever is wrong)
                    "/api/organizer/tournament/tournament-details/{id}"
                ).permitAll();

                authorize.requestMatchers(HttpMethod.POST,
                    "/api/organizer/tournament/*/register"
                ).hasRole("PLAYER");
                
                authorize.requestMatchers(HttpMethod.GET,
                    "/api/match/*/details",
                    "/api/public/**"
                ).permitAll();

                // ⬇️ Broad role-based rules come AFTER specific exceptions
                authorize.requestMatchers("/api/organizer/**").hasRole("ORGANIZER");
                authorize.requestMatchers("/api/player/**").hasRole("PLAYER");
                authorize.requestMatchers("/user/**").authenticated();
                
                authorize.anyRequest().authenticated();
            })

            .authenticationProvider(authenticationProvider())  
            .formLogin(formLogin -> {

                // Telling the User that you have to used Custom Login page....
                // loginProcessingUrl("/authenticate") => This is the URL where the login form is submitted
                /*  1) loginProcessingUrl("/authenticate") → Handles form       submission (POST) automatically
                2) You don’t write a controller for /authenticate, Spring handles login internally.
                3) Customize loginProcessingUrl only if you want a different POST URL than default /login. */
                formLogin.loginPage("/login")
                // authenticate is like a servlet page where login is handled...
                .loginProcessingUrl("/authenticate")
                

                // .failureForwardUrl("/login?error=true")

                // In Login page username field name is email 
                // and password field name is Password....
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/user/profile", true);


                // You can also used the SuccessHandler....
                // .failureHandler(new AuthenticationFailureHandler() {
                //     @Override
                //     public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                //         throw new UnsupportedOperationException("Not supported yet.");
                //     }

                // })
                

                
            

            })

            //  .formLogin(form -> form
            //     .defaultSuccessUrl("/user/dashboard", true)           // redirect after login
            //     .permitAll()                                          // allow everyone to see login page
            // )
            // .logout(logout -> logout
            //     .logoutUrl("/logout")
            //     .logoutSuccessUrl("/login?logout")
            //     .permitAll()
            // );
            .rememberMe(remember -> remember
                .key(rememberMeKey) 
                .rememberMeParameter("remember-me")         // 💡 ADD THIS LINE EXPLICITLY
                .tokenRepository(persistentTokenRepository()) // Uses the bean above
                .userDetailsService(userDetailsService)       // Loads profile details on startup   
                .tokenValiditySeconds(86400 * 7)              // Token keeps you logged in for 7 days
            )
            .csrf(AbstractHttpConfigurer::disable)
            
            .logout(logoutForm -> {
                logoutForm.logoutUrl("/do-logout");
                logoutForm.logoutSuccessUrl("/login?logout=true");  
                logoutForm.invalidateHttpSession(true);
                logoutForm.clearAuthentication(true);
                logoutForm.deleteCookies("JSESSIONID", "remember-me");
            })  
            
            // OAuth Configuration....

            .oauth2Login(oauth -> {
                oauth.loginPage("/login");
                //oauth.successHandler(null);

                // Before redirecting we store the User info in db...
                // Inside the oauthAuthenticationSuccessHandler....
                oauth.successHandler(handler); 
            });

        return httpSecurity.build();
    }

@Bean
    public PasswordEncoder passwordEncode(){
        return new BCryptPasswordEncoder();
    }


    
} 
