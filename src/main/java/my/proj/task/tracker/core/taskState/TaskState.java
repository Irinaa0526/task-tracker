package my.proj.task.tracker.core.taskState;

import lombok.*;
import my.proj.task.tracker.core.project.Project;
import my.proj.task.tracker.core.task.Task;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_state")
public class TaskState {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long taskStateId;

    @Column(unique = true)
    private String name;

    @OneToOne
    @JoinColumn(name="left_task_state_id", referencedColumnName = "taskStateId")
    private TaskState leftTaskState;

    @OneToOne
    @JoinColumn(name="right_task_state_id", referencedColumnName = "taskStateId")
    private TaskState rightTaskState;

    @Builder.Default
    private Instant createdAt = Instant.now();

//    @ManyToOne
//    private Project project;

    @Builder.Default
    @OneToMany
    @JoinColumn(name="task_state_id", referencedColumnName = "taskStateId")
    private List<Task> tasks = new ArrayList<>();

    public Optional<TaskState> getLeftTaskState() {
        return Optional.ofNullable(leftTaskState);
    }

    public Optional<TaskState> getRightTaskState() {
        return Optional.ofNullable(rightTaskState);
    }

}
