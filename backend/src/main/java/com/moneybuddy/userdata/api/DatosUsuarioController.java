package com.moneybuddy.userdata.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public final class DatosUsuarioController {
    
    @PostMapping("/datos-usuario")
    public ResponseEntity<DatosUsuarioResponse> guardarDatosUsuario(@Valid @RequestBody DatosUsuarioRequest request) {
        return ResponseEntity.ok(validarDatosUsuario(request));
    }

    @PutMapping("/datos-usuario")
    public ResponseEntity<DatosUsuarioResponse> actualizarDatosUsuario(@Valid @RequestBody DatosUsuarioRequest request) {
        return ResponseEntity.ok(validarDatosUsuario(request));
    }

    private DatosUsuarioResponse validarDatosUsuario(DatosUsuarioRequest request) {
        return new DatosUsuarioResponse(
            true, 
            "Datos financieros validados correctamente", 
            new DatosUsuarioResponse.Data(
                request.creditoTotal(), 
                request.ingresoMensual(), 
                request.frecuenciaAhorro()
            )
        );
    }
} 
