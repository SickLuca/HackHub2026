package it.unicam.cs.ids.utils.strategy;

import it.unicam.cs.ids.dtos.requests.PaymentRequestDTO;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.PaymentMethod;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Component;

@Component
public class BankTransferPaymentStrategy implements IPaymentStrategy {

    private final IUnitOfWork unitOfWork;

    public BankTransferPaymentStrategy(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public boolean pay(PaymentRequestDTO request) {

        Team team = unitOfWork.getTeamRepository().findById(request.teamId()).orElse(null);
        if (team == null) {
            throw new IllegalArgumentException("Team non trovato");
        }

        team.setBalance(team.getBalance() + request.amount());
        unitOfWork.getTeamRepository().save(team);
        return true;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }
}