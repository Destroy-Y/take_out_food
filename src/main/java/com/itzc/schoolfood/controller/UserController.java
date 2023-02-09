package com.itzc.schoolfood.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itzc.schoolfood.common.R;
import com.itzc.schoolfood.entity.User;
import com.itzc.schoolfood.service.UserService;
import com.itzc.schoolfood.utils.MailUtils;
import com.itzc.schoolfood.utils.SMSUtils;
import com.itzc.schoolfood.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 客户端用户
 */

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送邮箱短信
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user,HttpSession session) throws MessagingException {
        //获取邮箱号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)){
            //生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            //调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage();   //调用方法
            //这里的phone其实就是邮箱，code是我们生成的验证码
            MailUtils.sendTestMail(phone, code);

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);

            return R.success("验证码发送成功");
        }

        return R.error("验证码发送失败");
    }

    /**
     * 移动端手机用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        //进行验证码比对
        if (codeInSession != null && codeInSession.equals(code)){
            //如果能比对成功，说明登陆成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user == null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                user.setName("用户"+codeInSession);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("登录失败");
    }

    /**
     * 用户退出登录
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        //清理session中的用户id
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }

}
