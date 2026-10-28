# PROMPTS.md — AI Prompt Log

Record every prompt you send to the AI **starting from Slice 2**.  
Slice 1 is done manually — no AI. See `README.md` for the prompt template structure.

> **Rules:**
> - One prompt (+ follow-ups) per slice
> - Use the structured template from the README — free-form prompts are not allowed
> - Do not back-fill this file after the lab

---

## Example — How a good S1 prompt looks (for reference only, S1 is done manually)

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
  com.esd.lab2
  ├── config/       SecurityConfig.java, AdminSeeder.java
  ├── controller/   CourseController.java
  ├── service/      CourseService.java
  ├── dto/          CourseRequest.java, CourseResponse.java
  ├── entity/       User.java, Role.java (enum), Course.java
  ├── repository/   UserRepository.java, CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java,
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java


TASK
Implement only POST /api/auth/signup.
Request: SignupRequest { email (valid email, required), password (required, 8–64 chars),
         fullName (required, not blank, max 100) }.
Response: 201 with UserResponse { id, email, fullName, role, createdAt }.
The new user's role is always STUDENT, even if the request contains a role field.
Store email trimmed and lowercased. Hash the password with the existing PasswordEncoder bean.
If the email already exists, throw DuplicateResourceException.

ALLOWED TO CREATE OR CHANGE
dto/SignupRequest.java, dto/UserResponse.java,
service/AuthService.java (signup method only),
controller/AuthController.java (signup endpoint only)

RULES
- Do not create or modify any file not listed above.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

---

## Slice 2 — Login & JWT (S2)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
  com.esd.lab2
  ├── Lab2Application.java
  ├── config/       SecurityConfig.java (TEMPORARY: permits all), AdminSeeder.java
  ├── controller/   CourseController.java, AuthController.java
  ├── service/      CourseService.java, AuthService.java
  ├── dto/          CourseRequest.java, CourseResponse.java,
  │                 SignupRequest.java, UserResponse.java
  ├── entity/       User.java, Role.java (enum), Course.java
  ├── repository/   UserRepository.java (findByEmail, existsByEmail), CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java (401),
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java


TASK
Implement only: A2 POST /api/auth/login (login and JWT issuing).
Contract:
- POST /api/auth/login, public, body LoginRequest { email: required, valid email;
  password: required }.
- Success: 200 with LoginResponse { accessToken, tokenType: "Bearer", expiresIn: <seconds> }.
- Errors: 400 for invalid body, 401 for bad credentials.
- Look up the user by the trimmed, lowercased email. Verify the password with the
  injected PasswordEncoder.matches(). If the email is unknown OR the password is wrong,
  throw InvalidCredentialsException with the identical message "Invalid email or password".
- JWT claims: sub = email, role claim, jti = random UUID (not the user id), iat, exp.
  Sign with HS256 using a key built from the Base64-encoded jwt.secret read through
  @Value. No string literal secret. expiresIn must be computed from the configured
  expiry property, not hardcoded.
- Do NOT write any token parsing or validation method. That is a later slice.

ALLOWED TO CREATE OR CHANGE
dto/LoginRequest.java (create), dto/LoginResponse.java (create),
security/JwtService.java (create, generateToken(User user) only),
service/AuthService.java (add login method; constructor may change only to inject JwtService),
controller/AuthController.java (add login endpoint only)

RULES
- Do not create or modify any file not listed above. Do not touch SecurityConfig.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods
  (no setSubject, setExpiration, signWith(SignatureAlgorithm, ...), Jwts.parserBuilder()).
