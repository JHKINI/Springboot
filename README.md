# Spring Boot Basic -- 회원 관리 & 게시판

Spring Boot와 Spring Security, MyBatis, Thymeleaf, Oracle을 활용해
구현한 웹 애플리케이션 실습 프로젝트입니다.

회원가입과 로그인부터 회원 정보 관리, 게시판 CRUD, 조회수 처리,
페이지네이션까지 단계적으로 구현하며 Spring 기반 웹 애플리케이션의 기본
구조와 데이터 처리 흐름을 학습했습니다.

------------------------------------------------------------------------

## 1. 프로젝트 개요

### 주요 기능

  -----------------------------------------------------------------------
  구분                    기능                    설명
  ----------------------- ----------------------- -----------------------
  회원 관리               회원가입                사용자 정보를 DB에
                                                  저장하고 비밀번호를
                                                  BCrypt로 해시 처리

  회원 관리               로그인 / 로그아웃       Spring Security를
                                                  이용한 인증 및 세션
                                                  관리

  회원 관리               마이페이지              로그인한 사용자의 정보
                                                  조회

  회원 관리               회원 정보 수정          이름 및 비밀번호 수정

  회원 관리               회원 탈퇴               회원 정보 삭제 후
                                                  로그아웃

  게시판                  게시글 목록             등록된 게시글 목록 조회

  게시판                  게시글 작성             로그인한 사용자의
                                                  아이디를 작성자로
                                                  지정해 저장

  게시판                  게시글 상세 조회        게시글 내용 및 작성
                                                  정보 조회

  게시판                  게시글 수정 / 삭제      게시글 작성자 확인을
                                                  통한 수정·삭제 화면
                                                  제어

  게시판                  조회수                  상세 조회 시 조회수
                                                  증가

  게시판                  페이지네이션            Criteria를 이용한
                                                  페이징 기준 관리 단계
  -----------------------------------------------------------------------

> 페이지네이션은 제공된 실습 내용에서 `Criteria` 클래스 생성 단계까지
> 확인됩니다. 구체적인 필드, SQL의 LIMIT/OFFSET 또는 Oracle 페이징 쿼리,
> 페이지 이동 UI는 확인된 코드가 없어 구현 세부사항을 임의로 추가하지
> 않았습니다.

------------------------------------------------------------------------

## 2. 개발 환경

  항목               내용
  ------------------ -------------------------------------------
  언어               Java
  JDK                17 (Temurin 17)
  Spring Boot        4.0.9
  빌드 도구          Maven
  데이터베이스       Oracle
  JDBC URL 예시      `jdbc:oracle:thin:@localhost:1521:testdb`
  서버 포트          8081
  ORM / SQL Mapper   MyBatis
  화면 템플릿        Thymeleaf
  보안               Spring Security
  CSS 프레임워크     Bootstrap 5
  기타               Lombok

IntelliJ IDEA의 Project SDK를 JDK 17로 설정합니다.

------------------------------------------------------------------------

## 3. 기술 스택 및 사용 목적

  기술              사용 목적
  ----------------- -----------------------------------------------
  Java 17           서버 애플리케이션 개발
  Spring Boot       애플리케이션 구성 및 실행
  Spring Security   로그인 인증, 접근 제어, 로그아웃 및 세션 관리
  MyBatis           Mapper 인터페이스와 XML을 이용한 SQL 실행
  Oracle            회원 및 게시글 데이터 저장
  Thymeleaf         서버 데이터를 HTML 화면에 출력
  Bootstrap 5       게시판 화면의 기본 레이아웃과 스타일 구성
  Lombok            Getter, Setter, 생성자 등 반복 코드 간소화

------------------------------------------------------------------------

## 4. 프로젝트 구조

기존 회원 관리 코드와 게시판 구현 내용을 기준으로 한 주요 구조입니다.
실제 프로젝트의 세부 파일명이나 추가 클래스는 프로젝트 소스에 따라
달라질 수 있습니다.

``` text
src/main/java/kopo/aisw/springboot_basic
├── SpringbootBasicApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── MainController.java
│   ├── UserController.java
│   └── BoardController.java
├── service/
│   ├── UserService.java
│   └── BoardService.java
├── mapper/
│   ├── UserMapper.java
│   └── BoardMapper.java
├── domain/
│   ├── User.java
│   ├── Board.java
│   └── Criteria.java
└── security/
    ├── CustomUserDetails.java
    └── CustomUserDetailsService.java

src/main/resources
├── application.properties
├── mapper/
│   ├── UserMapper.xml
│   └── BoardMapper.xml
└── templates/
    ├── index.html
    ├── user/
    │   ├── login.html
    │   ├── signup.html
    │   └── mypage.html
    └── board/
        ├── list.html
        ├── write.html
        ├── view.html
        └── edit.html
```

