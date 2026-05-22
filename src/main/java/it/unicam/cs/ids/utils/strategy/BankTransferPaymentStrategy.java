package it.unicam.cs.ids.utils.strategy;

import it.unicam.cs.ids.dtos.requests.PaymentRequestDTO;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.PaymentMethod;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link IPaymentStrategy} for processing bank transfer payments.
 */
@Component
public class BankTransferPaymentStrategy implements IPaymentStrategy {

    private final IUnitOfWork unitOfWork;

    /**
     * Constructs a new {@code BankTransferPaymentStrategy} with the given unit of work.
     *
     * @param unitOfWork the unit of work used to interact with repositories
     */
    public BankTransferPaymentStrategy(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    /**
     * Processes a payment request using a bank transfer.
     * It adds the requested amount to the team's balance.
     *
     * @param request the payment request details
     * @return {@code true} if the payment is successfully processed
     * @throws IllegalArgumentException if the team specified in the request is not found
     */
    @Override
    public boolean pay(PaymentRequestDTO request) {

        Team team = unitOfWork.getTeamRepository().findById(request.teamId()).orElse(null);
        if (team == null) {
            throw new IllegalArgumentException("Team not found");
        }

        team.setBalance(team.getBalance() + request.amount());
        unitOfWork.getTeamRepository().save(team);
        return true;
    }

    /**
     * Retrieves the payment method type associated with this strategy.
     *
     * @return the {@link PaymentMethod#BANK_TRANSFER}
     */
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }
}