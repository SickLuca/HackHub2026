package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.exceptions.UnauthorizedActionException;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.SubmissionStatus;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing submissions (project deliveries).
 * <p>
 * Contains the logic to allow teams to submit their projects
 * before the established deadline. Also allows updating
 * submissions and evaluation by judges (assigning
 * a score and feedback).
 * </p>
 */
@Service
@Transactional
public class SubmissionService implements ISubmissionService {

    private final IUnitOfWork unitOfWork;


    public SubmissionService(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public SubmissionResponseDTO addSubmission(CreateSubmissionDTO request, Long userId) {
        // 1. Retrieve User and Team safely!
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Team team = user.getTeam();
        if (team == null) {
            throw new RuleViolationException("You must belong to a team in order to submit a project.");
        }

        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId()).orElse(null);
        if (hackathon == null) throw new ResourceNotFoundException("Hackathon not found.");

        // Check 1: Is the team registered for THIS hackathon?
        if (team.getSubscribedHackathon() == null || !team.getSubscribedHackathon().getId().equals(hackathon.getId())) {
            throw new RuleViolationException("The team is not subscribed to this Hackathon.");
        }

        // Check 2: Has the submission deadline passed?
        // It is important to verify the exact deadline as per the specifications
        if (LocalDateTime.now().isAfter(hackathon.getSubmitDeadline())) {
            throw new RuleViolationException("The submission deadline has already passed.");
        }

        // Check whether an existing submission already exists.
        // If the list is null, create an empty stream on the fly.
        Optional<Submission> sub = (team.getSubmissions() == null) ? Optional.empty() :
                team.getSubmissions().stream()
                        .filter(s -> s.getHackathon().getId().equals(hackathon.getId()))
                        .findFirst();

        if (sub.isEmpty()) {
            // If it doesn't exist, create a new submission
            Submission newSubmission = new Submission();
            newSubmission.setTeam(team);
            newSubmission.setHackathon(hackathon);
            newSubmission.setProjectUrl(request.projectUrl());
            newSubmission.setDescription(request.description());
            newSubmission.setSubmissionDate(LocalDateTime.now());
            newSubmission.setStatus(SubmissionStatus.OPEN);
            newSubmission.setScore(0);
            newSubmission.setJudgeFeedback("");


            // 1. Save to the database to generate the ID
            Submission savedSubmission = unitOfWork.getSubmissionRepository().save(newSubmission);

            // 2. BIDIRECTIONAL RELATIONSHIP SYNC!
            // Update the in-memory list of the Team, otherwise on the
            // next call the team would appear to have no submissions.
            team.getSubmissions().add(savedSubmission);
            hackathon.getSubmissions().add(savedSubmission);

            return mapToDTO(savedSubmission);
        } else {
            //TODO: or we could map the creation request to an update and return the update :)

            // If it exists, inform that a submission is already present and can be modified
            throw new RuleViolationException("A submission for this Hackathon already exists.");
        }
    }

    @Override
    public SubmissionResponseDTO updateSubmission(UpdateSubmissionDTO request, Long userId) {
        Submission submission = unitOfWork.getSubmissionRepository().findById(request.submissionId()).orElse(null);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found.");
        }
        if (submission.getHackathon().getSubmitDeadline().isBefore(LocalDateTime.now())) {
            throw new RuleViolationException("The submission deadline has already passed.");
        }

        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId).orElseThrow();
        if (user.getTeam() == null || !submission.getTeam().getId().equals(user.getTeam().getId())) {
            throw new UnauthorizedActionException("You cannot modify a submission that does not belong to your team.");
        }

        submission.setProjectUrl(request.projectUrl());
        submission.setDescription(request.description());
        submission.setSubmissionDate(LocalDateTime.now());
        unitOfWork.getSubmissionRepository().save(submission);

        return mapToDTO(submission);
    }

    @Override
    public SubmissionResponseDTO evaluateSubmission(EvaluateSubmissionDTO request, Long judgeId) {
        // 1. Retrieve the submission
        Submission submission = unitOfWork.getSubmissionRepository().findById(request.submissionId()).orElse(null);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found");
        }

        Hackathon hackathon = submission.getHackathon();

        // 2. Security check: is the evaluator actually the judge of this Hackathon?
        if (!hackathon.getJudge().getId().equals(judgeId)) {
            throw new UnauthorizedActionException("You are not the judge assigned to this Hackathon");
        }

        if (hackathon.getStatus() != HackathonStatus.UNDER_EVALUATION) {
            throw new RuleViolationException("The hackathon is not currently in the evaluation phase");
        }

        //Only closed because you cannot manage it if open or already evaluated
        if (submission.getStatus() != SubmissionStatus.CLOSED ) {
            throw new RuleViolationException("You cannot manage this submission");
        }

        // 5. Update the entity
        submission.setScore(request.score());
        submission.setJudgeFeedback(request.feedback());
        submission.setStatus(SubmissionStatus.EVALUATED);
        unitOfWork.getSubmissionRepository().save(submission);

        return mapToDTO(submission);
    }

    @Override
    public List<SubmissionResponseDTO> getSubmissionsByHackathon(Long hackathonId, Long staffId) {
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(hackathonId).orElse(null);
        if (hackathon == null) {
            throw new ResourceNotFoundException("Hackathon not found.");
        }

        // Security check: is the staff member part of this hackathon?
        if (!isStaffAssignedToHackathon(hackathon, staffId)) {
            throw new UnauthorizedActionException("You are not authorized to view the submissions of this hackathon.");
        }

        List<Submission> submissions = unitOfWork.getSubmissionRepository().findByHackathonId(hackathonId);

        return submissions.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public SubmissionResponseDTO getSubmissionDetails(Long submissionId, Long staffId) {
        Submission submission = unitOfWork.getSubmissionRepository().findById(submissionId).orElse(null);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found.");
        }

        // Security check: is the staff member part of the hackathon this submission belongs to?
        if (!isStaffAssignedToHackathon(submission.getHackathon(), staffId)) {
            throw new UnauthorizedActionException("You are not authorized to view the details of this submission.");
        }

        return mapToDTO(submission);
    }

    // Private helper method to centralize access control
    private boolean isStaffAssignedToHackathon(Hackathon hackathon, Long staffId) {
        // Is it the organizer?
        if (hackathon.getOrganizer().getId().equals(staffId)) return true;

        // Is it the judge?
        if (hackathon.getJudge().getId().equals(staffId)) return true;

        // Is it one of the mentors?
        return hackathon.getMentors().stream().anyMatch(m -> m.getId().equals(staffId));
    }


    private SubmissionResponseDTO mapToDTO(Submission submission) {
        return new SubmissionResponseDTO(
                submission.getId(),
                submission.getTeam().getName(),
                submission.getHackathon().getName(),
                submission.getProjectUrl(),
                submission.getDescription(),
                submission.getSubmissionDate(),
                submission.getStatus().toString(),
                submission.getScore(),
                submission.getJudgeFeedback()
        );
    }
}