------------------------------------------------------------------------

## 5. 회원 관리 -- Spring Security

### 5-1. Spring Security 핵심 개념

  -------------------------------------------------------------------------
  개념                    의미                    프로젝트 적용
  ----------------------- ----------------------- -------------------------
  인증(Authentication)    사용자가 누구인지 확인  아이디와 비밀번호를
                                                  이용한 로그인

  인가(Authorization)     사용자가 특정 기능에    URL별 접근 권한 설정
                          접근할 수 있는지 확인   

  비밀번호 해시           비밀번호 원문 대신      `BCryptPasswordEncoder`
                          해시값 저장             
  -------------------------------------------------------------------------

BCrypt는 단방향 해시 방식입니다. 같은 비밀번호라도 salt에 따라 해시
결과가 달라질 수 있으며, 로그인 시에는 `matches()`를 이용해 입력값과
저장된 해시를 비교합니다.

### 5-2. Security 설정

Spring Security 6 이상에서 사용하는 `SecurityFilterChain` 빈 등록 방식을
적용합니다.

``` java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(...).permitAll()
        .anyRequest().authenticated()
    );

    return http.build();
}
```

주요 설정 항목은 다음과 같습니다.

  설정                        역할
  --------------------------- ----------------------------------------
  `SecurityFilterChain`       HTTP 요청에 적용할 보안 필터 체인 구성
  `authorizeHttpRequests()`   요청 URL별 접근 권한 설정
  `requestMatchers()`         접근 권한을 적용할 URL 지정
  `permitAll()`               인증 없이 접근 허용
  `authenticated()`           로그인한 사용자만 접근 허용
  `formLogin()`               폼 로그인 처리
  `logout()`                  로그아웃 처리

### 5-3. 로그인 인증 흐름

``` text
[로그인 폼 제출]
       |
       | POST /user/login-proc
       | userId, userPassword
       v
UsernamePasswordAuthenticationFilter
       |
       v
CustomUserDetailsService.loadUserByUsername()
       |
       v
UserMapper.findById()로 DB 사용자 조회
       |
       v
CustomUserDetails 반환
       |
       v
PasswordEncoder.matches()
       |
       +---- 인증 성공
       |        |
       |        v
       |   SecurityContext에 인증 정보 저장
       |        |
       |        v
       |   로그인 상태 유지 및 메인 페이지 이동
       |
       +---- 인증 실패
                |
                v
           로그인 오류 처리
```

-   `UserDetails`: Spring Security가 이해하는 사용자 정보 규격입니다.
-   `UserDetailsService`: 사용자 아이디를 이용해 사용자 정보를 조회하는
    규격입니다.
-   `CustomUserDetails`: 프로젝트의 `User` 객체를 Spring Security가
    사용할 수 있도록 연결합니다.
-   권한은 일반적으로 `ROLE_` 접두사를 사용합니다. 예를 들어 DB의 `USER`
    권한은 `ROLE_USER`로 전달합니다.

### 5-4. 회원가입 및 비밀번호 처리

회원가입 시 비밀번호를 BCrypt로 해시 처리한 뒤 DB에 저장하고, 기본
권한을 `USER`로 설정합니다.

``` java
public void registerUser(User user) {
    user.setUserPassword(
        passwordEncoder.encode(user.getUserPassword())
    );
    user.setUserAuth("USER");
    userMapper.save(user);
}
```

### 5-5. 회원 관리 URL

  HTTP Method            URL                  기능
  ---------------------- -------------------- -----------------------------
  GET                    `/user/signup`       회원가입 화면
  POST                   `/user/signup`       회원가입 처리
  GET                    `/user/login`        로그인 화면
  POST                   `/user/login-proc`   Spring Security 로그인 처리
  GET                    `/user/mypage`       마이페이지
  POST                   `/user/update`       회원 정보 수정
  POST                   `/user/delete`       회원 탈퇴
  설정된 로그아웃 요청   `/user/logout`       로그아웃

