package my.proj.task.tracker.core.task;

import my.proj.task.tracker.core.taskState.TaskState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepo extends JpaRepository<Task, Long> {

    @Query(value = "SELECT * FROM task t JOIN task_state ts ON t.task_state_id = ts.task_state_id " +
                   "JOIN project p ON ts.project_id = p.project_id WHERE p.project_id = :projectId", nativeQuery = true)
    Page<Task> findTaskByProject(Long projectId, Pageable pageable);

    @Query(value = "SELECT * FROM task WHERE task_state_id = :taskStateId", nativeQuery = true)
    Page<Task> findTaskByTaskStateId(Long taskStateId, Pageable pageable);
}
