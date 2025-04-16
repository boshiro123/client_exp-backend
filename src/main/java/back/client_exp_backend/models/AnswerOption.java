package back.client_exp_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answer_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(nullable = false)
  private String text;

  @Column(name = "order_number", nullable = false)
  private Integer orderNumber;

  @OneToMany(mappedBy = "answerOption", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClientAnswer> clientAnswers = new ArrayList<>();

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}