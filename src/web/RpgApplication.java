import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({WebSecurity.class, GameRepository.class, GameController.class, WebErrors.class, ProductionGuard.class, DeveloperLab.class, CoopController.class, SocialStore.class, SocialController.class})
public class RpgApplication {
    public static void main(String[] args) { SpringApplication.run(RpgApplication.class,args); }
}
