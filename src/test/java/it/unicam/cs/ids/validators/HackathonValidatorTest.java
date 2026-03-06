package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.models.Hackathon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HackathonValidatorTest {

    private HackathonValidator validator;
    private Hackathon validHackathon;

    @BeforeEach
    void setUp() {
        validator = new HackathonValidator();
        validHackathon = new Hackathon();

        // Setup di base di un hackathon con valori corretti
        validHackathon.setStartDate(LocalDateTime.now().plusDays(10));
        validHackathon.setEndDate(LocalDateTime.now().plusDays(15));
        validHackathon.setMaxDimensionOfTeam(5);
        validHackathon.setCashPrize(1000.0);
    }

    @Test
    void shouldReturnTrueForValidHackathon() {
        assertTrue(validator.validate(validHackathon), "L'hackathon valido deve restituire true");
    }

    @Test
    void shouldReturnFalseIfHackathonIsNull() {
        assertFalse(validator.validate(null), "Un hackathon nullo deve restituire false");
    }

    @Test
    void shouldReturnFalseIfStartDateIsAfterEndDate() {
        // Data inizio successiva a quella di fine
        validHackathon.setStartDate(LocalDateTime.now().plusDays(20));
        validHackathon.setEndDate(LocalDateTime.now().plusDays(10));

        assertFalse(validator.validate(validHackathon), "Non deve validare se l'inizio è successivo alla fine");
    }

    @Test
    void shouldReturnFalseIfMaxDimensionIsLessThanOne() {
        validHackathon.setMaxDimensionOfTeam(0);

        assertFalse(validator.validate(validHackathon), "Non deve validare se i membri max sono < 1");
    }

    @Test
    void shouldReturnFalseIfCashPrizeIsNegative() {
        validHackathon.setCashPrize(-100.0);

        assertFalse(validator.validate(validHackathon), "Non deve validare se il premio in denaro è negativo");
    }
}
