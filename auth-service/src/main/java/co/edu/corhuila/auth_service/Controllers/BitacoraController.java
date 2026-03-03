package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.Entity.Bitacora;
import co.edu.corhuila.auth_service.Repository.BitacoraRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/bitacora")
public class BitacoraController {

    private final BitacoraRepository bitacoraRepository;

    public BitacoraController(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    @GetMapping
    public List<Bitacora> listarBitacora() {
        return bitacoraRepository.findAll();
    }
}
