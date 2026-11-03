# springboot_basic – Spring Security 회원가입 / 로그인 / 마이페이지

Spring Boot + Spring Security + MyBatis + Thymeleaf + Oracle 로 만든 **회원 관리 기본 예제**입니다.
회원가입, 로그인/로그아웃, 마이페이지(정보 수정), 회원 탈퇴까지 구현했고, 아래에 배운 이론을 같이 정리했습니다.

---

## 1. 개발 환경

| 항목 | 내용 |
|---|---|
| JDK | **17 (Temurin 17)** |
| Spring Boot | 4.0.9 |
| 빌드 도구 | Maven |
| DB | Oracle (`jdbc:oracle:thin:@localhost:1521:testdb`) |
| 포트 | **8081** |
| 주요 라이브러리 | Spring Security, Thymeleaf(+ Security 확장), MyBatis, Lombok |

> IntelliJ: File → Project Structure → Project SDK 를 `temurin-17` 로 설정

---

## 2. 이론 정리

### 2-1. Spring Security 핵심 개념 3가지

| 개념 | 한 줄 설명 | 이 프로젝트에서 |
|---|---|---|
| **인증 (Authentication)** | "너 누구야?" – 아이디/비밀번호로 본인 확인 | 로그인 (`CustomUserDetailsService`) |
| **권한 (Authorization)** | "너 이거 해도 돼?" – 역할(Role)에 따라 접근 제한 | `USER` 권한, `anyRequest().authenticated()` |
| **암호화 (Encryption)** | 비밀번호를 원문 그대로 저장하지 않음 | `BCryptPasswordEncoder` |

> 정확히는 비밀번호 암호화는 복호화가 불가능한 **해시(단방향)** 방식입니다. BCrypt는 같은 비밀번호도 매번 다른 해시(salt)가 나오고, 비교는 `matches()`가 대신 해줍니다.

### 2-2. Spring Security 주요 기능

1. **로그인/로그아웃** – 로그인 화면, 로그아웃 URL을 설정만으로 제공
2. **권한 설정** – URL별 접근 제한 (예: `/admin/**` 는 ADMIN만)
3. **세션 관리** – 로그인 상태를 세션으로 유지, 세션 하이재킹 등 공격 방어
4. **기본 보안 설정 제공** – 아무 설정 없어도 모든 요청에 로그인 요구 (기본 계정은 `spring.security.user.*`)

### 2-3. 설정 방식이 바뀐 부분 (중요)

예전 글에 자주 나오는 방식은 **더 이상 쓰지 않습니다.**

```java
// (구) Spring Security 5 – 지금은 사용 불가
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    protected void configure(HttpSecurity http) { ... .antMatchers(...) }
}
```

```java
// (신) Spring Security 6+ – 이 프로젝트 방식
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth.requestMatchers(...)...);
    return http.build();
}
```

| 구 | 신 |
|---|---|
| `WebSecurityConfigurerAdapter` 상속 | `SecurityFilterChain` **빈 등록** |
| `authorizeRequests()` | `authorizeHttpRequests()` |
| `antMatchers()` | `requestMatchers()` |
| `.and()` 체이닝 | 람다(`Customizer`) 방식 |

### 2-4. 인증이 동작하는 흐름

```
[로그인 폼 제출]  POST /user/login-proc  (userId, userPassword)
        │
        ▼
  UsernamePasswordAuthenticationFilter   ← formLogin() 이 등록해 주는 필터
        │
        ▼
  CustomUserDetailsService.loadUserByUsername(userId)
        │   └─ UserMapper.findById() 로 DB 조회 → CustomUserDetails 반환
        ▼
  PasswordEncoder.matches(입력 비밀번호, DB의 해시)  → 일치하면 인증 성공
        │
        ▼
  SecurityContext 에 Authentication 저장 (세션 유지) → "/" 로 이동
```

- `UserDetails` : Security가 이해하는 "사용자 정보" 규격 (아이디, 비밀번호, 권한, 계정 상태)
- `UserDetailsService` : 아이디로 사용자를 찾아 `UserDetails` 를 돌려주는 규격
- 우리가 만든 `User`(DB 도메인)를 `CustomUserDetails` 로 **감싸서** Security에 넘겨주는 구조입니다.
- 권한은 `ROLE_` 접두사를 붙입니다. DB에는 `USER` 로 저장 → `ROLE_USER`

---

## 3. 프로젝트 구조

```
src/main/java/kopo/aisw/springboot_basic
├─ SpringbootBasicApplication.java
├─ config/SecurityConfig.java            # 보안 설정 (필터 체인, 암호화 빈)
├─ controller/
│   ├─ MainController.java               # GET /  → index
│   └─ UserController.java               # 회원가입/로그인/마이페이지/수정/탈퇴
├─ service/UserService.java              # 비밀번호 암호화 + 비즈니스 로직
├─ mapper/UserMapper.java                # MyBatis 인터페이스
├─ domain/User.java                      # tb_user 매핑 객체
└─ security/
    ├─ CustomUserDetails.java            # User → UserDetails 변환
    └─ CustomUserDetailsService.java     # 로그인 시 DB에서 사용자 조회

src/main/resources
├─ application.properties
├─ mapper/UserMapper.xml                 # SQL
└─ templates/
    ├─ index.html                        # 메인 (로그인 여부에 따라 메뉴 변경)
    └─ user/ login.html, signup.html, mypage.html
```

---

## 4. 구현 내용 (기능별)

### 4-1. SecurityConfig – 보안 정책

