package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.SupportRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entità che rappresenta una richiesta di supporto creata da un team.
 * <p>
 * Utilizzata dai team per richiedere assistenza tecnica o generica
 * ai mentori di un hackathon. Permette inoltre di pianificare un incontro
 * tramite un link di videochiamata.
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

    // La richiesta è associata a un Team
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // Colleghiamo anche l'Hackathon direttamente per facilitare
    // le query dei mentori (che sono assegnati all'hackathon)
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

    //TODO: scadenza inviti?
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}