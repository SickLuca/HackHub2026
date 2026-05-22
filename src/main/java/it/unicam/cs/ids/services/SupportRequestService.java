package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.requests.ScheduleCallDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.exceptions.UnauthorizedActionException;
import it.unicam.cs.ids.models.*;
import it.unicam.cs.ids.models.utils.SupportRequestStatus;
import it.unicam.cs.ids.utils.adapter.ICalendarService;
import it.unicam.cs.ids.services.abstractions.ISupportRequestService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing technical support/mentoring requests.
 * <p>
 * Implements the functionality for teams to create help requests
 * and allows mentors to schedule support calls
 * (relying on the {@link ICalendarService} to generate meeting links).
 * </p>
 */
@Service
@Transactional
public class SupportRequestService implements ISupportRequestService {

    private final IUnitOfWork unitOfWork;
    private final ICalendarService calendarService;

    public SupportRequestService(IUnitOfWork unitOfWork, ICalendarService calendarService) {
        this.unitOfWork = unitOfWork;
        this.calendarService = calendarService;
    }

    @Override
    public SupportRequestResponseDTO createRequest(CreateSupportRequestDTO requestDTO, Long userId) {
        // 1. Retrieve User and Team safely from the token
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Team team = user.getTeam();
        if (team == null) {
            throw new RuleViolationException("You must belong to a team in order to request support.");
        }

        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(requestDTO.hackathonId()).orElse(null);
        if (hackathon == null) throw new ResourceNotFoundException("Hackathon not found");

        // (Optional) Verify that the Team is actually registered for this Hackathon
        if (!team.getSubscribedHackathon().getId().equals(hackathon.getId())) {
            throw new RuleViolationException("The team is not registered for this Hackathon");
        }

        boolean exists = unitOfWork.getSupportRequestRepository().findByHackathonId(requestDTO.hackathonId()).stream()
                .filter(request -> request.getStatus().equals(SupportRequestStatus.PENDING)) //Filter PENDING requests
                .anyMatch(request -> request.getTeam().getId().equals(team.getId())); //Check if a request exists for this team

        if (exists) {
            throw new RuleViolationException("A pending support request already exists for your team in this Hackathon");
        }

        // 2. Create the entity
        SupportRequest request = new SupportRequest();
        request.setTeam(team);
        request.setHackathon(hackathon);
        request.setMessage(requestDTO.message());
        request.setStatus(SupportRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        // 3. Save
        unitOfWork.getSupportRequestRepository().save(request);

        //update the bidirectional relationship in memory
        team.getSupportRequests().add(request);

        //update the bidirectional relationship in memory
        hackathon.getSupportRequests().add(request);

        // 4. Return DTO
        return mapToDTO(request);

    }

    @Override
    public List<SupportRequestResponseDTO> getRequestsForHackathon(Long hackathonId, Long mentorId) {
        // 1. (Security) Verify the user is actually a mentor for this Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(hackathonId).orElse(null);
        if (hackathon == null) throw new ResourceNotFoundException("Hackathon not found");

        boolean isMentor = hackathon.getMentors().stream()
                .anyMatch(mentor -> mentor.getId().equals(mentorId));

        if (!isMentor) {
            throw new UnauthorizedActionException("You are not assigned as a mentor for this Hackathon");
        }

        // 2. Retrieve the requests
        List<SupportRequest> requests = unitOfWork.getSupportRequestRepository().findByHackathonId(hackathonId);

        // 3. Map to DTO
        return requests.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public SupportRequestResponseDTO scheduleCall(ScheduleCallDTO request, Long mentorId) {
        // 1. Retrieve the request
        SupportRequest supportRequest = unitOfWork.getSupportRequestRepository().findById(request.supportRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Support request not found"));


        // 2. Retrieve the mentor
        StaffUser mentor = unitOfWork.getStaffUserRepository().findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        // 3. Verify that the request is in PENDING status
        if (supportRequest.getStatus() != SupportRequestStatus.PENDING) {
            throw new RuleViolationException("This request has already been handled or is closed.");
        }

        // 4. Verify that the mentor is assigned to the hackathon of this request
        boolean isMentor = supportRequest.getHackathon().getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentorId));

        if (!isMentor) {
            throw new UnauthorizedActionException("You are not assigned as a mentor for this Hackathon.");
        }

        // 5. Delegate link generation to the external system
        String meetingLink = calendarService.generateMeetingLink(mentor.getName(), supportRequest.getTeam().getName());

        // 6. Update the entity
        supportRequest.setStatus(SupportRequestStatus.SCHEDULED);
        supportRequest.setMeetingLink(meetingLink);
        supportRequest.setMeetingDate(request.callDate());

        unitOfWork.getSupportRequestRepository().save(supportRequest);

        return mapToDTO(supportRequest);
    }

    private SupportRequestResponseDTO mapToDTO(SupportRequest request) {
        return new SupportRequestResponseDTO(
                request.getId(),
                request.getTeam().getName(),
                request.getHackathon().getName(),
                request.getMessage(),
                request.getStatus().name(),
                request.getCreatedAt(),
                request.getMeetingLink() == null ? "Not Planned" : request.getMeetingLink(),
                request.getMeetingDate()
        );
    }
}