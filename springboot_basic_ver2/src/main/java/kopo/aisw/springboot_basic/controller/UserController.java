package kopo.aisw.springboot_basic.controller;

import kopo.aisw.springboot_basic.domain.User;
import kopo.aisw.springboot_basic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String signupForm() {
        return "user/signup";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    // 코드 추가 1 : signup 처리
    @PostMapping("/signup")
    public String signup(User user) {
        userService.registerUser(user);
        return "redirect:/user/login";
    }

    // 마이페이지 (로그인한 사용자 정보 조회)
    @GetMapping("/mypage")
    public String myPage(Authentication auth, Model model) {
        User user = userService.getUserById(auth.getName());
        model.addAttribute("user", user);
        return "user/mypage";
    }

    // 사용자 정보 수정
    @PostMapping("/update")
    public String updateUser(User user, Authentication auth) {
        // 폼에서 넘어온 userId 대신 로그인한 본인 아이디를 사용 (다른 사람 계정 수정 방지)
        user.setUserId(auth.getName());
        userService.updateUser(user);
        return "redirect:/user/mypage";
    }

    // 회원 탈퇴
    @PostMapping("/delete")
    public String deleteUser(Authentication auth) {
        userService.deleteUser(auth.getName());
        return "redirect:/user/logout";
    }
}
