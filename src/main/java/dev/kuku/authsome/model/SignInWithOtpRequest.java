package dev.kuku.authsome.model;

import dev.kuku.authsome.services.project.api.model.IdentityType;

public class SignInWithOtpRequest {
    public String identity;
    public IdentityType identityType;
}
