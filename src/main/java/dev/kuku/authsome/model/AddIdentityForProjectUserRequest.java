package dev.kuku.authsome.model;

import dev.kuku.authsome.services.project.api.model.IdentityOptions;
import dev.kuku.authsome.services.project.api.model.IdentityType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddIdentityForProjectUserRequest {
    public String projectId;
    public String userId;
    public String identity;
    public IdentityType identityType;
    public IdentityOptions options;
}
