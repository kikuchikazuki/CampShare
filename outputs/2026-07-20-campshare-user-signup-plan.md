# CampShare User Signup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Public signup creates one `ROLE_USER` account in PostgreSQL without storing a raw password.

**Architecture:** Flyway V2 owns the `users` table. JPA maps `User` and `Role`; `SignupService` owns the transaction, duplicate-email rejection, default-role assignment, and BCrypt hashing. `SignupController` validates the request and renders the Thymeleaf form.

**Tech Stack:** Java 17, Spring Boot 3.5.16, Spring Data JPA, Flyway, PostgreSQL 16, Thymeleaf, Bean Validation, Spring Security Crypto, JUnit 5, AssertJ, MockMvc.

## Global Constraints

- Execute Maven only through `campshare\\mvnw.cmd` with `MAVEN_USER_HOME` set to `"$PWD\\.maven-cache"`.
- Keep database credentials only in the ignored project-root `application-local.properties`.
- Preserve `spring.jpa.hibernate.ddl-auto=none`; Flyway is the only schema writer.
- Add `V2__create_users.sql`; never edit the already-applied V1 migration.
- Public signup must always assign `ROLE_USER`, without accepting a role field from the request.
- Only BCrypt hashes may be saved in `password_hash`; never log or render the raw password.
- Login, logout, authorization, and public admin creation remain outside this task.
- Stop after every step that requests a user verification result.

---

## File Structure

- Modify: `campshare/pom.xml` — validation and BCrypt dependencies.
- Create: `campshare/src/main/resources/db/migration/V2__create_users.sql` — users schema.
- Create: `campshare/src/main/java/com/example/campshare/config/PasswordConfig.java` — password encoder bean.
- Create: `campshare/src/main/java/com/example/campshare/user/Role.java` — role entity.
- Create: `campshare/src/main/java/com/example/campshare/user/User.java` — user entity.
- Create: `campshare/src/main/java/com/example/campshare/user/RoleRepository.java` — role lookup.
- Create: `campshare/src/main/java/com/example/campshare/user/UserRepository.java` — email lookup and persistence.
- Create: `campshare/src/main/java/com/example/campshare/user/SignupForm.java` — validated form data.
- Create: `campshare/src/main/java/com/example/campshare/user/DuplicateEmailException.java` — duplicate-email error.
- Create: `campshare/src/main/java/com/example/campshare/user/SignupService.java` — signup use case.
- Create: `campshare/src/main/java/com/example/campshare/web/SignupController.java` — `/signup` endpoints.
- Create: `campshare/src/main/resources/templates/signup.html` — registration form.
- Create: `campshare/src/test/java/com/example/campshare/config/UserMigrationTest.java` — V2 verification.
- Create: `campshare/src/test/java/com/example/campshare/user/SignupServiceTest.java` — service verification.
- Create: `campshare/src/test/java/com/example/campshare/web/SignupControllerTest.java` — MVC verification.

## Task 4.1: Add and Verify the Users Schema

**Produces:** `users(id, display_name, email, password_hash, role_id, created_at)` with a unique email and a foreign key to `roles(id)`.

- [ ] **Step 1: Add the failing migration test**

Create `campshare/src/test/java/com/example/campshare/config/UserMigrationTest.java`:

```java
package com.example.campshare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "file:./application-local.properties")
class UserMigrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void usersTableHasColumnsAndRoleForeignKey() {
        Integer columns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'users'
                  AND column_name IN ('id', 'display_name', 'email', 'password_hash', 'role_id', 'created_at')
                """, Integer.class);
        Integer foreignKeys = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.table_constraints
                WHERE table_schema = 'public' AND table_name = 'users'
                  AND constraint_type = 'FOREIGN KEY'
                """, Integer.class);

        assertThat(columns).isEqualTo(6);
        assertThat(foreignKeys).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Confirm the expected red state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=UserMigrationTest
```

Expected: `BUILD FAILURE` and an assertion such as `expected: 6 but was: 0`.

