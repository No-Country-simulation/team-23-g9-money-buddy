package com.moneybuddy.transactions.api;

import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public final class TransaccionesService {

    public TransaccionesResponse clasificar(TransaccionesRequest request) {
        return new TransaccionesResponse(
            true,
            "Transacciones clasificadas exitosamente",
            new TransaccionesResponse.Data(request.transacciones().stream()
                .map(transaccion -> new TransaccionesResponse.TransaccionClasificada(
                    normalizeType(transaccion.tipo()),
                    transaccion.fecha(),
                    transaccion.descripcion(),
                    transaccion.tipoPago(),
                    transaccion.mesesADeber(),
                    transaccion.monto(),
                    category(transaccion)))
                .toList()));
    }

    private String category(TransaccionesRequest.TransaccionRequest transaccion) {
        if ("Ingreso".equals(transaccion.tipo())) {
            return "ingreso";
        }

        String description = normalize(transaccion.descripcion());

        if (containsAny(description, "supermercado", "mercado", "comida", "restaurante", "cafe", "delivery", "alimento")) {
            return "alimentos";
        }
        if (containsAny(description, "bus", "taxi", "uber", "transporte", "metro", "combustible", "gasolina")) {
            return "transporte";
        }
        if (containsAny(description, "farmacia", "medico", "salud", "hospital", "clinica")) {
            return "salud";
        }
        if (containsAny(description, "alquiler", "renta", "hipoteca")) {
            return "vivienda";
        }
        if (containsAny(description, "colegio", "universidad", "curso", "libro", "educacion")) {
            return "educacion";
        }
        if (containsAny(description, "luz", "agua", "internet", "servicio", "telefono", "gas")) {
            return "servicios";
        }
        if (containsAny(description, "cine", "streaming", "juego", "entretenimiento", "ocio")) {
            return "ocio_entretenimiento";
        }
        if (containsAny(description, "ropa", "zapato", "calzado", "accesorio", "camisa", "pantalon")) {
            return "ropa_calzado";
        }
        if (containsAny(description, "laptop", "notebook", "computadora", "celular", "smartphone", "electronica", "tecnologia")) {
            return "tecnologia";
        }

        return "otros";
    }

    private String normalizeType(String tipo) {
        return "Ingreso".equals(tipo) ? "Ingreso" : "Egreso";
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
