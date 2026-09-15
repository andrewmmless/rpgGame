import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@Configuration(proxyBeanMethods=false)
public class WebSecurity {
    @Bean PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder(12);}
    @Bean UserDetailsService users(JdbcTemplate jdbc) {
        return username -> jdbc.query("SELECT username,password_hash FROM hearthglen.rpg_users WHERE username=?",(rs,n)->User.withUsername(rs.getString(1)).password(rs.getString(2)).roles("PLAYER").build(),username.toLowerCase(Locale.ROOT))
            .stream().findFirst().orElseThrow(()->new UsernameNotFoundException("Account not found"));
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/","/index.html","/app.js","/style.css","/favicon.svg","/landscape.svg","/town.svg","/api/csrf","/api/register","/api/me","/health").permitAll()
            .anyRequest().authenticated());
        // Spring Security keeps session-based CSRF protection enabled for every mutation, including login/logout.
        http.formLogin(form -> form.loginProcessingUrl("/api/login")
            .successHandler((req,res,auth)->res.setStatus(204))
            .failureHandler((req,res,error)->{res.setStatus(401);res.setContentType("application/json");res.getWriter().write("{\"error\":\"Account name or password is incorrect.\"}");}).permitAll());
        http.logout(logout -> logout.logoutUrl("/api/logout").logoutSuccessHandler((req,res,auth)->res.setStatus(204)));
        http.exceptionHandling(errors -> errors.authenticationEntryPoint((req,res,error)->res.setStatus(401)));
        http.headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'; form-action 'self'")));
        http.addFilterBefore(new AuthThrottle(),UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    /** Bounded per-address authentication throttling. Persistent distributed limiting can replace this for multiple replicas. */
    private static final class AuthThrottle extends OncePerRequestFilter {
        private record Window(long since,int count) {}
        private final Map<String,Window> windows=new LinkedHashMap<>();
        private synchronized boolean allowed(String address) {
            long now=System.currentTimeMillis();windows.entrySet().removeIf(e->now-e.getValue().since()>600_000);
            Window old=windows.get(address);
            if(old==null) {if(windows.size()>=10_000)return false;windows.put(address,new Window(now,1));return true;}
            if(old.count()>=30)return false;windows.put(address,new Window(old.since(),old.count()+1));return true;
        }
        @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException {
            if(req.getMethod().equals("POST") && Set.of("/api/login","/api/register").contains(req.getRequestURI()) && !allowed(req.getRemoteAddr())) {
                res.setStatus(429);res.setContentType("application/json");res.getWriter().write("{\"error\":\"Too many sign-in attempts. Try again in ten minutes.\"}");return;
            }
            chain.doFilter(req,res);
        }
    }
}
