package my.proj.task.tracker.core.taskState.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStateView {

    @NonNull
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private Long ordinal;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;

}
