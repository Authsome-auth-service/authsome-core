package dev.kuku.authsome.controller.project;

import dev.kuku.authsome.coordinator.ProjectCoordinator;
import dev.kuku.authsome.model.CreateProjectRequest;
import dev.kuku.authsome.model.ResponseModel;
import dev.kuku.authsome.services.project.api.model.FetchedTenantProject;
import dev.kuku.authsome.services.tenant.api.dto.FetchedTenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/project")
@Slf4j
@RequiredArgsConstructor
public class ProjectController {
    final ProjectCoordinator projectCoordinator;

    @PostMapping("/")
    public ResponseModel<FetchedTenantProject> createProject(@RequestBody CreateProjectRequest body) {
        log.debug("Received request to create project {}", body);
        FetchedTenant currentUser = getCurrentUser();
        FetchedTenantProject createdProject = projectCoordinator.createProject(currentUser.id(), body);
        return ResponseModel.of(createdProject);
    }

    FetchedTenant getCurrentUser() {
        FetchedTenant currentUser = (FetchedTenant) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Current user is {}", currentUser);
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return currentUser;
    }

}
