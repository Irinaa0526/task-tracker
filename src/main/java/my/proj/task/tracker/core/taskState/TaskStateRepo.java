package my.proj.task.tracker.core.taskState;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskStateRepo extends JpaRepository<TaskState, Long> {

    Optional<TaskState> findTaskStateByProjectIdAndNameContainsIgnoreCase(Long projectId, String taskStateName);

    Optional<TaskState> findById(Long taskStateId);
}
