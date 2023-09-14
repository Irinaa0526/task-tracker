package my.proj.task.tracker.core.taskState;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskStateRepo extends JpaRepository<TaskState, Long> {

    @Query(value = "SELECT * FROM task_state WHERE project_id = :projectId", nativeQuery = true)
    Page<TaskState> findTaskStateByProject(Long projectId, Pageable pageable);
}
