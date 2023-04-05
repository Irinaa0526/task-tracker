package my.proj.task.tracker.core.taskState.converter;

import my.proj.task.tracker.core.taskState.TaskState;
import my.proj.task.tracker.core.taskState.web.TaskStateView;
import org.springframework.stereotype.Component;

@Component
public class TaskStateToTaskStateViewConverter {
    public TaskStateView convert(TaskState taskState) {
        return TaskStateView.builder()
                .id(taskState.getId())
                .name(taskState.getName())
                .ordinal(taskState.getOrdinal())
                .createdAt(taskState.getCreatedAt())
                .build();
    }
}
