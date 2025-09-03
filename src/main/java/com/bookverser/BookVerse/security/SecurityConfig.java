package com.bookverser.BookVerse.security;

//public class SecurityConfig {
	//import org.springframework.beans.factory.annotation.Autowired;
	//import org.springframework.context.annotation.Bean;
	//import org.springframework.context.annotation.Configuration;
	//import org.springframework.security.authentication.AuthenticationManager;
	//import org.springframework.security.authentication.AuthenticationProvider;
	//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
	//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
	//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
	//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
	//import org.springframework.security.config.http.SessionCreationPolicy;
	//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	//import org.springframework.security.crypto.password.PasswordEncoder;
	//import org.springframework.security.web.SecurityFilterChain;
	//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
	//
	//@Configuration
	//public class SecurityConfig {
	//
//	    @Autowired
//	    private JwtAuthenticationFilter jwtFilter;
	//
//	    @Autowired
//	    private CustomUserDetailsService userDetailsService;
	//
//	    @Autowired
//		private JwtAuthenticationEntryPoint entryPoint;
//	    @Bean
//	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//	        return http
//	                .csrf(csrf -> csrf.disable())
//	                .authorizeHttpRequests(auth -> auth
//	                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
//	                        .anyRequest().authenticated()
//	                )
//	                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
//	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//	                .authenticationProvider(authenticationProvider())
//	                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//	                .build();
//	    }
	//
//	    @Bean
//	    public PasswordEncoder passwordEncoder() {
//	        return new BCryptPasswordEncoder();
//	    }
	//
//	    @Bean
//	    public AuthenticationProvider authenticationProvider() {
//	        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//	        provider.setUserDetailsService(userDetailsService); // load user from DB
//	        provider.setPasswordEncoder(passwordEncoder());
//	        return provider;
//	    }
	//
//	    @Bean
//	    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//	        return config.getAuthenticationManager();
//	    }
	//}



	//new code 



	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.security.authentication.AuthenticationManager;
	import org.springframework.security.authentication.AuthenticationProvider;
	import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
	import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
	import org.springframework.security.config.annotation.web.builders.HttpSecurity;
	import org.springframework.security.config.http.SessionCreationPolicy;
	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	import org.springframework.security.crypto.password.PasswordEncoder;
	import org.springframework.security.web.SecurityFilterChain;
	import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
	import org.springframework.web.cors.CorsConfiguration;
	import org.springframework.web.cors.CorsConfigurationSource;
	import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

	import java.util.List;

	@Configuration
	public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
//
//	    @Autowired
//	    private JwtAuthenticationFilter jwtFilter;
//
//	    @Autowired
//	    private CustomUserDetailsService userDetailsService;
//
//	    @Autowired
//	    private JwtAuthenticationEntryPoint entryPoint;
//
//	    @Bean
//	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//	        logger.info("Configuring Spring Security filter chain");
//	        return http
//	                .csrf(csrf -> csrf.disable())
//	                .authorizeHttpRequests(auth -> auth
//	                    .requestMatchers("/authController/login","/authController/registeruser").permitAll() // public endpoints // "/auth/registeruser"
//	                    .anyRequest().authenticated()
//	                )
//	                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
//	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//	                .authenticationProvider(authenticationProvider())
//	                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//	                .build();
//	    }
//
//	    @Bean
//	    public CorsConfigurationSource corsConfigurationSource() {
//	        CorsConfiguration configuration = new CorsConfiguration();
//	        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Adjust for your frontend URL
//	        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//	        configuration.setAllowedHeaders(List.of("*"));
//	        configuration.setAllowCredentials(true);
//	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	        source.registerCorsConfiguration("/**", configuration);
//	        logger.info("CORS configuration applied for allowed origins: {}", configuration.getAllowedOrigins());
//	        return source;
//	    }
//
//	    @Bean
//	    public PasswordEncoder passwordEncoder() {
//	        logger.debug("Initializing BCryptPasswordEncoder");
//	        return new BCryptPasswordEncoder();
//	    }
//
//	    @Bean
//	    public AuthenticationProvider authenticationProvider() {
//	        logger.debug("Configuring AuthenticationProvider with UserDetailsService and PasswordEncoder");
//	        return new DaoAuthenticationProvider() {{
//	            setUserDetailsService(userDetailsService);
//	            setPasswordEncoder(passwordEncoder());
//	        }};
//	    }
//
//	    @Bean
//	    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//	        logger.debug("Initializing AuthenticationManager");
//	        return config.getAuthenticationManager();
//	    }
    
    //second new code
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Autowired
    private JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/authController/login", "/authController/register").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // frontend URL
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


