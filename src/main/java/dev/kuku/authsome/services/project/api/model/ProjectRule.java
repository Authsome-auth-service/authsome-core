package dev.kuku.authsome.services.project.api.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRule {
    public int maxSessions; //The max no. of sessions allowed per user
    public String accessTokenSigningToken; //The secret used to sign access tokens
    public TokenRotationMode refreshTokenRotationMode; //The refresh token rotation mode
    public int maxIdentityPerUser; //-1 for no limit
}
