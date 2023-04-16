package my.proj.task.tracker.helpers;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.project.ProjectRepo;
import my.proj.task.tracker.error.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Transactional
public class ControllerHelper {

    private final ProjectRepo projectRepo;

    public Project getProjectOrThrowException(Long projectId) {
        return projectRepo
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Project with id = %s doesn't exists", projectId))
                );
    }
}
