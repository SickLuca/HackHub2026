package it.unicam.cs.ids.utils.strategy;

import it.unicam.cs.ids.dtos.requests.PaymentRequestDTO;
import it.unicam.cs.ids.models.utils.PaymentMethod;

public interface IPaymentStrategy {
    // Esegue il pagamento
    boolean pay(PaymentRequestDTO request);

    // Metodo utile per far capire a Spring quale strategia stiamo usando
    PaymentMethod getPaymentMethod();
}