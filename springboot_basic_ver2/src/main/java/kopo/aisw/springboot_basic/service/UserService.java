package kopo.aisw.springboot_basic.service;

import kopo.aisw.springboot_basic.domain.User;
import kopo.aisw.springboot_basic.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    // 코드 추가 1
    private final PasswordEncoder passwordEncoder;

    public void registerUser(User user) {
        // 코드 추가 2 : 비밀번호 암호화
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        // 코드 추가 3 : 기본 권한 설정
        user.setUserAuth("USER");

        userMapper.save(user);
    }

    public User getUserById(String userId) {
        return userMapper.findById(userId);
    }

    public void updateUser(User user) {
        if (user.getUserPassword() == null || user.getUserPassword().isBlank()) {
            // 새 비밀번호를 비워두면 기존 비밀번호 유지
            // (비워둔 채로 암호화하면 빈 비밀번호로 저장되어 로그인이 안 됨)
            User saved = userMapper.findById(user.getUserId());
            user.setUserPassword(saved.getUserPassword());
        } else {
            // 코드 추가 : 비밀번호 암호화
            user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        }
        // 코드 추가 : 기본 권한 설정
        user.setUserAuth("USER");

        userMapper.update(user);
    }

    public void deleteUser(String userId) {
        userMapper.delete(userId);
    }
}