- [ ] **Step 3: Add the immutable V2 migration**

Create `campshare/src/main/resources/db/migration/V2__create_users.sql`:

```sql
CREATE TABLE users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **Step 4: Confirm the migration is green**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=UserMigrationTest
```

Expected: `BUILD SUCCESS`, with Flyway applying V2 once.

- [ ] **Step 5: Check the actual database in pgAdmin**

Run this in the `campshare` database Query Tool:

```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'users'
ORDER BY ordinal_position;

SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Expected: six user columns, plus a successful V2 history row named `create users`.

## Task 4.2: Add JPA Mapping and Signup Contract

**Produces:** `RoleRepository.findByName(String)`, `UserRepository.existsByEmail(String)`, and the service contract `void register(SignupForm form)`.

- [ ] **Step 1: Add failing service tests**

Create `campshare/src/test/java/com/example/campshare/user/SignupServiceTest.java`:

```java
package com.example.campshare.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private SignupService signupService;

    @Test
    void registerSavesRoleUserWithEncodedPassword() {
        SignupForm form = new SignupForm("Camper Taro", "taro@example.com", "password123");
        Role roleUser = new Role(1L, "ROLE_USER");
        when(userRepository.existsByEmail("taro@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        signupService.register(form);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getValue().getRole()).isSameAs(roleUser);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        SignupForm form = new SignupForm("Camper Taro", "taro@example.com", "password123");
        when(userRepository.existsByEmail("taro@example.com")).thenReturn(true);

        assertThatThrownBy(() -> signupService.register(form))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
```

- [ ] **Step 2: Confirm the expected red state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=SignupServiceTest
```

Expected: `BUILD FAILURE` because the user package classes do not exist yet.

- [ ] **Step 3: Add dependencies and JPA files**

Add the following two dependencies under `<dependencies>` in `campshare/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

Create `campshare/src/main/java/com/example/campshare/user/Role.java`:

```java
package com.example.campshare.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    protected Role() { }

    Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```

Create `campshare/src/main/java/com/example/campshare/user/User.java`:

```java
package com.example.campshare.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected User() { }

    public User(String displayName, String email, String passwordHash, Role role) {
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
```

Create `RoleRepository.java` and `UserRepository.java`:

```java
package com.example.campshare.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
```

```java
package com.example.campshare.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
```

Create `campshare/src/main/java/com/example/campshare/config/PasswordConfig.java`:

```java
package com.example.campshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 4: Confirm the remaining red state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=SignupServiceTest
```

Expected: `BUILD FAILURE` because `SignupForm`, `SignupService`, and `DuplicateEmailException` are still absent.

## Task 4.3: Implement Transactional Signup

**Produces:** `SignupService.register(SignupForm)` saves a new BCrypt-hashed `ROLE_USER` account or throws `DuplicateEmailException`.

- [ ] **Step 1: Add the form, error, and service**

Create `campshare/src/main/java/com/example/campshare/user/SignupForm.java`:

```java
package com.example.campshare.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupForm {
    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must be at most 100 characters")
    private String displayName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
    private String password;

    public SignupForm() { }

    public SignupForm(String displayName, String email, String password) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }

    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
```

Create `campshare/src/main/java/com/example/campshare/user/DuplicateEmailException.java`:

```java
package com.example.campshare.user;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("Email is already registered");
    }
}
```

Create `campshare/src/main/java/com/example/campshare/user/SignupService.java`:

```java
package com.example.campshare.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(SignupForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new DuplicateEmailException();
        }
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER is not configured"));
        String passwordHash = passwordEncoder.encode(form.getPassword());
        userRepository.save(new User(form.getDisplayName(), form.getEmail(), passwordHash, roleUser));
    }
}
```

- [ ] **Step 2: Confirm the service tests are green**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=SignupServiceTest
```

Expected: `BUILD SUCCESS` with two passing tests.

## Task 4.4: Add and Verify the Signup Page

