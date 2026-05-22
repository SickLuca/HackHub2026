package it.unicam.cs.ids.utils.strategy;

import it.unicam.cs.ids.dtos.requests.PaymentRequestDTO;
import it.unicam.cs.ids.models.utils.PaymentMethod;

/**
 * Base interface for implementing the Strategy pattern related to payments.
 * <p>
 * Defines a standard contract (the {@code pay} method) allowing the addition
 * of different payment methods in a flexible way.
 * </p>
 */
public interface IPaymentStrategy {
    // Executes the payment
    boolean pay(PaymentRequestDTO request);

    // Useful to let Spring know which strategy is being used
    PaymentMethod getPaymentMethod();
}