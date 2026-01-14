package ru.practicum.requests.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.interaction.enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "participation_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private Long requesterId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    private LocalDateTime created;

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == RequestStatus.CONFIRMED;
    }

    public void confirm() {
        if (!isPending()) {
            throw new IllegalStateException("Невозможно полдтвердить заявку, которая уже не в состоянии PENDING");
        }
        status = RequestStatus.CONFIRMED;
    }

    public void reject() {
        if (!isPending()) {
            throw new IllegalStateException("Невозможно отменить заявку, которая уже не в состоянии PENDING");
        }
        status = RequestStatus.REJECTED;
    }

    public void cancel() {
        if (!isPending()) {
            throw new IllegalStateException("Невозможно отменить заявку, которая уже не в состоянии PENDING");
        }
        status = RequestStatus.CANCELED;
    }

}