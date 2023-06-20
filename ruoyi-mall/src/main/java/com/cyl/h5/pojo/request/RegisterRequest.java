package com.cyl.h5.pojo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterRequest {

    @ApiModelProperty("手机号")
    @NotBlank
    private String mobile;

    @ApiModelProperty("密码")
    @NotBlank
    private String password;

    @ApiModelProperty("uuid")
    @NotBlank
    private String uuid;

    @ApiModelProperty("验证码")
    @NotBlank
    private String code;

}