회원 정보 수정 시에는 로그인한 사용자 정보를
`Authentication.getName()`으로 확인합니다. 새 비밀번호가 비어 있다면
기존 비밀번호를 유지하도록 처리하고, 폼에서 전달된 사용자 아이디를
그대로 신뢰하지 않도록 합니다.

------------------------------------------------------------------------

## 6. 게시판 구현 -- CRUD

### 6-1. 게시판 테이블 설계

게시글에는 제목, 내용, 작성자, 조회수, 등록일, 수정일을 저장합니다.
작성자 아이디는 회원 테이블의 사용자 아이디를 참조합니다.

#### MariaDB 버전

``` sql
CREATE TABLE tb_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    view_count INT DEFAULT 0,
    reg_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    mod_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);
```

#### Oracle 버전

Oracle은 MariaDB의 `AUTO_INCREMENT` 대신 시퀀스와 트리거를 사용해 게시글
번호를 생성합니다.

``` sql
-- 시퀀스 생성
CREATE SEQUENCE board_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 게시판 테이블 생성
CREATE TABLE tb_board (
    board_id NUMBER PRIMARY KEY,
    title VARCHAR2(200) NOT NULL,
    content CLOB NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    view_count NUMBER DEFAULT 0,
    reg_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    mod_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_board_user
        FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);
```

### 6-2. 게시글 번호 자동 생성 트리거

게시글 INSERT 전에 시퀀스의 다음 값을 `board_id`에 넣습니다.

``` sql
CREATE OR REPLACE TRIGGER trg_board_insert
BEFORE INSERT ON tb_board
FOR EACH ROW
BEGIN
    SELECT board_seq.NEXTVAL
    INTO :NEW.board_id
    FROM DUAL;
END;
/
```

### 6-3. 수정일 자동 변경 트리거

게시글이 UPDATE될 때 수정일을 현재 시각으로 갱신합니다.

``` sql
CREATE OR REPLACE TRIGGER trg_board_update
BEFORE UPDATE ON tb_board
FOR EACH ROW
BEGIN
    :NEW.mod_date := SYSTIMESTAMP;
END;
/
```

### 6-4. Board 도메인 클래스

`Board`는 게시글 한 건의 데이터를 담는 도메인 객체입니다.

``` java
package kopo.aisw.springboot_basic.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Board {
    private Long boardId;
    private String title;
    private String content;
    private String userId;
    private int viewCount;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
```

### 6-5. BoardMapper 인터페이스

Mapper 인터페이스는 Service에서 호출할 데이터 접근 메서드를 정의합니다.

``` java
@Mapper
public interface BoardMapper {
    void save(Board board);
    Board findById(Long boardId);
    List<Board> findAll();
    void update(Board board);
    void delete(Long boardId);
    void updateViewCount(Long boardId);
}
```

### 6-6. BoardMapper.xml

실제 SQL은 XML Mapper에 작성합니다.

``` xml
<mapper namespace="kopo.aisw.springboot_basic.mapper.BoardMapper">

    <insert id="save" parameterType="Board">
        INSERT INTO tb_board (title, content, user_id)
        VALUES (#{title}, #{content}, #{userId})
    </insert>

    <select id="findById" parameterType="Long" resultType="Board">
        SELECT * FROM tb_board WHERE board_id = #{boardId}
    </select>

    <select id="findAll" resultType="Board">
        SELECT * FROM tb_board ORDER BY board_id DESC
    </select>

    <update id="update" parameterType="Board">
        UPDATE tb_board
        SET title = #{title}, content = #{content}
        WHERE board_id = #{boardId}
    </update>

    <delete id="delete" parameterType="Long">
        DELETE FROM tb_board WHERE board_id = #{boardId}
    </delete>

    <update id="updateViewCount" parameterType="Long">
        UPDATE tb_board
        SET view_count = view_count + 1
        WHERE board_id = #{boardId}
    </update>

</mapper>
```

> Oracle에서 게시글 번호는 INSERT 트리거가 생성하므로 INSERT SQL에서
> `board_id`를 직접 전달하지 않습니다.

### 6-7. BoardService

Service는 게시글 기능의 비즈니스 흐름을 담당하며 Mapper를 호출합니다.

``` java
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    public void saveBoard(Board board) {
        boardMapper.save(board);
    }

    public Board getBoardById(Long boardId) {
        boardMapper.updateViewCount(boardId);
        return boardMapper.findById(boardId);
    }

    public List<Board> getAllBoards() {
        return boardMapper.findAll();
    }

    public void updateBoard(Board board) {
        boardMapper.update(board);
    }

    public void deleteBoard(Long boardId) {
        boardMapper.delete(boardId);
    }
}
```

