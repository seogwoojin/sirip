package uos.software.sirip.event.infra.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptimizeRequest {

    private String title;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("organizer_type")
    private String organizerType;

    @JsonProperty("target_major")
    private String targetMajor;

    @JsonProperty("target_grade")
    private String targetGrade;

    private String weekday;

    @JsonProperty("brand_score")
    private double brandScore;

    @JsonProperty("date_gap")
    private int dateGap;

    @JsonProperty("target_participants")
    private double targetParticipants;

    // getters & setters
}

