package it.unicam.cs.ids.utils.strategy;

import it.unicam.cs.ids.dtos.requests.PaymentRequestDTO;
import it.unicam.cs.ids.models.utils.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Componente responsabile della risoluzione ed esecuzione della corretta strategia di pagamento.
 * <p>
 * Tramite iniezione delle dipendenze, raccoglie tutte le implementazioni
 * di {@link IPaymentStrategy} in una Map e vi delega le richieste
 * di pagamento (es. iscrizione team a pagamento) a runtime.
 * </p>
 */
@Service
public class PaymentProcessor {

    private final Map<PaymentMethod, IPaymentStrategy> strategies;

    public PaymentProcessor(List<IPaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(IPaymentStrategy::getPaymentMethod, Function.identity()));
    }

    // Modifichiamo i parametri per accettare esattamente i dati necessari
    public void processPayment(PaymentMethod method, Long teamId, Double amount) {
        IPaymentStrategy strategy = strategies.get(method);

        if (strategy == null) {
            throw new IllegalArgumentException("Metodo di pagamento non supportato: " + method);
        }

        // Creiamo il DTO richiesto dall'interfaccia IPaymentStrategy
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO(teamId, amount);

        boolean success = strategy.pay(paymentRequest);

        if (!success) {
            throw new IllegalStateException("Il pagamento tramite " + method + " è fallito.");
        }
    }
}