| 설정 | 의미 |
|---|---|
| `csrf.disable()` | 학습용으로 CSRF 보호 끔 (GET 로그아웃 링크 사용 가능) |
| `/css/**, /js/**, /images/**` permitAll | 정적 리소스는 로그인 없이 접근 |
| `/`, `/user/signup`, `/user/login` permitAll | 메인/회원가입/로그인은 누구나 접근 |
| `anyRequest().authenticated()` | **나머지는 전부 로그인 필요** (마이페이지 등) |
| `loginPage("/user/login")` | 직접 만든 로그인 화면 사용 |
| `loginProcessingUrl("/user/login-proc")` | 폼이 전송되는 주소 (컨트롤러 없이 Security가 처리) |
| `usernameParameter("userId")`, `passwordParameter("userPassword")` | 폼 input의 `name` 과 일치시켜야 함 (기본값은 username/password) |
| `logoutUrl("/user/logout")`, `logoutSuccessUrl("/")` | 로그아웃 후 메인으로 |
| `PasswordEncoder` 빈 (BCrypt) | 비밀번호 암호화/비교에 사용 |

### 4-2. 회원가입 – `POST /user/signup`

```java
public void registerUser(User user) {
    user.setUserPassword(passwordEncoder.encode(user.getUserPassword())); // 암호화
    user.setUserAuth("USER");                                             // 기본 권한
    userMapper.save(user);
}
```
→ 성공하면 `/user/login` 으로 redirect.

### 4-3. 로그인 / 로그아웃

- 로그인: 컨트롤러 코드 **없음**. `formLogin()` 설정 + `CustomUserDetailsService` 로 Security가 처리
- 실패 시 `/user/login?error` 로 이동 → 로그인 화면에 오류 메시지 표시
- 로그아웃: `/user/logout` (세션 종료 후 `/` 로 이동)

### 4-4. 메인 페이지 – 로그인 상태에 따라 다른 화면

```html
<span sec:authorize="!isAuthenticated()"> 로그인 / 회원가입 </span>
<span sec:authorize="isAuthenticated()">  마이페이지 / 로그아웃 </span>
<span sec:authentication="name"></span>님 환영합니다!
```
- `sec:authorize` : 조건에 맞을 때만 태그를 보여줌
- `sec:authentication="name"` : 로그인한 사용자 아이디 출력
- 필요 라이브러리: `thymeleaf-extras-springsecurity6`

### 4-5. 마이페이지 / 수정 / 탈퇴

| URL | 동작 |
|---|---|
| `GET /user/mypage` | `Authentication.getName()` 으로 로그인한 사용자 조회 후 화면 출력 |
| `POST /user/update` | 이름/비밀번호 수정 → `/user/mypage` |
| `POST /user/delete` | 회원 삭제 → `/user/logout` (세션 정리) |

- 로그인한 사용자 정보는 컨트롤러 파라미터 `Authentication auth` 로 받을 수 있습니다.
- **수정 시 주의 (추가 처리한 부분)**
  - 새 비밀번호를 비워두면 기존 비밀번호 유지 (빈 값을 암호화해 저장하는 사고 방지)
  - 폼의 `userId` 를 믿지 않고 `auth.getName()` 으로 덮어씀 → 남의 계정 수정 방지

---

## 5. 실행 방법

1. Oracle 에 `tb_user` 테이블 준비 (`user_id, user_password, user_name, user_auth, reg_date, mod_date`)
   - `user_password` 는 BCrypt 해시(60자)가 들어가므로 `VARCHAR2(100)` 이상
2. `application.properties` 의 DB 접속 정보 확인
3. `SpringbootBasicApplication` 실행 (JDK 17)
4. 브라우저에서 확인

| 순서 | 주소 | 확인 |
|---|---|---|
| 1 | http://localhost:8081/user/signup | 회원가입 → 로그인 화면으로 이동 |
| 2 | http://localhost:8081/user/login | 로그인 → 메인으로 이동, "OOO님 환영합니다!" |
| 3 | http://localhost:8081/user/mypage | 정보 수정 |
| 4 | 마이페이지 → 회원 탈퇴 | 탈퇴 후 로그아웃, 메인 이동 |
| 5 | 로그아웃 상태로 `/user/mypage` 접속 | 로그인 화면으로 튕기면 보안 설정 정상 |

---

## 6. 트러블슈팅 메모

| 증상 | 원인 | 해결 |
|---|---|---|
| `RequiredArgsConstructor` 빨간 줄 | `lombok` import 누락 | `import lombok.RequiredArgsConstructor;` |
| 컨트롤러 컴파일 오류 | 클래스 중괄호 `}` 위치 틀림, `Authentication`/`Model` import 누락 | 메서드를 클래스 안으로, import 추가 |
| 로그인이 항상 실패 | 폼 `name` 과 `usernameParameter` 불일치 | 둘 다 `userId`, `userPassword` 로 통일 |
| `Whitelabel 404` (`/` 또는 `/user/mypage`) | `index.html`, `mypage.html` 템플릿 없음 | `templates/` 아래에 생성 |
| 비밀번호 수정 후 로그인 불가 | 빈 비밀번호를 암호화해서 저장 | 비어 있으면 기존 값 유지 |
| 기본 계정 `user / 1234` 로그인 안 됨 | `UserDetailsService` 빈이 있으면 `spring.security.user.*` 는 무시됨 | DB에 가입한 계정으로 로그인 |
| `sec:` 태그 관련 오류 | Thymeleaf Security 확장과 Boot/Security 버전 호환 문제 | 에러 로그 확인 후 의존성 점검 |

---

## 7. 한 줄 요약

> **SecurityFilterChain 으로 "누가 어디까지 접근할지" 정하고,
> UserDetailsService 로 "누구인지" DB에서 확인하고,
> PasswordEncoder(BCrypt) 로 "비밀번호를 안전하게" 저장한다.**
