package dev.kuku.authsome.model;

import dev.kuku.authsome.services.project.api.model.ProjectSignInKey;
import lombok.ToString;

@ToString
public class SignInUserWithCredentialRequest {
    public String key;
    public ProjectSignInKey keyType;
    public String password;
}
