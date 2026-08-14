package com.moneybuddy.classification.application;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public final class DeterministicTransactionClassifier {

	public String classify(TransactionClassificationInput transaction) {
		if ("Ingreso".equalsIgnoreCase(transaction.tipo())) {
			return "ingreso";
		}

		String description = normalize(transaction.descripcion());

		if (containsAny(description, "supermercado", "mercado", "comida", "restaurante", "cafe", "delivery", "alimento", "cena")) {
			return "alimentos";
		}
		if (containsAny(description, "bus", "taxi", "uber", "transporte", "metro", "combustible", "gasolina", "auto")) {
			return "transporte";
		}
		if (containsAny(description, "farmacia", "medico", "medica", "medicina", "salud", "hospital", "clinica", "consulta")) {
			return "salud";
		}
		if (containsAny(description, "alquiler", "renta", "hipoteca", "departamento")) {
			return "vivienda";
		}
		if (containsAny(description, "colegio", "universidad", "curso", "libro", "educacion")) {
			return "educacion";
		}
		if (containsAny(description, "luz", "agua", "internet", "servicio", "telefono", "gas", "factura")) {
			return "servicios";
		}
		if (containsAny(description, "cine", "netflix", "streaming", "juego", "entretenimiento", "ocio")) {
			return "ocio_entretenimiento";
		}
		if (containsAny(description, "ropa", "zapato", "zapatilla", "calzado", "accesorio", "camisa", "pantalon")) {
			return "ropa_calzado";
		}
		if (containsAny(description, "laptop", "notebook", "computadora", "celular", "smartphone", "electronica", "tecnologia")) {
			return "tecnologia";
		}

		return "otros";
	}

	private String normalize(String value) {
		if (value == null) {
			return "";
		}
		String lowerCase = value.toLowerCase(Locale.ROOT);
		String normalized = Normalizer.normalize(lowerCase, Normalizer.Form.NFD);
		return normalized.replaceAll("\\p{M}", "");
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
