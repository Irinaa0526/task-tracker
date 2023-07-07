package my.proj.task.tracker.core.project.web;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.ProjectService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@RestController
public class ProjectController {

    private final ProjectService service;

    public static final String FETCH_PROJECTS = "/api/projects";
    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    @GetMapping(FETCH_PROJECTS)
    public List<ProjectView> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {
        return service.fetchProjects(optionalPrefixName);
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectView createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name") String projectName) {
        return service.createOrUpdateProject(optionalProjectId, projectName);
    }

    @DeleteMapping(DELETE_PROJECT)
    public Boolean deleteProject(@PathVariable("project_id") Long projectId) {
        return service.deleteProject(projectId);
    }
}
