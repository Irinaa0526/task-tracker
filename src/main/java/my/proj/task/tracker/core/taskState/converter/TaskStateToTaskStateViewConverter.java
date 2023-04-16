package my.proj.task.tracker.core.taskState.converter;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.task.converter.TaskToTaskViewConverter;
import my.proj.task.tracker.core.taskState.TaskState;
import my.proj.task.tracker.core.taskState.web.TaskStateView;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TaskStateToTaskStateViewConverter {

    private final TaskToTaskViewConverter taskToTaskViewConverter;

    public TaskStateView convert(TaskState taskState) {
        return TaskStateView.builder()
                .id(taskState.getId())
                .name(taskState.getName())
                .leftTaskStateId(taskState.getLeftTaskState().map(TaskState::getId).orElse(null))
                .rightTaskStateId(taskState.getRightTaskState().map(TaskState::getId).orElse(null))
                .createdAt(taskState.getCreatedAt())
                .tasks(
                        taskState
                            .getTasks()
                            .stream()
                            .map(taskToTaskViewConverter::convert)
                            .collect(Collectors.toList())
                )
                .build();
    }
}
