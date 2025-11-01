package dev.kuku.authsome.services.tenant.api.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TenantAndRefreshToken {
    public FetchedTenant tenant;
    public String refreshToken;
}