**상세 조회 처리 순서**

1.  게시글 번호를 전달받습니다.
2.  조회수 증가 SQL을 실행합니다.
3.  해당 게시글을 조회합니다.
4.  조회 결과를 Controller에 반환합니다.

### 6-8. BoardController

Controller는 브라우저 요청을 받아 Service를 호출하고, 결과에 맞는 화면을
반환합니다.

  HTTP Method   URL                         기능
  ------------- --------------------------- ------------------
  GET           `/board/list`               게시글 목록
  GET           `/board/write`              글쓰기 화면
  POST          `/board/write`              게시글 저장
  GET           `/board/view/{boardId}`     게시글 상세 조회
  GET           `/board/edit/{boardId}`     수정 화면
  POST          `/board/edit/{boardId}`     게시글 수정
  POST          `/board/delete/{boardId}`   게시글 삭제

게시글 작성 시 작성자 아이디는 폼에서 받지 않고 인증 정보에서
가져옵니다.

``` java
@PostMapping("/write")
public String write(Board board, Authentication auth) {
    board.setUserId(auth.getName());
    boardService.saveBoard(board);
    return "redirect:/board/list";
}
```

이렇게 하면 로그인한 사용자 정보와 게시글 작성자 정보가 연결됩니다.

------------------------------------------------------------------------

## 7. 게시판 화면 구성

Bootstrap 5와 Thymeleaf를 사용해 게시판 화면을 구성합니다.

  -----------------------------------------------------------------------
  화면                    템플릿                  주요 내용
  ----------------------- ----------------------- -----------------------
  게시글 목록             `board/list.html`       게시글 번호, 제목,
                                                  작성자, 작성일, 조회수

  게시글 작성             `board/write.html`      제목과 내용 입력

  게시글 상세             `board/view.html`       게시글 내용 및 작성
                                                  정보

  게시글 수정             `board/edit.html`       기존 제목과 내용을
                                                  불러와 수정
  -----------------------------------------------------------------------

### 7-1. Thymeleaf 주요 표현식

  표현식        역할                      사용 예
  ------------- ------------------------- -------------------------------
  `th:text`     서버 데이터 출력          `th:text="${board.title}"`
  `th:href`     링크 URL 생성             `th:href="@{/board/list}"`
  `th:action`   폼 전송 URL 생성          `th:action="@{/board/write}"`
  `th:each`     목록 반복 출력            `th:each="board : ${boards}"`
  `th:if`       조건에 따라 화면 출력     `th:if="${조건}"`
  `th:value`    입력 요소의 초기값 지정   `th:value="${board.title}"`

### 7-2. 로그인 여부에 따른 화면 제어

Thymeleaf Spring Security 확장 기능을 사용하면 로그인 상태에 따라 버튼을
다르게 보여줄 수 있습니다.

``` html
<a th:href="@{/board/write}"
   sec:authorize="isAuthenticated()">
    글쓰기
</a>
```

### 7-3. 게시글 작성자 확인

상세 화면에서는 로그인한 사용자와 게시글 작성자 아이디가 같은 경우에만
수정 및 삭제 버튼을 보여줍니다.

``` html
<th:block th:if="${#authentication.name == board.userId}">
    <a th:href="@{/board/edit/{id}(id=${board.boardId})}">
        수정
    </a>
</th:block>
```

> 화면에서 버튼을 숨기는 것은 UI 제어입니다. 실제 서비스에서는 수정·삭제
> 요청을 처리하는 서버 측에서도 로그인 사용자와 게시글 작성자가
> 일치하는지 반드시 검증해야 합니다.

------------------------------------------------------------------------

## 8. 페이지네이션 구현

게시글이 많아지면 모든 게시글을 한 화면에 출력하는 대신 여러 페이지로
나누어 보여줄 필요가 있습니다.

제공된 실습 내용에서는 페이지네이션 구현을 위한 첫 단계로 `Criteria`
클래스를 생성하도록 안내하고 있습니다.

``` text
domain/
└── Criteria.java
```

`Criteria`는 페이징 처리에 필요한 기준 정보를 관리하기 위한
클래스입니다.

일반적인 페이지네이션 처리 흐름은 다음과 같습니다.

