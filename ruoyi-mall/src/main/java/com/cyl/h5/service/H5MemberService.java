package com.cyl.h5.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyl.h5.pojo.request.H5AccountLoginRequest;
import com.cyl.h5.pojo.request.H5SmsLoginRequest;
import com.cyl.h5.pojo.request.RegisterRequest;
import com.cyl.h5.pojo.response.RegisterResponse;
import com.cyl.h5.pojo.response.ValidatePhoneResponse;
import com.cyl.h5.pojo.response.H5LoginResponse;
import com.cyl.manager.ums.domain.Member;
import com.cyl.manager.ums.domain.MemberWechat;
import com.cyl.manager.ums.mapper.MemberMapper;
import com.cyl.manager.ums.mapper.MemberWechatMapper;
import com.cyl.manager.ums.pojo.vo.MemberVO;
import com.cyl.wechat.WechatAuthService;
import com.cyl.wechat.response.WechatUserAuth;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginMember;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.AesCryptoUtils;
import com.ruoyi.common.utils.PhoneUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.LocalDataUtil;
import com.ruoyi.framework.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
public class H5MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TokenService tokenService;

    @Value("${aes.key}")
    private String aesKey;

    @Autowired
    private WechatAuthService wechatAuthService;

    @Autowired
    private MemberWechatMapper memberWechatMapper;

    /**
     * 注册
     * @param request 注册请求体
     * @return 结果
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request){
        LocalDateTime optDate = LocalDateTime.now();
        RegisterResponse response = new RegisterResponse();
        //校验验证码
        this.validateVerifyCode(request.getUuid(), request.getMobile(), request.getCode());
        //创建会员
        Member member = new Member();
        member.setPhoneEncrypted(AesCryptoUtils.encrypt(aesKey, request.getMobile()));
        member.setPhoneHidden(PhoneUtils.hidePhone(request.getMobile()));
        member.setPassword(SecurityUtils.encryptPassword(request.getPassword()));
        member.setNickname("用户" + request.getMobile().substring(7,11));
        member.setStatus(Constants.MEMBER_ACCOUNT_STATUS.NORMAL);
        member.setGender(0);
        member.setCreateTime(optDate);
       int rows = memberMapper.insert(member);
       if (rows < 1){
           throw new RuntimeException("注册失败，请重试");
       }
       //调用微信授权业务拿到openId等
        WechatUserAuth userToken = wechatAuthService.getUserToken(request.getWechatCode());
       if (userToken == null){
           throw new RuntimeException("授权失败，请重试");
       }
        MemberWechat memberWechat = new MemberWechat();
        memberWechat.setMemberId(member.getId());
        memberWechat.setOpenid(userToken.getOpenid());
        memberWechat.setAccessToken(userToken.getAccess_token());
        memberWechat.setExpiresIn(userToken.getExpires_in());
        memberWechat.setRefreshToken(userToken.getRefresh_token());
        memberWechat.setCreateTime(optDate);
        memberWechat.setCreateBy(member.getId());
        rows = memberWechatMapper.insert(memberWechat);
        if (rows < 1){
            throw new RuntimeException("注册失败，请重试");
        }
        //注册成功直接返回token了
        H5LoginResponse loginResponse = getLoginResponse(member.getId());
        response.setToken(loginResponse.getToken());
        return response;
    }

    public ValidatePhoneResponse validate(String phone) {
        ValidatePhoneResponse response = new ValidatePhoneResponse();
        byte[] decodedBytes = Base64.getDecoder().decode(phone);
        phone = new String(decodedBytes);
        QueryWrapper<Member> qw = new QueryWrapper<>();
        qw.eq("phone_encrypted", AesCryptoUtils.encrypt(aesKey, phone));
        Member member = memberMapper.selectOne(qw);
        if (member != null){
            throw new RuntimeException("该手机号已被占用");
        }
        response.setIfSuccess(true);
        response.setMessage("该手机号可用");
        return response;
    }

    /**
     * 账号密码登录
     * @param data
     * @return
     */
    public H5LoginResponse accountLogin(String data) {
        if (StringUtils.isEmpty(data)){
            throw new RuntimeException(Constants.LOGIN_INFO.WRONG);
        }
        // 解码 转 对象
        H5AccountLoginRequest request = JSON.parseObject(new String(Base64Utils.decodeFromString(data)), H5AccountLoginRequest.class);
        log.info("account login request:{}", JSONUtil.toJsonStr(request));
        QueryWrapper<Member> qw = new QueryWrapper<>();
        qw.eq("phone_encrypted", AesCryptoUtils.encrypt(aesKey, request.getMobile()));
        Member member = memberMapper.selectOne(qw);
        if (member == null){
            throw new RuntimeException(Constants.LOGIN_INFO.WRONG);
        }
        validateMemberStatus(member);
        //check 密码
        if (!SecurityUtils.matchesPassword(request.getPassword(), member.getPassword())){
            throw new RuntimeException(Constants.LOGIN_INFO.WRONG);
        }
        return getLoginResponse(member.getId());
    }

    public H5LoginResponse smsLogin(String data){
        if (StringUtils.isEmpty(data)){
            throw new RuntimeException(Constants.LOGIN_INFO.WRONG);
        }
        H5SmsLoginRequest request = JSON.parseObject(new String(Base64Utils.decodeFromString(data)), H5SmsLoginRequest.class);
        //校验验证码
        this.validateVerifyCode(request.getUuid(), request.getMobile(), request.getCode());
        //查会员
        QueryWrapper<Member> qw = new QueryWrapper<>();
        qw.eq("phone_encrypted", AesCryptoUtils.encrypt(aesKey, request.getMobile()));
        Member member = memberMapper.selectOne(qw);
        if (member == null){
            throw new RuntimeException(Constants.LOGIN_INFO.TO_REGISTER);
        }
        //校验会员状态
        validateMemberStatus(member);
        return getLoginResponse(member.getId());
    }

    /**
     * 校验会员状态
     * @param member 会员信息
     */
    private void validateMemberStatus(Member member) {
        if (Constants.MEMBER_ACCOUNT_STATUS.FORBIDDEN == member.getStatus()){
            throw new RuntimeException(Constants.LOGIN_INFO.FORBIDDEN);
        }
    }

    /**
     * 校验验证码有效性
     * @param uuid 唯一标识
     * @param phone 手机号
     * @param inputCode 输入的验证码
     */
    private void validateVerifyCode(String uuid, String phone, String inputCode){
        String key = uuid + "_" + phone;
        String redisCode = redisCache.getCacheObject(key);
        if (redisCode ==  null){
            throw new RuntimeException(Constants.VERIFY_CODE_INFO.EXPIRED);
        }else if (!redisCode.equals(inputCode)){
            throw new RuntimeException(Constants.VERIFY_CODE_INFO.WRONG);
        }
        //删除缓存
        redisCache.deleteObject(key);
    }

    /**
     * 封装登录响应
     * @param memberId 登录会员id
     * @return 结果
     */
    private H5LoginResponse getLoginResponse(Long memberId){
        LoginMember loginMember = new LoginMember();
        loginMember.setMemberId(memberId);
        String token = tokenService.createMemberToken(loginMember);
        H5LoginResponse response = new H5LoginResponse();
        response.setToken(token);
        return response;
    }

    public MemberVO getMemberInfo() {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        MemberVO memberVO = new MemberVO();
        BeanUtils.copyProperties(member, memberVO);
        memberVO.setPhone(AesCryptoUtils.decrypt(aesKey, member.getPhoneEncrypted()));
        return memberVO;
    }
}
