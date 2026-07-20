# CampShare Login and Security Design

## Goal

Enable registered users to log in with email and password, keep an authenticated session, and log out safely.

## Scope

- Add Spring Security form login backed by the existing PostgreSQL `users` table.
- Use email as the login identifier and the existing BCrypt `password_hash` for password verification.
- Provide a custom `GET /login` page and let Spring Security process `POST /login`.
- Redirect successful login and successful logout to `/`.
- Show login/signup actions to guests and email/logout actions to authenticated users on the home page.

## Security Configuration

- Add `spring-boot-starter-security` and remove the direct `spring-security-crypto` dependency because the starter provides it.
- Configure a `SecurityFilterChain` that permits `/`, `/signup`, and `/login`; all other paths require authentication.
- Configure `formLogin` with `loginPage("/login")`, `usernameParameter("email")`, and `defaultSuccessUrl("/", true)`.
- Keep CSRF protection enabled. Logout is a Thymeleaf `POST /logout` form and redirects to `/`.
- Do not implement remember-me, password reset, OAuth, or public administrator creation.

## User Loading

- Add `UserRepository.findByEmail(String email)`.
- Add `DatabaseUserDetailsService` implementing `UserDetailsService`.
- It loads a `User` by email, exposes the stored BCrypt hash, and maps the linked role name (`ROLE_USER` or `ROLE_ADMIN`) as an authority.
- If no user exists, it throws `UsernameNotFoundException` without disclosing whether an email is registered to the browser.

## Views

- `login.html` contains an email field named `email`, a password field named `password`, a CSRF-protected form POSTing to `/login`, a login-error message, and a link to `/signup`.
- `home.html` shows guest actions when anonymous and the authenticated email plus a POST logout form when logged in.

## Verification

1. Unit-test database user-to-`UserDetails` conversion and unknown-user handling.
2. MVC-test the login page and protected-path redirect behavior.
3. Run the full Maven suite.
4. Use the browser to log in with the Task 4 account, observe the home page, log out, and confirm return to the home page.