``` text
사용자가 게시판 페이지 요청
          |
          v
      Controller
          |
          v
       Criteria
   (페이징 기준 전달)
          |
          v
       Service
          |
          v
        Mapper
          |
          v
    DB에서 해당 범위 조회
          |
          v
     게시글 목록 반환
          |
          v
      Thymeleaf 화면
```

현재 제공된 내용에는 `Criteria`의 실제 필드 및 Mapper SQL, 페이지 번호
계산 코드가 포함되어 있지 않으므로 해당 부분은 구현 코드 확인 후 보완할
수 있습니다.

------------------------------------------------------------------------

## 9. 데이터 처리 흐름 정리

### 9-1. 일반적인 게시글 작성 흐름

``` text
Browser
   |
   v
BoardController
   |
   v
BoardService
   |
   v
BoardMapper
   |
   v
BoardMapper.xml
   |
   v
Oracle DB
```

  계층         담당 역할
  ------------ -------------------------------------------
  Controller   사용자 요청 수신 및 응답 화면 결정
  Service      비즈니스 흐름 처리
  Mapper       DB 접근 메서드 제공
  Mapper XML   실제 SQL 작성
  Domain       DB 및 애플리케이션에서 사용할 데이터 객체

### 9-2. 회원 인증과 게시판 연동

``` text
회원가입
   |
   v
UserService
   |
   v
BCrypt 비밀번호 해시
   |
   v
UserMapper → Oracle
   |
   v
로그인
   |
   v
Spring Security 인증
   |
   v
Authentication에 사용자 정보 저장
   |
   v
게시글 작성 시 auth.getName()
   |
   v
Board의 userId에 작성자 아이디 설정
   |
   v
tb_board 저장
```

회원 관리와 게시판 기능이 별도로 동작하는 것이 아니라, Spring Security의
인증 정보를 통해 게시글 작성자와 회원 데이터가 연결됩니다.

------------------------------------------------------------------------

## 10. 실행 방법

### 10-1. 데이터베이스 준비

1.  Oracle 데이터베이스를 실행합니다.
2.  `tb_user` 테이블을 생성합니다.
3.  위의 Oracle SQL을 이용해 `tb_board` 테이블을 생성합니다.
4.  `board_seq` 시퀀스와 `trg_board_insert`, `trg_board_update` 트리거를
    생성합니다.
5.  `application.properties`의 DB 접속 정보를 확인합니다.

회원 테이블의 비밀번호 컬럼은 BCrypt 해시값을 저장할 수 있도록 충분한
길이로 설정해야 합니다. 기존 실습 기준으로 `VARCHAR2(100)` 이상을
사용합니다.

### 10-2. 애플리케이션 실행

1.  IntelliJ IDEA에서 프로젝트를 엽니다.
2.  Project SDK를 JDK 17로 설정합니다.
3.  Maven 의존성을 확인합니다.
4.  `application.properties`의 DB 연결 설정을 확인합니다.
5.  `SpringbootBasicApplication`을 실행합니다.
6.  브라우저에서 아래 주소로 접속합니다.

``` text
http://localhost:8081
```

