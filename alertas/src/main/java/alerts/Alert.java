package alerts;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotBlank(message = "El tipo de alerta es obligatorio")
    @Column(nullable = false, length = 80)
    private String type;

    @NotBlank(message = "La ubicación es obligatoria")
    @Column(nullable = false, length = 150)
    private String location;

    @NotBlank(message = "La severidad es obligatoria")
    @Column(nullable = false, length = 50)
    private String severity;

    @Column(length = 500)
    private String description;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
