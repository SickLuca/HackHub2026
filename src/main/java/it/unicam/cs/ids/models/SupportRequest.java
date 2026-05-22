package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.SupportRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a support request created by a team.
 * <p>
 * Used by teams to request technical or general assistance
 * from hackathon mentors. Also allows scheduling a meeting
 * via a video call link.
 * </p>
 */
@Entity
@Table(name = "support_requests")
@Getter
@Setter
@NoArgsConstructor
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The request is associated with a Team
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // Also linking the Hackathon directly to simplify
    // queries by mentors (who are assigned to the hackathon)
    @ManyToOne
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportRequestStatus status = SupportRequestStatus.PENDING;

    @Column(length = 500)
    private String meetingLink;

    private LocalDateTime meetingDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}