package my.proj.task.tracker.core.taskState;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskStateRepo extends JpaRepository<TaskState, Long> {

    Optional<TaskState> findTaskStateByProjectAndNameContainsIgnoreCase(Long projectId, String taskStateName);

//    Optional<TaskState> findByTaskStateId(Long taskStateId);
}
