package back.client_exp_backend.dto;

import back.client_exp_backend.models.enums.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientWithAnswersDto {
  private Long id;
  private String name;
  private String email;
  private String phone;
  private AgeGroup ageGroup;
  private Gender gender;
  private String profession;
  private String region;
  private LocationPreference locationPreference;
  private UsageFrequency usageFrequency;
  private Source source;
  private String socialNetwork;
  private UsagePurpose usagePurpose;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate clientSince;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  private List<ClientAnswerDto> answers;
}