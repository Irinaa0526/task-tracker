package my.proj.task.tracker.core.taskState;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStateRepo extends JpaRepository<TaskState, Long> {
}
