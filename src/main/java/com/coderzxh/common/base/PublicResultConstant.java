package com.coderzxh.common.base;

public class PublicResultConstant {

    public static final String FAILED  = "系统错误";

    public static final String SUCCEED = "操作成功";

    public static final String UNAUTHORIZED  = "获取登录用户信息失败";

    public static final String ERROR  = "操作失败";

    public static final String DATA_ERROR  = "数据操作错误";

    public static final String PARAM_ERROR  = "参数错误";

    public static final String PARAM_NULL  = "参数为空值";

    public static final String INVALID_USERNAME_PASSWORD  = "用户名或密码错误";

    public static final String INVALID_RE_PASSWORD  = "两次输入密码不一致";

    public static final String INVALID_USER  = "用户不存在";
    public static final String INVALID_EMAIL  = "邮箱不存在";

    public static final String INVALID_USER_EXIST  = "用户已存在";

    public static final String INVALID_ROLE  = "角色不存在";

    public static final String ROLE_USER_USED  = "角色使用中，不可删除";

    public static final String USER_NO_PERMITION  = "当前用户无该接口权限";

    public static final String VERIFY_PARAM_ERROR  = "校验码错误";

    public static final String VERIFY_PARAM_PASS  = "校验码过期";

    public static final String MOBILE_ERROR  = "手机号格式错误";

    public static final String UPDATE_ROLEINFO_ERROR  = "更新角色信息失败";

    public static final String UPDATE_SYSADMIN_INFO_ERROR  = "不能修改管理员信息!";

    public static final String EMAIL_ERROR  = "邮箱格式错误";

    public static final String DEVICE_NEVER_EXIST_ERROR  = "此设备从未与平台建立连接，请使其首次上线或登录";

    public static final String DATABASE_UNIQUENESS = "数据库数据不唯一";
    public static final String DEVICE_ALREADY_BIND = "该设备已经被该用户绑定";

    public static final String INVALID_DEVICE_PASSWORD = "设备密码错误";
    public static final String DEVICE_PASSWORD_FORMAT_ERROR = "设备密码在6-20位之间";
    public static final String PASSWORD_FORMAT_ERROR = "密码在6-20位之间";
    public static final String NEED_DEVICE_PASSWORD = "需要设备密码参数";
    public static final String USERDEVICE_NOT_BIND_ERROR = "用户与该设备未绑定";
    public static final String DEVICE_NOT_EXIST_ERROR = "设备不存在";
    public static final String DEVICE_EXIST_ERROR = "设备已经存在";

    public static final String SEND_EMAIL_ERROR = "发送邮件失败";
    public static final String SEND_EMAIL_ADDRESS_ERROR = "此邮箱地址无法连接";
    public static final String INVALID_EMAIL_EXIST = "邮箱地址已存在";
    public static final String DEVICE_OFFLINE = "设备离线";

    public static final String FILE_NOT_EXIST_ERROR = "文件不存在";
    public static final String FILE_UPLOAD_FAIL = "文件上传失败";
    public static final String UNBIND_USER_ALL_DEVICE_FAIL = "解绑该用户下所有设备失败";
}
