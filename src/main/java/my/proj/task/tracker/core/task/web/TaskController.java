package my.proj.task.tracker.core.task.web;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.task.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Transactional
@RestController
public class TaskController {

    private final TaskService taskService;

    public static final String GET_TASKS = "/api/projects/{project_id}/tasks";
    public static final String CREATE_TASK = "/api/task_states/{task_state_id}/tasks";
    public static final String UPDATE_TASK = "/api/task_states/{old_task_state_id}/{task_id}";
    public static final String DELETE_TASK = "/api/tasks/{task_id}";

    @GetMapping(GET_TASKS)
    public Page<TaskView> getTasks(@PathVariable(name = "project_id") Long projectId,
                                        @RequestParam(value = "task_state_id", required = false) Long taskStateId,
                                        @PageableDefault(sort = "created_at", direction = Sort.Direction.ASC) Pageable pageable) {
        return taskService.getTasks(projectId, taskStateId, pageable);
    }

    @PostMapping(CREATE_TASK)
    public TaskView createTask(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "task_name") String taskName,
            @RequestParam(name = "description", required = false) String description) {
        return taskService.createTask(taskStateId, taskName, description);
    }

    @PatchMapping(UPDATE_TASK)
    public TaskView updateTask(
            @PathVariable(name = "task_id") Long taskId,
            @PathVariable(name = "old_task_state_id") Long oldTaskStateId,
            @RequestParam(name = "task_state_id", required = false) Long taskStateId,
            @RequestParam(name = "task_name", required = false) String taskName,
            @RequestParam(name = "description", required = false) String description) {
        return taskService.updateTask(taskId, oldTaskStateId, taskStateId, taskName, description);
    }

    @DeleteMapping(DELETE_TASK)
    public Boolean deleteTask(@PathVariable(name = "task_id") Long taskId) {
        return taskService.deleteTask(taskId);
    }
}
