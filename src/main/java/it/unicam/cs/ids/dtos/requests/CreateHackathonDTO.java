package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record CreateHackathonDTO(

        @NotBlank(message = "The hackathon name is required")
        @Size(min = 3, max = 100, message = "The name must be between 3 and 100 characters")
        String name,

        @NotNull(message = "The start date cannot be null")
        @Future(message = "The start date must be in the future")
        LocalDateTime startDate,

        @NotNull(message = "The registration deadline cannot be null")
        @Future(message = "The registration deadline must be in the future")
        LocalDateTime registrationDeadline,

        @NotNull(message = "The submission deadline cannot be null")
        @Future(message = "The submission deadline must be in the future")
        LocalDateTime submitDeadline,

        @NotBlank(message = "The regulation is required")
        String regulation,

        @PositiveOrZero(message = "The cash prize cannot be negative")
        Double cashPrize,

        @NotBlank(message = "The location is required")
        String location,

        @Min(value = 1, message = "The maximum team size must be at least 1")
        Integer maxDimensionOfTeam,

        @NotNull(message = "You must assign a judge")
        Long judgeId,

        @NotEmpty(message = "You must assign at least one mentor")
        List<Long> mentorsIdS
) {

    /**
     * This method is automatically invoked by Spring Validation.
     * Checks the chronological consistency of the dates.
     */
    @AssertTrue(message = "Chronology error: registration must end before the start date, and submission must occur after the hackathon start.")
    public boolean isDatesValid() {
        // If any date is null, return true to skip this check.
        // The @NotNull annotations above will block the request,
        // preventing a NullPointerException here.
        if (startDate == null || registrationDeadline == null || submitDeadline == null) {
            return true;
        }

        // Business rules:
        // 1. The registration deadline must be before or equal to the start date
        // 2. The submission deadline must be after the start date
        return (registrationDeadline.isBefore(startDate) || registrationDeadline.isEqual(startDate))
                && submitDeadline.isAfter(startDate) && submitDeadline.isAfter(registrationDeadline);
    }
}