- Put no password or hash in the token claims.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
Make me go through these files one by one.
```

---

## Slice 3 — JWT Filter & Protected API (S3)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
  com.esd.lab2
  ├── Lab2Application.java
  ├── config/       SecurityConfig.java (TEMPORARY: permits all), AdminSeeder.java
  ├── controller/   CourseController.java, AuthController.java, UserController.java
  ├── service/      CourseService.java, AuthService.java
  ├── dto/          CourseRequest.java, CourseResponse.java, SignupRequest.java,
  │                 UserResponse.java, LoginRequest.java, LoginResponse.java
  ├── entity/       User.java, Role.java (enum: STUDENT, ADMIN), Course.java
  ├── repository/   UserRepository.java (findByEmail returns Optional<User>, existsByEmail),
  │                 CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java,
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java,
                    JwtService.java (generateToken only so far)

TASK
Implement only: S3 JWT filter and protected API (contract row P2).
Contract:
- GET /api/users/me: any logged-in user, returns 200 UserResponse
  { id, email, fullName, role, createdAt }; 401 if there is no valid token.
- Public endpoints, and only these three: POST /api/auth/signup,
  POST /api/auth/login, GET /api/courses. Everything else requires authentication.
  POST /api/courses must NOT be public.
- 401 and 403 responses from the filter chain use the existing ApiError JSON via the
  existing JsonAuthenticationEntryPoint and JsonAccessDeniedHandler.
- Add JwtService.parseToken(String token) returning io.jsonwebtoken.Claims, using
  Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(). It must
  verify the signature and the expiry.
- Create JwtAuthenticationFilter extending OncePerRequestFilter. It reads the
  "Authorization: Bearer <token>" header, parses the claims, and sets a
  UsernamePasswordAuthenticationToken in SecurityContextHolder with principal = the sub
  claim (email) and authority "ROLE_" + the role claim. On any invalid, expired or
  malformed token it clears the SecurityContext and continues the chain. It must never
  write a response itself.
- SecurityConfig: SecurityFilterChain bean with the lambda DSL, CSRF disabled, STATELESS
  sessions, authorizeHttpRequests with requestMatchers, exceptionHandling wiring the two
  existing handlers, and the filter added before UsernamePasswordAuthenticationFilter.
  Keep the existing PasswordEncoder bean unchanged.
- UserController: GET /api/users/me loads the user from the database by
  authentication.getName() and returns a UserResponse, never the entity.

ALLOWED TO CREATE OR CHANGE
security/JwtService.java (add parseToken only),
security/JwtAuthenticationFilter.java (create),
config/SecurityConfig.java (filterChain bean only; keep the PasswordEncoder bean),
controller/UserController.java (create or fill in GET /api/users/me only)

RULES
- Do not create or modify any file not listed above.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods
  (no WebSecurityConfigurerAdapter, antMatchers, authorizeRequests, .and(),
  Jwts.parserBuilder(), or Base64-decode-and-trust parsing).
- Do not add UserDetailsService, AuthenticationManager, CORS configuration or sessions.
- Do not add the ADMIN rule for POST /api/courses. That is the next slice.
- Register the JWT filter in exactly one place so it does not run twice.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 4 — Role Restriction (S4)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout: <same tree as S3, now including JwtAuthenticationFilter.java,
UserController.java, LoginRequest/LoginResponse>

TASK
Implement only: P3, POST /api/courses restricted to ADMIN.
Contract: POST /api/courses, ADMIN only, body { code, title, description }.
Success 201; errors 400, 401 (no token), 403 (authenticated non-admin), 409.
GET /api/courses must stay public.

ALLOWED TO CREATE OR CHANGE
config/SecurityConfig.java (add one requestMatchers rule only)

RULES
- Do not create or modify any file not listed above.
- Add exactly: .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN"),
  placed immediately before .anyRequest().authenticated().
- Do not rewrite or reformat the rest of the config.
- Use only Spring Security 6 APIs. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
The changed file in full, with its path, then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 5 — Logout *(stretch, S5)*

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout: <same tree as S4, plus nothing new>

TASK
Implement only: A3 POST /api/auth/logout (token denylist).
Contract: logged-in only, no body, 204 on success, 401 if not logged in.
After logout the same token gets 401 on every protected endpoint.
- Create TokenDenylist: @Component, ConcurrentHashMap<String, Instant>,
  revoke(jti, expiry), isRevoked(jti), cleanExpired().
- Logout parses the Bearer token, reads jti and expiration, calls revoke, returns 204.
- The filter checks isRevoked(jti) after parsing; a revoked token is handled like an
  expired one: clear the SecurityContext and continue the chain, never write a response.

ALLOWED TO CREATE OR CHANGE
security/TokenDenylist.java (create), security/JwtAuthenticationFilter.java (inject
denylist, add check), controller/AuthController.java (add logout only),
config/SecurityConfig.java (only to pass the denylist into the filter constructor)

RULES
- Do not create or modify any file not listed above.
- Key the denylist by jti, not email. Use ConcurrentHashMap.
- Logout must NOT be under permitAll.
- Use only Spring Security 6 and jjwt 0.12 APIs. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each changed file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```
