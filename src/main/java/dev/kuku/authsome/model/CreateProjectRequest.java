package dev.kuku.authsome.model;

import dev.kuku.authsome.services.project.api.model.ProjectRule;
import lombok.ToString;

@ToString
public class CreateProjectRequest {
    public String projectName;
    public ProjectRule rules;
}
