package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.Entity.Binnacle;
import co.edu.corhuila.auth_service.Repository.BinnacleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/binnacle")
public class BinnacleController {

    private final BinnacleRepository binnacleRepository;

    public BinnacleController(BinnacleRepository binnacleRepository) {
        this.binnacleRepository = binnacleRepository;
    }

    @GetMapping
    public List<Binnacle> listLogbook() {
        return binnacleRepository.findAll();
    }
}
