package back.client_exp_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @ManyToOne
  @JoinColumn(name = "survey_id", nullable = false)
  private Survey survey;

  @ManyToOne
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @ManyToOne
  @JoinColumn(name = "answer_option_id")
  private AnswerOption answerOption;

  @Column(name = "text_answer", columnDefinition = "TEXT")
  private String textAnswer;

  @Column(name = "numeric_answer")
  private Integer numericAnswer;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}