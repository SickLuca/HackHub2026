package it.unicam.cs.ids.utils.strategy;

import it.unicam.cs.ids.dtos.requests.PaymentRequestDTO;
import it.unicam.cs.ids.models.utils.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Component responsible for resolving and executing the correct payment strategy.
 * <p>
 * Through dependency injection, collects all implementations
 * of {@link IPaymentStrategy} into a Map and delegates
 * payment requests (e.g. paid team registration) to them at runtime.
 * </p>
 */
@Service
public class PaymentProcessor {

    private final Map<PaymentMethod, IPaymentStrategy> strategies;

    public PaymentProcessor(List<IPaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(IPaymentStrategy::getPaymentMethod, Function.identity()));
    }

    // Adjust the parameters to accept exactly the required data
    public void processPayment(PaymentMethod method, Long teamId, Double amount) {
        IPaymentStrategy strategy = strategies.get(method);

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }

        // Create the DTO required by the IPaymentStrategy interface
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO(teamId, amount);

        boolean success = strategy.pay(paymentRequest);

        if (!success) {
            throw new IllegalStateException("Payment via " + method + " failed.");
        }
    }
}