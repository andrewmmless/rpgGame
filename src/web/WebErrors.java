import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.Map;

@RestControllerAdvice
public class WebErrors {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e) {return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()==null?"Invalid request.":e.getMessage()));}
    @ExceptionHandler(GameRepository.StaleGameException.class)
    public ResponseEntity<Map<String,String>> stale() {return ResponseEntity.status(409).body(Map.of("error","Your game changed in another tab. Reloading the latest save; your action was not applied."));}
}
