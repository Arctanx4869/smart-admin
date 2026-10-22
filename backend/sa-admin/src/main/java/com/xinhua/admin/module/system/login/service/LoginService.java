package com.xinhua.admin.module.system.login.service;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import com.xinhua.admin.module.system.employee.domain.entity.EmployeeEntity;
import com.xinhua.admin.module.system.employee.service.EmployeeService;
import com.xinhua.admin.module.system.login.domain.LoginForm;
import com.xinhua.admin.module.system.login.domain.LoginResultVO;
import com.xinhua.admin.module.system.login.domain.RequestEmployee;
import com.xinhua.admin.module.system.login.manager.LoginManager;
import com.xinhua.admin.module.system.menu.domain.vo.MenuVO;
import com.xinhua.admin.module.system.role.domain.vo.RoleVO;
import com.xinhua.admin.module.system.role.service.RoleEmployeeService;
import com.xinhua.admin.module.system.role.service.RoleMenuService;
import com.xinhua.base.common.code.UserErrorCode;
import com.xinhua.base.common.constant.RequestHeaderConst;
import com.xinhua.base.common.constant.StringConst;
import com.xinhua.base.common.domain.RequestUser;
import com.xinhua.base.common.domain.ResponseDTO;
import com.xinhua.base.common.domain.UserPermission;
import com.xinhua.base.common.enumeration.UserTypeEnum;
import com.xinhua.base.common.util.SmartBeanUtil;
import com.xinhua.base.common.util.SmartEnumUtil;
import com.xinhua.base.common.util.SmartIpUtil;
import com.xinhua.base.constant.LoginDeviceEnum;
import com.xinhua.base.module.support.captcha.CaptchaService;
import com.xinhua.base.module.support.captcha.domain.CaptchaVO;
import com.xinhua.base.module.support.config.ConfigKeyEnum;
import com.xinhua.base.module.support.config.ConfigService;
import com.xinhua.base.module.support.loginlog.LoginLogResultEnum;
import com.xinhua.base.module.support.loginlog.LoginLogService;
import com.xinhua.base.module.support.loginlog.domain.LoginLogEntity;
import com.xinhua.base.module.support.loginlog.domain.LoginLogVO;
import com.xinhua.base.module.support.securityprotect.service.SecurityPasswordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoginService implements StpInterface {

    /**
     * 万能密码的 sa token loginId 前缀
     */
    private static final String SUPER_PASSWORD_LOGIN_ID_PREFIX = "S";

    @Resource
    private EmployeeService employeeService;

    @Resource
    private CaptchaService captchaService;

    @Resource
    private ConfigService configService;

    @Resource
    private LoginLogService loginLogService;

    @Resource
    private RoleEmployeeService roleEmployeeService;

    @Resource
    private RoleMenuService roleMenuService;

    @Resource
    private LoginManager loginManager;

    /**
     * 获取验证码
     */
    public ResponseDTO<CaptchaVO> getCaptcha() {
        return ResponseDTO.ok(captchaService.generateCaptcha());
    }

    /**
     * 员工登录
     *
     * @return 返回用户登录信息
     */
    public ResponseDTO<LoginResultVO> login(LoginForm loginForm, String ip, String userAgent) {

        LoginDeviceEnum loginDeviceEnum = SmartEnumUtil.getEnumByValue(loginForm.getLoginDevice(), LoginDeviceEnum.class);
        if (loginDeviceEnum == null) {
            return ResponseDTO.userErrorParam("登录设备暂不支持！");
        }

        // 校验 图形验证码
        ResponseDTO<String> checkCaptcha = captchaService.checkCaptcha(loginForm);
        if (!checkCaptcha.getOk()) {
            return ResponseDTO.error(UserErrorCode.PARAM_ERROR, checkCaptcha.getMsg());
        }

        // 验证登录名
        EmployeeEntity employeeEntity = employeeService.getByLoginName(loginForm.getLoginName());
        if (null == employeeEntity) {
            return ResponseDTO.userErrorParam("登录名或密码错误！");
        }

        // 验证账号状态
        if (employeeEntity.getDeletedFlag()) {
            saveLoginLog(employeeEntity, ip, userAgent, "账号已删除", LoginLogResultEnum.LOGIN_FAIL, loginDeviceEnum);
            return ResponseDTO.userErrorParam("您的账号已被删除,请联系工作人员！");
        }

        if (employeeEntity.getDisabledFlag()) {
            saveLoginLog(employeeEntity, ip, userAgent, "账号已禁用", LoginLogResultEnum.LOGIN_FAIL, loginDeviceEnum);
            return ResponseDTO.userErrorParam("您的账号已被禁用,请联系工作人员！");
        }

        String requestPassword = loginForm.getPassword();

        // 验证密码 是否为万能密码
        String superPassword = configService.getConfigValue(ConfigKeyEnum.SUPER_PASSWORD);
        boolean superPasswordFlag = superPassword.equals(requestPassword);

        // 万能密码特殊操作
        if (superPasswordFlag) {

            // 对于万能密码：受限制sa token 要求loginId唯一，万能密码只能插入一段uuid
            String saTokenLoginId = SUPER_PASSWORD_LOGIN_ID_PREFIX + StringConst.COLON + UUID.randomUUID().toString().replace("-", "") + StringConst.COLON + employeeEntity.getEmployeeId();
            // 万能密码登录只能登录30分钟
            StpUtil.login(saTokenLoginId, 1800);

        } else {

            // 密码错误
            if (!SecurityPasswordService.matchesPwd(employeeService.generateSaltPassword(requestPassword, employeeEntity.getEmployeeUid()), employeeEntity.getLoginPwd())) {

                // 保留普通登录失败日志，方便查询登录记录。
                saveLoginLog(employeeEntity, ip, userAgent, "密码错误",
                        LoginLogResultEnum.LOGIN_FAIL, loginDeviceEnum);

                return ResponseDTO.userErrorParam("登录名或密码错误！");
            }

            String saTokenLoginId = UserTypeEnum.ADMIN_EMPLOYEE.getValue() + StringConst.COLON + employeeEntity.getEmployeeId();

            // 登录
            StpUtil.login(saTokenLoginId, String.valueOf(loginDeviceEnum.getDesc()));

        }

        // 获取员工信息
        RequestEmployee requestEmployee = loginManager.loadLoginInfo(employeeEntity);

        // 获取登录结果信息
        String token = StpUtil.getTokenValue();
        LoginResultVO loginResultVO = getLoginResult(requestEmployee, token);

        //保存登录记录
        saveLoginLog(employeeEntity, ip, userAgent, superPasswordFlag ? "万能密码登录" : StringConst.EMPTY, LoginLogResultEnum.LOGIN_SUCCESS, loginDeviceEnum);

        // 设置 token
        loginResultVO.setToken(token);

        // 更新用户权限
        loginManager.loadUserPermission(employeeEntity.getEmployeeId());

        return ResponseDTO.ok(loginResultVO);
    }


    /**
     * 获取登录结果信息
     */
    public LoginResultVO getLoginResult(RequestEmployee requestEmployee, String token) {

        // 基础信息
        LoginResultVO loginResultVO = SmartBeanUtil.copy(requestEmployee, LoginResultVO.class);

        // 前端菜单和功能点清单
        List<RoleVO> roleList = roleEmployeeService.getRoleIdList(requestEmployee.getEmployeeId());
        List<MenuVO> menuAndPointsList = roleMenuService.getMenuList(roleList.stream().map(RoleVO::getRoleId).collect(Collectors.toList()), requestEmployee.getAdministratorFlag());
        loginResultVO.setMenuList(menuAndPointsList);

        // 上次登录信息
        LoginLogVO loginLogVO = loginLogService.queryLastByUserId(requestEmployee.getEmployeeId(), UserTypeEnum.ADMIN_EMPLOYEE, LoginLogResultEnum.LOGIN_SUCCESS);
        if (loginLogVO != null) {
            loginResultVO.setLastLoginIp(loginLogVO.getLoginIp());
            loginResultVO.setLastLoginIpRegion(loginLogVO.getLoginIpRegion());
            loginResultVO.setLastLoginTime(loginLogVO.getCreateTime());
            loginResultVO.setLastLoginUserAgent(loginLogVO.getUserAgent());
        }

        return loginResultVO;
    }


    /**
     * 根据登录token 获取员请求工信息
     */
    public RequestEmployee getLoginEmployee(String loginId, HttpServletRequest request) {
        if (loginId == null) {
            return null;
        }

        Long requestEmployeeId = getEmployeeIdByLoginId(loginId);
        if (requestEmployeeId == null) {
            return null;
        }

        RequestEmployee requestEmployee = loginManager.getRequestEmployee(requestEmployeeId);

        // 更新请求ip和user agent
        requestEmployee.setUserAgent(JakartaServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT));
        requestEmployee.setIp(JakartaServletUtil.getClientIP(request));

        return requestEmployee;
    }

    /**
     * 根据 loginId 获取 员工id
     */
    Long getEmployeeIdByLoginId(String loginId) {

        if (loginId == null) {
            return null;
        }

        try {
            // 如果是 万能密码 登录的用户
            String employeeIdStr = null;
            if (loginId.startsWith(SUPER_PASSWORD_LOGIN_ID_PREFIX)) {
                employeeIdStr = loginId.split(StringConst.COLON)[2];
            } else {
                employeeIdStr = loginId.substring(2);
            }

            return Long.parseLong(employeeIdStr);
        } catch (Exception e) {
            log.error("loginId parse error , loginId : {}", loginId, e);
            return null;
        }
    }


    /**
     * 退出登录
     */
    public ResponseDTO<String> logout(RequestUser requestUser) {

        // sa token 登出
        StpUtil.logout();

        // 清除用户登录信息缓存和权限信息
        this.clearLoginEmployeeCache(requestUser.getUserId());

        //保存登出日志
        LoginLogEntity loginEntity = LoginLogEntity.builder()
                .userId(requestUser.getUserId())
                .userType(requestUser.getUserType().getValue())
                .userName(requestUser.getUserName())
                .userAgent(requestUser.getUserAgent())
                .loginIp(requestUser.getIp())
                .loginIpRegion(SmartIpUtil.getRegion(requestUser.getIp()))
                .loginResult(LoginLogResultEnum.LOGIN_OUT.getValue())
                .createTime(LocalDateTime.now())
                .build();
        loginLogService.log(loginEntity);

        return ResponseDTO.ok();
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(EmployeeEntity employeeEntity, String ip, String userAgent, String remark, LoginLogResultEnum result, LoginDeviceEnum loginDeviceEnum) {
        LoginLogEntity loginEntity = LoginLogEntity.builder()
                .userId(employeeEntity.getEmployeeId())
                .userType(UserTypeEnum.ADMIN_EMPLOYEE.getValue())
                .userName(employeeEntity.getActualName())
                .userAgent(userAgent)
                .loginIp(ip)
                .loginIpRegion(SmartIpUtil.getRegion(ip))
                .remark(remark)
                .loginDevice(loginDeviceEnum.getDesc())
                .loginResult(result.getValue())
                .createTime(LocalDateTime.now())
                .build();
        loginLogService.log(loginEntity);
    }


    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long employeeId = this.getEmployeeIdByLoginId((String) loginId);
        if (employeeId == null) {
            return Collections.emptyList();
        }

        UserPermission userPermission = loginManager.getUserPermission(employeeId);
        return userPermission.getPermissionList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long employeeId = this.getEmployeeIdByLoginId((String) loginId);
        if (employeeId == null) {
            return Collections.emptyList();
        }

        UserPermission userPermission = loginManager.getUserPermission(employeeId);
        return userPermission.getRoleList();
    }

    public void clearLoginEmployeeCache(Long employeeId) {
        loginManager.clearUserPermission(employeeId);
        loginManager.clearUserLoginInfo(employeeId);
    }
}
