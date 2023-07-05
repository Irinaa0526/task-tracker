package my.proj.task.tracker.core.task.converter;

import my.proj.task.tracker.core.task.Task;
import my.proj.task.tracker.core.task.web.TaskView;
import org.springframework.stereotype.Component;

@Component
public class TaskToTaskViewConverter {

    public TaskView convert(Task task) {
        return TaskView.builder()
                .id(task.getTaskId())
                .name(task.getName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .description(task.getDescription())
                .build();
    }
}
