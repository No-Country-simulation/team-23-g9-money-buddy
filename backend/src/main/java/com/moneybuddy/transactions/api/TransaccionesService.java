package com.moneybuddy.transactions.api;

import com.moneybuddy.classification.application.DeterministicTransactionClassifier;
import com.moneybuddy.classification.application.TransactionClassificationInput;
import com.moneybuddy.classification.application.TransactionClassificationPort;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public final class TransaccionesService {

    private final TransactionClassificationPort transactionClassificationPort;
    private final DeterministicTransactionClassifier deterministicTransactionClassifier;

    public TransaccionesService(
        TransactionClassificationPort transactionClassificationPort,
        DeterministicTransactionClassifier deterministicTransactionClassifier) {
        this.transactionClassificationPort = transactionClassificationPort;
        this.deterministicTransactionClassifier = deterministicTransactionClassifier;
    }

    public TransaccionesResponse clasificar(TransaccionesRequest request) {
        List<TransactionClassificationInput> inputs = request.transacciones().stream()
            .map(TransaccionesService::toClassificationInput)
            .toList();
        List<String> categories = categoriesFor(inputs);

        return new TransaccionesResponse(
            true,
            "Transacciones clasificadas exitosamente",
            new TransaccionesResponse.Data(java.util.stream.IntStream.range(0, request.transacciones().size())
                .mapToObj(index -> {
                    TransaccionesRequest.TransaccionRequest transaccion = request.transacciones().get(index);
                    return new TransaccionesResponse.TransaccionClasificada(
                        normalizeType(transaccion.tipo()),
                        transaccion.fecha(),
                        transaccion.descripcion(),
                        transaccion.tipoPago(),
                        transaccion.mesesADeber(),
                        transaccion.monto(),
                        categories.get(index));
                })
                .toList()));
    }

    private static TransactionClassificationInput toClassificationInput(TransaccionesRequest.TransaccionRequest transaccion) {
        return new TransactionClassificationInput(
            transaccion.tipo(),
            transaccion.fecha(),
            transaccion.descripcion(),
            transaccion.tipoPago(),
            transaccion.mesesADeber(),
            transaccion.monto());
    }

    private List<String> categoriesFor(List<TransactionClassificationInput> inputs) {
        List<String> deterministicCategories = deterministicCategories(inputs);

        try {
            return transactionClassificationPort.classify(inputs)
                .filter(categories -> categories.size() == inputs.size())
                .filter(categories -> categories.stream().noneMatch(category -> category == null || category.isBlank()))
                .map(mlCategories -> mergeDeterministicAndMlCategories(deterministicCategories, mlCategories))
                .orElse(deterministicCategories);
        } catch (RuntimeException exception) {
            return deterministicCategories;
        }
    }

    private List<String> mergeDeterministicAndMlCategories(List<String> deterministicCategories, List<String> mlCategories) {
        return java.util.stream.IntStream.range(0, deterministicCategories.size())
            .mapToObj(index -> "otros".equals(deterministicCategories.get(index))
                ? mlCategories.get(index)
                : deterministicCategories.get(index))
            .toList();
    }

    private List<String> deterministicCategories(List<TransactionClassificationInput> inputs) {
        return inputs.stream()
            .map(deterministicTransactionClassifier::classify)
            .toList();
    }

    private static String normalizeType(String tipo) {
        return "Ingreso".equals(tipo) ? "Ingreso" : "Egreso";
    }
}
