package my.proj.task.tracker.helpers;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.project.ProjectRepo;
import my.proj.task.tracker.core.task.Task;
import my.proj.task.tracker.core.task.TaskRepo;
import my.proj.task.tracker.core.taskState.TaskState;
import my.proj.task.tracker.core.taskState.TaskStateRepo;
import my.proj.task.tracker.error.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Transactional
public class ControllerHelper {

    private final ProjectRepo projectRepo;
    private final TaskStateRepo taskStateRepo;
    private final TaskRepo taskRepo;

    public Project getProjectOrThrowException(Long projectId) {
        return projectRepo
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Project with id = %s doesn't exists", projectId))
                );
    }

    public TaskState getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepo
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Task state with id = %s doesn't exists", taskStateId))
                );
    }

    public Task getTaskOrThrowException(Long taskId) {
        return taskRepo
                .findById(taskId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Task with id = %s doesn't exists", taskId))
                );
    }
}
