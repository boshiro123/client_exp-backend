package back.client_exp_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "segments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Segment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String criteria;

  @ManyToMany(mappedBy = "segments")
  private Set<Client> clients = new HashSet<>();

  @ManyToMany(mappedBy = "segments")
  private Set<Survey> surveys = new HashSet<>();

  @OneToMany(mappedBy = "segment")
  private Set<RecommendationTemplate> recommendationTemplates = new HashSet<>();

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