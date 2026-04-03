package co.edu.corhuila.inventory_service.Controllers;



import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Service.MotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MotionController {

    private final MotionService motionService;

    public MotionController(MotionService motionService) {
        this.motionService = motionService;
    }



    @GetMapping({"/api/movements", "/api/motions", "/api/Motion"})
    public ResponseEntity<List<MotionResponse>> listMotion() {
        return ResponseEntity.ok(motionService.listMotion());
    }

    @GetMapping("/api/movements/filter-by-user")
    public ResponseEntity<List<MotionResponse>> listMotionByUser(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(motionService.listMotionByUser(userId));
    }

    @GetMapping("/api/movements/entrance")
    public ResponseEntity<List<MotionResponse>> listEntranceMotion() {
        return ResponseEntity.ok(motionService.listEntranceMotion());
    }

    @GetMapping("/api/movements/exit")
    public ResponseEntity<List<MotionResponse>> listExitMotion() {
        return ResponseEntity.ok(motionService.listExitMotion());
    }

    @GetMapping("/api/movements/updated")
    public ResponseEntity<List<MotionResponse>> listUpdatedMotion() {
        return ResponseEntity.ok(motionService.listUpdatedMotion());
    }
}
