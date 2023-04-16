package my.proj.task.tracker.core.taskState.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import lombok.*;
import my.proj.task.tracker.core.task.Task;
import my.proj.task.tracker.core.task.web.TaskView;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStateView {

    @NonNull
    private Long id;

    @NonNull
    private String name;

    @JsonProperty("left_task_state_id")
    private Long leftTaskStateId;

    @JsonProperty("right_task_state_id")
    private Long rightTaskStateId;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;

    @NotNull
    List<TaskView> tasks;
}
