package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ScheduleCallDTO(
        @NotNull(message = "The support request ID is required")
        @Positive(message = "The ID must be a positive number")
        Long supportRequestId,

        @NotNull(message = "You must provide a date for the call")
        @Future (message = "The date must be in the future")
        LocalDateTime callDate
) {
}