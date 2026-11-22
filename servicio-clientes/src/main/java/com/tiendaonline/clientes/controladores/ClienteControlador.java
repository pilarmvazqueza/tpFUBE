package com.tiendaonline.clientes.controladores;

import com.tiendaonline.clientes.clases.MaskUtils;
import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.mapper.ClienteMapper;
import com.tiendaonline.clientes.servicios.ClienteServicio;
import com.tiendaonline.clientes.servicios.FeatureFlagService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController()
@RequestMapping("/api/v1/clientes")
public class ClienteControlador {

    private final ClienteServicio servicio;
    private final FeatureFlagService featureFlagService;


    public ClienteControlador(ClienteServicio servicio,
                              FeatureFlagService featureFlagService) {
        this.servicio = servicio;
        this.featureFlagService = featureFlagService;
    }


    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@RequestBody @Valid ClienteDTO clienteDTO) {
        log.info("POST /api/v1/clientes");
        return new ResponseEntity<>(servicio.crear(ClienteMapper.toEntity(clienteDTO)), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> id(@PathVariable Long id) {

    log.info("GET /api/v1/clientes/{}", id);
    ClienteDTO clienteDTO = servicio.buscarPorId(id);

    if ( featureFlagService.isEnabled ("mask-pii")){
        log.info("Feature Flag mask-pii activada");
        clienteDTO.setNombre(MaskUtils.maskNombre(clienteDTO.getNombre()));
        clienteDTO.setEmail(MaskUtils.maskEmail(clienteDTO.getEmail()));
    }

        return new ResponseEntity<>(clienteDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}/pedidos")
    public ResponseEntity<List<PedidoDTO>> pedidos(@PathVariable Long id) {
        log.info("GET /api/v1/clientes/{}/pedidos", id);
        return new ResponseEntity<>(servicio.listarPedidos(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> Todos() {
        log.info("GET /api/v1/clientes");
        return new ResponseEntity<>(servicio.buscarTodos(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> Id(@PathVariable Long id,
                                         @RequestBody @Valid ClienteDTO clienteDTO) {
        log.info("PUT /api/v1/clientes/{}", id);
        clienteDTO.setId(id);
        return new ResponseEntity<>(servicio.actualizar(ClienteMapper.toEntity(clienteDTO)), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/clientes/{}", id);
        servicio.eliminar(id);
    }
}