### 10-3. 기능 테스트 순서

  -----------------------------------------------------------------------
  순서                    테스트                  확인 내용
  ----------------------- ----------------------- -----------------------
  1                       회원가입                사용자 정보가 DB에
                                                  저장되는지 확인

  2                       로그인                  DB에 저장된 계정으로
                                                  로그인되는지 확인

  3                       마이페이지              로그인한 사용자 정보가
                                                  표시되는지 확인

  4                       회원 정보 수정          수정된 정보가 DB에
                                                  반영되는지 확인

  5                       회원 탈퇴               회원 삭제 및 로그아웃이
                                                  처리되는지 확인

  6                       게시글 작성             로그인한 사용자의
                                                  아이디로 게시글이
                                                  저장되는지 확인

  7                       게시글 목록             게시글이 목록에
                                                  표시되는지 확인

  8                       게시글 상세             제목, 내용, 작성자,
                                                  조회수가 표시되는지
                                                  확인

  9                       조회수                  상세 조회 시 조회수가
                                                  증가하는지 확인

  10                      게시글 수정             제목과 내용이
                                                  수정되는지 확인

  11                      게시글 삭제             게시글이 삭제되는지
                                                  확인

  12                      작성자별 버튼 제어      작성자와 로그인
                                                  사용자가 다를 때
                                                  수정·삭제 버튼이
                                                  표시되지 않는지 확인
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 11. 트러블슈팅

  -----------------------------------------------------------------------------------------------
  증상                        원인                     해결 방법
  --------------------------- ------------------------ ------------------------------------------
  `RequiredArgsConstructor`   Lombok import 누락 또는  `import lombok.RequiredArgsConstructor;`
  오류                        Lombok 설정 문제         및 IDE 설정 확인

  Controller 컴파일 오류      중괄호 위치 또는 import  클래스 구조 및 `Authentication`, `Model`
                              누락                     import 확인

  로그인 실패                 폼 input의 `name`과      `userId`, `userPassword`로 이름 통일
                              Security 설정 불일치     

  Whitelabel 404              요청 경로 또는 Thymeleaf Controller의 반환 경로와 `templates` 구조
                              템플릿 누락              확인

  비밀번호 수정 후 로그인     빈 비밀번호를 해시       새 비밀번호가 비어 있으면 기존 비밀번호
  실패                        처리해 저장              유지

  기본 계정 로그인 실패       사용자 정의              DB에 가입된 계정으로 로그인
                              `UserDetailsService`가   
                              DB 계정을 조회           

  `sec:` 태그 오류            Thymeleaf Security 확장  `thymeleaf-extras-springsecurity6` 및 버전
                              의존성 문제              확인

  게시글 INSERT 오류          시퀀스 또는 트리거 문제  `board_seq`, `trg_board_insert` 상태 확인

  게시글 작성자 FK 오류       `tb_user`에 존재하지     회원 테이블과 게시글의 `user_id` 확인
                              않는 사용자 아이디 사용  

  수정일이 갱신되지 않음      UPDATE 트리거 문제       `trg_board_update` 상태 및 실행 결과 확인
  -----------------------------------------------------------------------------------------------

------------------------------------------------------------------------

## 12. 학습 내용 및 핵심 정리

### Spring Security

-   `SecurityFilterChain`을 이용해 URL별 접근 권한을 설정합니다.
-   `UserDetailsService`를 통해 DB에서 로그인 사용자를 조회합니다.
-   `BCryptPasswordEncoder`로 비밀번호를 안전하게 해시 처리합니다.
-   `Authentication`으로 현재 로그인한 사용자의 정보를 가져옵니다.

### MyBatis

-   Mapper 인터페이스에서 DB 접근 메서드를 정의합니다.
-   XML Mapper에서 SQL을 관리합니다.
-   `#{}`를 사용해 전달받은 값을 SQL에 바인딩합니다.
-   Domain 객체를 이용해 조회 결과와 요청 데이터를 전달합니다.

### Spring MVC

-   Controller는 요청을 처리하고 화면을 반환합니다.
-   Service는 비즈니스 흐름을 담당합니다.
-   Mapper는 DB 접근을 담당합니다.
-   Thymeleaf는 서버에서 전달된 데이터를 HTML에 출력합니다.

### Oracle

-   MariaDB의 `AUTO_INCREMENT`와 달리 Oracle에서는 시퀀스와 트리거를
    활용해 자동 번호 생성을 구현합니다.
-   외래 키를 통해 회원과 게시글의 관계를 설정합니다.
-   UPDATE 트리거를 통해 수정일을 자동 갱신합니다.

### 게시판 CRUD

-   Create: 게시글 작성
-   Read: 게시글 목록 및 상세 조회
-   Update: 게시글 수정
-   Delete: 게시글 삭제

------------------------------------------------------------------------

## 13. 프로젝트를 통해 이해한 핵심 구조

``` text
Spring Boot
    |
    +-- Spring Security
    |       |
    |       +-- 로그인 인증
    |       +-- 권한 및 접근 제어
    |       +-- 세션 관리
    |
    +-- Controller
    |       |
    |       +-- UserController
    |       +-- BoardController
    |
    +-- Service
    |       |
    |       +-- UserService
    |       +-- BoardService
    |
    +-- Mapper
    |       |
    |       +-- UserMapper
    |       +-- BoardMapper
    |
    +-- Domain
    |       |
    |       +-- User
    |       +-- Board
    |       +-- Criteria
    |
    +-- Thymeleaf
            |
            +-- 회원 화면
            +-- 게시판 화면
```

> **핵심:** Spring Security로 사용자를 인증하고, Controller와 Service를
> 통해 비즈니스 로직을 처리하며, MyBatis Mapper와 Oracle을 이용해
> 데이터를 관리하는 Spring Boot 웹 애플리케이션의 기본 구조를
> 학습했습니다.
