package dev.kuku.authsome.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateProjectUserRequest {
    public String username;
    public String password;
    public String projectId;
}
