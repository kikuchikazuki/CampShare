# CampShare Login and Security Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan in two verification blocks. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Authenticate existing users by email and BCrypt password, and provide secure login/logout flows.

**Architecture:** `DatabaseUserDetailsService` loads `User` and `Role` through JPA. `SecurityConfig` uses it in Spring Security form login. Thymeleaf renders the login page and CSRF-protected logout form.

**Tech Stack:** Java 17, Spring Boot 3.5.16, Spring Security, Spring Data JPA, Thymeleaf, JUnit 5, MockMvc.

## Global Constraints

- Keep Flyway migrations and `spring.jpa.hibernate.ddl-auto=none` unchanged.
- Preserve BCrypt password storage; never create a plain-password field.
- Permit `/`, `/signup`, and `/login`; require authentication for all other paths.
- Login uses request parameter `email`; logout is a CSRF-protected `POST /logout`.
- Use Maven Wrapper with `$env:MAVEN_USER_HOME = "$PWD\.maven-cache"`.

---

## Task 5.1: Authentication Foundation

**Files:**

- Modify: `campshare/pom.xml`
- Modify: `campshare/src/main/java/com/example/campshare/user/User.java`
- Modify: `campshare/src/main/java/com/example/campshare/user/UserRepository.java`
- Create: `campshare/src/main/java/com/example/campshare/user/DatabaseUserDetailsService.java`
- Create: `campshare/src/main/java/com/example/campshare/config/SecurityConfig.java`
- Create: `campshare/src/test/java/com/example/campshare/user/DatabaseUserDetailsServiceTest.java`

- [ ] **Step 1: Write the failing user-details tests**

The test must assert that `loadUserByUsername("taro@example.com")` returns a `UserDetails` whose username is the email, password is the stored hash, and authority is `ROLE_USER`. A second test must assert that a missing user throws `UsernameNotFoundException`.

- [ ] **Step 2: Confirm the red state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=DatabaseUserDetailsServiceTest
```

Expected: `BUILD FAILURE` because the service and `findByEmail` API do not exist.

- [ ] **Step 3: Implement the authentication foundation**

1. Replace direct `spring-security-crypto` with `spring-boot-starter-security` in `pom.xml`.
2. Add `getEmail()` to `User`.
3. Add `Optional<User> findByEmail(String email)` to `UserRepository`.
4. Implement `DatabaseUserDetailsService` using `UserDetailsService`; map missing email to `UsernameNotFoundException` and map `user.getRole().getName()` as the authority.
5. Create `SecurityConfig` with a `SecurityFilterChain`: permit `/`, `/signup`, `/login`; configure custom login page, email parameter, `defaultSuccessUrl("/", true)`, and logout success URL `/`.

- [ ] **Step 4: Confirm the green state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=DatabaseUserDetailsServiceTest
```

Expected: `BUILD SUCCESS`.

## Task 5.2: Login and Logout UI

**Files:**

- Create: `campshare/src/main/java/com/example/campshare/web/LoginController.java`
- Create: `campshare/src/main/resources/templates/login.html`
- Modify: `campshare/src/main/resources/templates/home.html`
- Create: `campshare/src/test/java/com/example/campshare/web/LoginControllerTest.java`

- [ ] **Step 1: Write failing web tests**

The tests must require `GET /login` to return the `login` view, reject unauthenticated access to a non-public path by redirecting to `/login`, and render the login form’s error state for `/login?error`.

- [ ] **Step 2: Confirm the red state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=LoginControllerTest
```

Expected: `BUILD FAILURE` because `LoginController` and `login.html` do not exist.

- [ ] **Step 3: Implement views**

1. Add `LoginController` with `GET /login` returning `login`.
2. Add `login.html` with an email input named `email`, password input named `password`, `POST /login`, error display when `param.error` exists, and a link to `/signup`.
3. Update `home.html` to show login/signup links to guests and the logged-in user’s email plus a `POST /logout` form to authenticated users. The logout form must use `th:action="@{/logout}"` so the CSRF token is emitted.

- [ ] **Step 4: Confirm web tests and the full suite are green**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test
```

Expected: `BUILD SUCCESS`, with zero failures and zero errors.

- [ ] **Step 5: Browser verification**

Start the application, log in using the Task 4 account, verify return to `/` and the logout control, submit logout, and verify return to `/` as a guest.
