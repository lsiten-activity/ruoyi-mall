package com.cyl.h5.pojo.response;

import com.cyl.ums.domain.Member;
import lombok.Data;

@Data
public class RegisterResponse {

    private boolean ifSuccess;

    private String message;

    private String token;

    private Member member;
}
