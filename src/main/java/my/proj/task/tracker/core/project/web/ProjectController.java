package my.proj.task.tracker.core.project.web;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.project.ProjectRepo;
import my.proj.task.tracker.core.project.converter.ProjectToProjectViewConverter;
import my.proj.task.tracker.error.BadRequestException;
import my.proj.task.tracker.error.NotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@RestController
public class ProjectController {

    private final ProjectRepo projectRepo;
    private final ProjectToProjectViewConverter projectToProjectViewConverter;

    public static final String FETCH_PROJECTS = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @GetMapping(FETCH_PROJECTS)
    public List<ProjectView> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<Project> projectStream = optionalPrefixName
                .map(projectRepo::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepo::streamAll);

        return projectStream
                .map(projectToProjectViewConverter::convert)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_PROJECT)
    public ProjectView createProject(@RequestParam("project_name") String projectName) {

        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        projectRepo
                .findByName(projectName)
                .ifPresent(project -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", projectName));
                });
        Project project = projectRepo.saveAndFlush(
                Project.builder()
                    .name(projectName)
                    .build()
        );

        return projectToProjectViewConverter.convert(project);
    }

    @PatchMapping(EDIT_PROJECT)
    public ProjectView editProject(
            @PathVariable("project_id") Long projectId,
            @RequestParam("project_name") String projectName) {

        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        Project project = getProjectOrThrowException(projectId);

        projectRepo
                .findByName(projectName)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", projectName));
                });

        project.setName(projectName);

        project = projectRepo.saveAndFlush(project);

        return projectToProjectViewConverter.convert(project);
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectView createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name") String projectName) {

        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        boolean isCreate = !optionalProjectId.isPresent();

        Project project;

        if(isCreate) {
            projectRepo
                    .findByName(projectName)
                    .ifPresent(proj -> {
                        throw new BadRequestException(String.format("Project \"%s\" already exists", projectName));
                    });
            project = projectRepo.saveAndFlush(
                    Project.builder()
                            .name(projectName)
                            .build()
            );
        }
        else {
            project = getProjectOrThrowException(optionalProjectId.get());

            projectRepo
                    .findByName(projectName)
                    .filter(anotherProject -> !Objects.equals(anotherProject.getId(), optionalProjectId))
                    .ifPresent(anotherProject -> {
                        throw new BadRequestException(String.format("Project \"%s\" already exists", projectName));
                    });

            project.setName(projectName);
            project = projectRepo.saveAndFlush(project);
        }
        return projectToProjectViewConverter.convert(project);
    }

    @DeleteMapping(DELETE_PROJECT)
    public Boolean deleteProject(@PathVariable("project_id") Long projectId) {

        getProjectOrThrowException(projectId);
        projectRepo.deleteById(projectId);

        return true;
    }

    private Project getProjectOrThrowException(Long projectId) {
        return projectRepo
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Project with \"%s\" doesn't exists", projectId))
                );
    }
}
