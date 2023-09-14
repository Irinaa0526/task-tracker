package my.proj.task.tracker.core.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {

    Optional<Project> findByName(String name);

    @Query(value = "SELECT * FROM project WHERE lower(name) LIKE %:prefixName%", nativeQuery = true)
    Page<Project> findByNameLike(@Param("prefixName") String prefixName, Pageable pageable);
}
