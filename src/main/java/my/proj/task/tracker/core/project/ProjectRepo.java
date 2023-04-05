package my.proj.task.tracker.core.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface ProjectRepo extends JpaRepository<Project, Long> {

    Optional<Project> findByName(String name);

    Stream<Project> streamAll();

    Stream<Project> streamAllByNameStartsWithIgnoreCase(String prefixName);
}