**Produces:** `GET /signup`, `POST /signup`, form validation feedback, duplicate-email feedback, and a success redirect.

- [ ] **Step 1: Add failing MVC tests**

Create `campshare/src/test/java/com/example/campshare/web/SignupControllerTest.java`:

```java
package com.example.campshare.web;

import com.example.campshare.user.DuplicateEmailException;
import com.example.campshare.user.SignupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(SignupController.class)
class SignupControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private SignupService signupService;

    @Test
    void signupPageReturnsSignupView() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("signupForm"));
    }

    @Test
    void signupRejectsBlankFields() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("displayName", "")
                        .param("email", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeHasFieldErrors("signupForm", "displayName", "email", "password"));
    }

    @Test
    void signupRedirectsAfterSuccess() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("displayName", "Camper Taro")
                        .param("email", "taro@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup?registered=true"));
    }

    @Test
    void signupShowsEmailErrorWhenEmailIsDuplicate() throws Exception {
        doThrow(new DuplicateEmailException()).when(signupService).register(any());

        mockMvc.perform(post("/signup")
                        .param("displayName", "Camper Taro")
                        .param("email", "taro@example.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeHasFieldErrors("signupForm", "email"));
    }
}
```

- [ ] **Step 2: Confirm the expected red state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=SignupControllerTest
```

Expected: `BUILD FAILURE` because `SignupController` does not exist.

- [ ] **Step 3: Add the controller and Thymeleaf template**

Create `campshare/src/main/java/com/example/campshare/web/SignupController.java`:

```java
package com.example.campshare.web;

import com.example.campshare.user.DuplicateEmailException;
import com.example.campshare.user.SignupForm;
import com.example.campshare.user.SignupService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignupController {
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/signup")
    public String signupPage(@RequestParam(defaultValue = "false") boolean registered, Model model) {
        model.addAttribute("signupForm", new SignupForm());
        model.addAttribute("registered", registered);
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") SignupForm signupForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            signupService.register(signupForm);
        } catch (DuplicateEmailException exception) {
            bindingResult.rejectValue("email", "duplicate", exception.getMessage());
            return "signup";
        }
        return "redirect:/signup?registered=true";
    }
}
```

Create `campshare/src/main/resources/templates/signup.html`:

```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>新規登録 | CampShare</title>
</head>
<body>
<main>
    <h1>CampShare 新規登録</h1>
    <p th:if="${registered}">登録が完了しました。ログイン機能は次のステップで追加します。</p>
    <form th:action="@{/signup}" th:object="${signupForm}" method="post">
        <div>
            <label for="displayName">表示名</label>
            <input id="displayName" type="text" th:field="*{displayName}">
            <p th:errors="*{displayName}"></p>
        </div>
        <div>
            <label for="email">メールアドレス</label>
            <input id="email" type="email" th:field="*{email}">
            <p th:errors="*{email}"></p>
        </div>
        <div>
            <label for="password">パスワード</label>
            <input id="password" type="password" th:field="*{password}">
            <p th:errors="*{password}"></p>
        </div>
        <button type="submit">登録する</button>
    </form>
</main>
</body>
</html>
```

- [ ] **Step 4: Confirm the MVC tests are green**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=SignupControllerTest
```

Expected: `BUILD SUCCESS` with four passing tests.

- [ ] **Step 5: Run complete verification and inspect the browser flow**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080/signup`, register one unused email address, and verify a success message after redirect. In pgAdmin, verify the corresponding `users` row references `ROLE_USER` and that `password_hash` begins with `$2a$`, `$2b$`, or `$2y$`. Stop the application with `Ctrl + C`.

## User Verification Gates

1. Migration test red before V2.
2. Migration test green and V2 inspected in pgAdmin.
3. Service test red before the user-layer code.
4. Service test green after BCrypt registration logic.
5. MVC test red before the controller.
6. MVC test green after the controller and template.
7. Full suite green and manual signup verified.
