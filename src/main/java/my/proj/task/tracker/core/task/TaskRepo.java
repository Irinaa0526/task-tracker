package my.proj.task.tracker.core.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepo extends JpaRepository<Task, Long> {

    Optional<Task> findByName(String name);

    Optional<Task> findById(Long taskId);
}
