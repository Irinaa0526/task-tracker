package my.proj.task.tracker.core.project;

import lombok.RequiredArgsConstructor;
import my.proj.task.tracker.core.project.converter.ProjectToProjectViewConverter;
import my.proj.task.tracker.core.project.web.ProjectView;
import my.proj.task.tracker.error.BadRequestException;
import my.proj.task.tracker.helpers.ControllerHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepo projectRepo;
    private final ProjectToProjectViewConverter projectToProjectViewConverter;
    private final ControllerHelper controllerHelper;

    public List<ProjectView> fetchProjects (Optional<String> optionalPrefixName) {
        // проверяем фильтр на пустоту (если пустой - optionalPrefixName будет пустым)
        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.isBlank());

        Stream<Project> projectStream = optionalPrefixName
                .map(projectRepo::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepo::streamAllBy);

        return projectStream
                .map(projectToProjectViewConverter::convert)
                .collect(Collectors.toList());
    }

    public ProjectView createOrUpdateProject(Optional<Long> optionalProjectId, String projectName) {
        if (projectName.isBlank()) {
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
            project = controllerHelper.getProjectOrThrowException(optionalProjectId.get());

            projectRepo
                    .findByName(projectName)
                    .filter(anotherProject -> !Objects.equals(anotherProject.getProjectId(), optionalProjectId))
                    .ifPresent(anotherProject -> {
                        throw new BadRequestException(String.format("Project with name '%s' already exists", projectName));
                    });

            project.setName(projectName);
            project.setUpdatedAt(Instant.now());
            project = projectRepo.saveAndFlush(project);
        }
        return projectToProjectViewConverter.convert(project);
    }

    public Boolean deleteProject(Long projectId) {
        controllerHelper.getProjectOrThrowException(projectId);
        projectRepo.deleteById(projectId);

        return true;
    }
}
