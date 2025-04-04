package back.client_exp_backend.models;

import back.client_exp_backend.models.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(name = "age_group")
  private AgeGroup ageGroup;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private String profession;

  private String region;

  @Enumerated(EnumType.STRING)
  @Column(name = "location_preference")
  private LocationPreference locationPreference;

  @Enumerated(EnumType.STRING)
  @Column(name = "usage_frequency")
  private UsageFrequency usageFrequency;

  @Enumerated(EnumType.STRING)
  private Source source;

  @Column(name = "social_network")
  private String socialNetwork;

  @Enumerated(EnumType.STRING)
  @Column(name = "usage_purpose")
  private UsagePurpose usagePurpose;

  @Column(name = "client_since")
  private LocalDate clientSince;

  @ManyToMany
  @JoinTable(name = "client_segments", joinColumns = @JoinColumn(name = "client_id"), inverseJoinColumns = @JoinColumn(name = "segment_id"))
  private Set<Segment> segments = new HashSet<>();

  @OneToMany(mappedBy = "client")
  private List<ClientAnswer> clientAnswers = new ArrayList<>();

  @OneToMany(mappedBy = "client")
  private List<Feedback> feedbacks = new ArrayList<>();

  @OneToMany(mappedBy = "client")
  private List<ClientInteraction> interactions = new ArrayList<>();

  @OneToMany(mappedBy = "client")
  private List<Recommendation> recommendations = new ArrayList<>();

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