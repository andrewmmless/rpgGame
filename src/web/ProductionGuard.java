import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/** Prevent deployment with ephemeral local saves or insecure production cookies. */
@Configuration(proxyBeanMethods=false)
@Profile("production")
public class ProductionGuard {
    public ProductionGuard(Environment environment) {
        String url=environment.getProperty("spring.datasource.url","");
        if(!url.startsWith("jdbc:postgresql:") || !url.contains("sslmode="))
            throw new IllegalStateException("Production requires a PostgreSQL JDBC URL with SSL enabled.");
        if(url.contains("sslmode=disable") || url.contains("sslmode=allow") || url.contains("sslmode=prefer"))
            throw new IllegalStateException("Production database SSL must be required or verified.");
        if(!environment.getProperty("server.servlet.session.cookie.secure",Boolean.class,false))
            throw new IllegalStateException("Production requires secure cookies and HTTPS.");
        if(environment.getProperty("spring.datasource.password","").isBlank())
            throw new IllegalStateException("Production requires a database password.");
    }
}
