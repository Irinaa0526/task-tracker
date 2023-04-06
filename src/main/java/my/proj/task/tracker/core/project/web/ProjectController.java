package my.proj.task.tracker.core.project.web;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.project.ProjectRepo;
import my.proj.task.tracker.core.project.converter.ProjectToProjectViewConverter;
import my.proj.task.tracker.error.BadRequestException;
import my.proj.task.tracker.error.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @GetMapping(FETCH_PROJECTS)
    @Transactional
    public List<ProjectView> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        // проверяем фильтр на пустоту (если пустой - optionalPrefixName будет пустым)
        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<Project> projectStream = optionalPrefixName
                .map(projectRepo::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepo::streamAllBy);

        return projectStream
                .map(projectToProjectViewConverter::convert)
                .collect(Collectors.toList());
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectView createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name") String projectName) {

        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        // если не передали id, значит операция создания проекта, иначе - редактирование
        boolean isCreate = !optionalProjectId.isPresent();

        Project project;

        if (isCreate) {
            projectRepo
                    .findByName(projectName)
                    .ifPresent(proj -> {
                        throw new BadRequestException(String.format("Project with name '%s' already exists", projectName));
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
                        throw new BadRequestException(String.format("Project with name '%s' already exists", projectName));
                    });

            project.setName(projectName);
            project.setUpdatedAt(Instant.now());
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
                        new NotFoundException(String.format("Project with id = %s doesn't exists", projectId))
                );
    }
}
