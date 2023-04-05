package my.proj.task.tracker.core.project.converter;

import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.project.web.ProjectView;
import org.springframework.stereotype.Component;

@Component
public class ProjectToProjectViewConverter {

    public ProjectView convert(Project project) {
        return ProjectView.builder()
                .id(project.getId())
                .name(project.getName())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
