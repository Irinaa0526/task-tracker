package my.proj.task.tracker.core.task.web;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.task.Task;
import my.proj.task.tracker.core.task.TaskRepo;
import my.proj.task.tracker.core.task.converter.TaskToTaskViewConverter;
import my.proj.task.tracker.core.taskState.TaskState;
import my.proj.task.tracker.core.taskState.TaskStateRepo;
import my.proj.task.tracker.error.BadRequestException;
import my.proj.task.tracker.helpers.ControllerHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@RestController
public class TaskController {

    public final TaskRepo taskRepo;
    public final TaskStateRepo taskStateRepo;

    private final TaskToTaskViewConverter taskToTaskViewConverter;

    private final ControllerHelper controllerHelper;

    public static final String GET_TASKS = "/api/projects/{project_id}/tasks";
    public static final String CREATE_TASK = "/api/task_states/{task_state_id}/tasks";
    public static final String UPDATE_TASK = "/api/task_states/{old_task_state_id}/{task_id}";
    public static final String DELETE_TASK = "/api/tasks/{task_id}";

    @GetMapping(GET_TASKS)
    public List<TaskView> getTasks(@PathVariable(name = "project_id") Long projectId,
                                   @RequestParam(value = "task_state_id", required = false) Long taskStateId) {

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

    @PostMapping(CREATE_TASK)
    public TaskView createTask(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "task_name") String taskName,
            @RequestParam(name = "description", required = false) String description) {

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

    @PatchMapping(UPDATE_TASK)
    public TaskView updateTask(
            @PathVariable(name = "task_id") Long taskId,
            @PathVariable(name = "old_task_state_id") Long oldTaskStateId,
            @RequestParam(name = "task_state_id", required = false) Long taskStateId,
            @RequestParam(name = "task_name", required = false) String taskName,
            @RequestParam(name = "description", required = false) String description) {

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

    @DeleteMapping(DELETE_TASK)
    public Boolean deleteTask(@PathVariable(name = "task_id") Long task_id) {

        Task task = controllerHelper.getTaskOrThrowException(task_id);
        taskRepo.delete(task);

        return true;
    }
}
