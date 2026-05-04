package it.unicam.cs.ids.utils.strategy;

import it.unicam.cs.ids.dtos.requests.PaymentRequestDTO;
import it.unicam.cs.ids.models.utils.PaymentMethod;

/**
 * Interfaccia base per l'implementazione del pattern Strategy relativo ai pagamenti.
 * <p>
 * Definisce un contratto standard (metodo {@code pay}) permettendo l'aggiunta
 * di diverse modalità di pagamento in modo flessibile.
 * </p>
 */
public interface IPaymentStrategy {
    // Esegue il pagamento
    boolean pay(PaymentRequestDTO request);

    // Metodo utile per far capire a Spring quale strategia stiamo usando
    PaymentMethod getPaymentMethod();
}