package dev.kuku.authsome.model;

import dev.kuku.authsome.services.tenant.api.dto.IdentityType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TenantSignInRequest {
    public String identity;
    public IdentityType identityType;
    public String password;
}
