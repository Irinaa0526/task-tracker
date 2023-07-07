package my.proj.task.tracker.core.taskState;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.taskState.converter.TaskStateToTaskStateViewConverter;
import my.proj.task.tracker.core.taskState.web.TaskStateView;
import my.proj.task.tracker.error.BadRequestException;
import my.proj.task.tracker.helpers.ControllerHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TaskStateService {

    private final TaskStateRepo taskStateRepo;
    private final TaskStateToTaskStateViewConverter taskStateToTaskStateViewConverter;
    private final ControllerHelper controllerHelper;

    public List<TaskStateView> getTaskStates(Long projectId) {
        Project project = controllerHelper.getProjectOrThrowException(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(taskStateToTaskStateViewConverter::convert)
                .collect(Collectors.toList());
    }

    public TaskStateView createTaskState(Long projectId, String taskStateName) {
        if (taskStateName.isBlank()) {
            throw new BadRequestException("Task state name can't be empty");
        }

        Project project = controllerHelper.getProjectOrThrowException(projectId);
        Optional<TaskState> optionalLeftTaskState = Optional.empty();

        // проверяем существует ли таск стейт с введенным именем и ищем последний таск стейт (без соседа справа)
        for (TaskState taskState : project.getTaskStates()) {
            if (taskState.getName().equalsIgnoreCase(taskStateName))
                throw new BadRequestException(String.format("Task state with name '%s' already exists", taskStateName));

            if (!taskState.getRightTaskState().isPresent())
                optionalLeftTaskState = Optional.of(taskState);
        }

        TaskState taskState = taskStateRepo.saveAndFlush(
                TaskState.builder()
                        .name(taskStateName)
                        .project(project)
                        .build()
        );

        optionalLeftTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);
                    anotherTaskState.setRightTaskState(taskState);
                    taskStateRepo.saveAndFlush(anotherTaskState);
                });

        final TaskState savedTaskState = taskStateRepo.saveAndFlush(taskState);

        return taskStateToTaskStateViewConverter.convert(savedTaskState);
    }

    public TaskStateView updateTaskState(Long taskStateId, String taskStateName) {
        if (taskStateName.isBlank()) {
            throw new BadRequestException("Task state name can't be empty");
        }

        TaskState taskState = controllerHelper.getTaskStateOrThrowException(taskStateId);

        // проверяем, что внутри проекта нет таск стейта с таким же именем (изменяемый таск стейт не в счет)
        taskStateRepo
                .findTaskStateByProjectAndNameContainsIgnoreCase(taskState.getProject().getProjectId(), taskStateName)
                .filter(anotherTaskState -> !anotherTaskState.getTaskStateId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException(String.format("Task state '%s' already exists", taskStateName));
                });

        taskState.setName(taskStateName);
        taskState = taskStateRepo.saveAndFlush(taskState);

        return taskStateToTaskStateViewConverter.convert(taskState);
    }

    public TaskStateView changeTaskStatePosition(Long taskStateId, Optional<Long> optionalLeftTaskStateId) {
        TaskState changeTaskState = controllerHelper.getTaskStateOrThrowException(taskStateId);
        Project project = changeTaskState.getProject();

        Optional<Long> optionalOldLeftTaskStateId = changeTaskState.getLeftTaskState().map(TaskState::getTaskStateId);

        // если старый левый сосед == новый левый сосед, тогда просто возвращаем изменяемый таск стейт
        if (optionalOldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return taskStateToTaskStateViewConverter.convert(changeTaskState);
        }

        // получаем новый левый таск стейт по заданному id
        Optional <TaskState> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {
                    if (taskStateId.equals(leftTaskStateId)) {
                        throw new BadRequestException("Left task state id equals changed task state id");
                    }

                    TaskState leftTaskStateEntity = controllerHelper.getTaskStateOrThrowException(leftTaskStateId);

                    // проверяем что найденный таск стейт находится в одном проекте с изменяемым таск стейтом
                    if (!project.getProjectId().equals(leftTaskStateEntity.getProject().getProjectId())) {
                        throw new BadRequestException("Task state position can be changed within the same project");
                    }
                    return leftTaskStateEntity;
                });

        // если новый левый таск стейт не существует => ищем в рамках проекта таск стейт без левого соседа - это новый правый сосед
        // если новый левый таск стейт существует => новый правый сосед - берем у нового левого соседа правого соседа
        Optional<TaskState> optionalNewRightTaskState;
        if (!optionalNewLeftTaskState.isPresent()) {
            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> !anotherTaskState.getLeftTaskState().isPresent())
                    .findAny();
        } else {
            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        // обновляем соседей у старых соседей изменяемого таск стейта
        replaceOldTaskStatePosition(changeTaskState);

        // связываем нового левого соседа с изменяемым таск стейтом
        if (optionalNewLeftTaskState.isPresent()) {
            TaskState newLeftTaskState = optionalNewLeftTaskState.get();
            newLeftTaskState.setRightTaskState(changeTaskState);
            changeTaskState.setLeftTaskState(newLeftTaskState);
        } else {
            changeTaskState.setLeftTaskState(null);
        }

        // связываем нового правого соседа с изменяемым таск стейтом
        if (optionalNewRightTaskState.isPresent()) {
            TaskState newRightTaskState = optionalNewRightTaskState.get();
            newRightTaskState.setLeftTaskState(changeTaskState);
            changeTaskState.setRightTaskState(newRightTaskState);
        } else {
            changeTaskState.setRightTaskState(null);
        }

        changeTaskState = taskStateRepo.saveAndFlush(changeTaskState);

        optionalNewLeftTaskState.ifPresent(taskStateRepo::saveAndFlush);

        optionalNewRightTaskState.ifPresent(taskStateRepo::saveAndFlush);

        return taskStateToTaskStateViewConverter.convert(changeTaskState);
    }

    public Boolean deleteTaskState(Long taskStateId) {
        TaskState changeTaskState = controllerHelper.getTaskStateOrThrowException(taskStateId);
        replaceOldTaskStatePosition(changeTaskState);
        taskStateRepo.delete(changeTaskState);

        return true;
    }

    private void replaceOldTaskStatePosition(TaskState changeTaskState) {
        Optional<TaskState> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskState> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        // если старый левый сосед существует => его правый сосед теперь старый правый сосед изменяемого таск стейта либо null
        optionalOldLeftTaskState
                .ifPresent(it -> {
                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));
                    taskStateRepo.saveAndFlush(it);
                });

        // если старый правый сосед существует => его левый сосед теперь старый левый сосед изменяемого таск стейта либо null
        optionalOldRightTaskState
                .ifPresent(it -> {
                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));
                    taskStateRepo.saveAndFlush(it);
                });
    }
}
