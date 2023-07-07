package my.proj.task.tracker.core.task;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.task.converter.TaskToTaskViewConverter;
import my.proj.task.tracker.core.task.web.TaskView;
import my.proj.task.tracker.core.taskState.TaskState;
import my.proj.task.tracker.core.taskState.TaskStateRepo;
import my.proj.task.tracker.error.BadRequestException;
import my.proj.task.tracker.helpers.ControllerHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TaskService {

    private final TaskRepo taskRepo;
    private final TaskStateRepo taskStateRepo;
    private final TaskToTaskViewConverter taskToTaskViewConverter;
    private final ControllerHelper controllerHelper;

    public List<TaskView> getTasks(Long projectId, Long taskStateId) {
        Project project = controllerHelper.getProjectOrThrowException(projectId);

        // в зависимости от наличия id состояния выводим либо все задачи проекта, либо все задачи
        // соответствующие указанному состоянию в проекте
        return project
                .getTaskStates()
                .stream()
                .filter(taskStateId != null ? taskState -> taskState.getTaskStateId().equals(taskStateId) : taskState -> true)
                .flatMap(taskState -> taskState.getTasks().stream())
                .map(taskToTaskViewConverter::convert)
                .collect(Collectors.toList());
    }

    public TaskView createTask(Long taskStateId, String taskName, String description) {
        if (taskName.isBlank()) {
            throw new BadRequestException("Task name can't be empty");
        }

        // берем по id состояние (id состояний не повторяются в рамках всей бд)
        TaskState taskState = controllerHelper.getTaskStateOrThrowException(taskStateId);

        // создаем задачу
        Task task = taskRepo.saveAndFlush(
                Task.builder()
                        .name(taskName)
                        .description(description)
                        .build()
        );

        // добавляем задачу в список задач состояния и сохраняем
        taskState.getTasks().add(task);
        taskStateRepo.saveAndFlush(taskState);

        return taskToTaskViewConverter.convert(task);
    }

    public TaskView updateTask(Long taskId, Long oldTaskStateId, Long taskStateId, String taskName, String description) {
        Task task = controllerHelper.getTaskOrThrowException(taskId);

        // если изменений нет, то возвращаем тот же таск
        if (taskStateId == null && taskName == null && description == null) {
            return taskToTaskViewConverter.convert(task);
        }

        if (taskStateId != null && !taskStateId.equals(oldTaskStateId)) {
            // добавляем задачу в новое состояние
            TaskState taskState = controllerHelper.getTaskStateOrThrowException(taskStateId);
            taskState.getTasks().add(task);
            taskStateRepo.saveAndFlush(taskState);

            // удаляем задачу из предыдущего состояния
            TaskState oldTaskState = controllerHelper.getTaskStateOrThrowException(oldTaskStateId);
            oldTaskState.getTasks().remove(task);
            taskStateRepo.saveAndFlush(oldTaskState);
        }

        if (taskName != null) {
            if (taskName.isBlank()) {
                throw new BadRequestException("Task name can't be empty");
            }
            task.setName(taskName);
        }

        // чтобы обнулить описание таски просто присваиваем ей пустую строку
        if (description != null)
            task.setDescription(description);

        task.setUpdatedAt(Instant.now());
        task = taskRepo.saveAndFlush(task);

        return taskToTaskViewConverter.convert(task);
    }

    public Boolean deleteTask(Long taskId) {
        Task task = controllerHelper.getTaskOrThrowException(taskId);
        taskRepo.delete(task);

        return true;
    }
}
