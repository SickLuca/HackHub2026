package it.unicam.cs.ids.dtos.requests;

public record UpdateReportDTO (
        Long reportId,
        Long organizerId,
        String decisionNote
){
}