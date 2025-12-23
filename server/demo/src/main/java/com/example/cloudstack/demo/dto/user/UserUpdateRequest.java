package com.example.cloudstack.demo.dto.user;

import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateRequest {

    private String nickname;
    private String avatar;
    private String bio;
    private String profession;
    private String location;
    private String website;
}
