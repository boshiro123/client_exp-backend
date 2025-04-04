package back.client_exp_backend.models;

import back.client_exp_backend.models.enums.MetricType;
import back.client_exp_backend.models.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "survey_id", nullable = false)
  private Survey survey;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String text;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuestionType type;

  @Column(nullable = false)
  private boolean required;

  @Column(name = "order_number", nullable = false)
  private Integer orderNumber;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private QuestionCategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "metric_type")
  private MetricType metricType;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AnswerOption> answerOptions = new ArrayList<>();

  @OneToMany(mappedBy = "question")
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