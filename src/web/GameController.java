import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.*;
import java.security.Principal;
import java.util.*;

@RestController
public class GameController {
    private final GameRepository games;private final PasswordEncoder encoder;
    public GameController(GameRepository games,PasswordEncoder encoder) {this.games=games;this.encoder=encoder;}
    public record Registration(String username,String password) {}
    public record Creation(String name,String playerClass) {}
    public record Command(long version,String action,String value) {}
    public record ImportSave(String text) {}
    @GetMapping("/health") public Map<String,String> health() {return Map.of("status","ok");}
    @GetMapping("/api/csrf") public Map<String,String> csrf(CsrfToken token) {return Map.of("headerName",token.getHeaderName(),"token",token.getToken());}
    @GetMapping("/api/me") public Map<String,Object> me(Principal principal) {return principal==null?Map.of("signedIn",false):Map.of("signedIn",true,"username",principal.getName());}
    @PostMapping("/api/register") @ResponseStatus(HttpStatus.CREATED) public void register(@RequestBody Registration body) {games.register(body.username(),body.password(),encoder);}
    @GetMapping("/api/leaderboard") public List<Map<String,Object>> leaderboard() {return games.leaderboard();}
    @GetMapping("/api/game") public Map<String,Object> game(Principal principal) {return games.load(principal.getName());}
    @PostMapping("/api/character") public Map<String,Object> create(Principal principal,@RequestBody Creation body) {return games.create(principal.getName(),body.name(),body.playerClass());}
    @PostMapping("/api/command") public Map<String,Object> command(Principal principal,@RequestBody Command body) {return games.command(principal.getName(),body.version(),body.action(),body.value());}
    @PostMapping("/api/import") public Map<String,Object> importSave(Principal principal,@RequestBody ImportSave body) {return games.importConsole(principal.getName(),body.text());}
    @GetMapping("/api/export") public ResponseEntity<String> export(Principal principal) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=hearthglen-save.json").contentType(MediaType.APPLICATION_JSON).body(games.export(principal.getName()));
    }
}
