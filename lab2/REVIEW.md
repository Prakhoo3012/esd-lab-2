# REVIEW.md — AI Output Review Log

Complete **one review entry per slice** before moving to the next.  


---

## Slice 1 — Signup (S1)

### A. Bucket Check
List every class and method the AI produced. Tag each one:  
`Controller` / `Service` / `Repository` / `DTO` / `Security` / `Config` / `Exception`

| File / Method | Tag | In Allowed List? |
|---|---|---|
| | | |

> **Important:** Anything **not** in the allowed list → **reject, re-prompt**. Don't keep it because it works.

### B. Contract Check
Compare against API contract row A1 line by line.

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/signup` | |
| Returns **201** | |
| Response shape: `{ id, email, fullName, role, createdAt }` | |
| No `passwordHash` field in response | |
| Role in response is always `STUDENT` | |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| `@Valid` on controller parameter | |
| `@Email`, `@NotBlank`, `@Size` on DTO fields | |
| Duplicate email throws `DuplicateResourceException` | |
| No new error/response wrapper class invented | |
| No stack trace or exception message leaked to client | |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Password hashed via injected `PasswordEncoder` (not `new BCryptPasswordEncoder()`) | |
| `SignupRequest` has no `role` field, or it is ignored | |
| Duplicate check uses normalised (lowercased/trimmed) email | |
| AI did **not** touch `SecurityConfig`, add JWT class, or add `UserDetailsService` | |

### E. Understanding Questions
*Answer in your own words (1–2 lines each)*

1. Where exactly is the password hashed, and why is the same password stored as a different string for two users?

   > Password is hashed in AuthService.java file. Same Password stored as a different string because we are using passwordEncoder function to hashed the password before it is stored.

2. What would happen if `SignupRequest` accepted a `role` field and the AI used it?

   > When SignupRequest accept role field then AI will immediately know the type of user registered to the app and accordingly it will provide that user the function he can use on the platform

3. Which class turns `DuplicateResourceException` into a 409 response?

   > GlobalExceptionHandler class handles it.

### F. Decision

| File | Decision | Reason (if modified/rejected) |
|---|---|---|
| `SignupRequest.java` | Accepted | Written manually. Removed a compact constructor that trimmed the email, because normalisation belongs in the service. |
| `UserResponse.java` | Accepted | Already matched the contract, no hash field. |
| `AuthService.java` | Accepted | Fixed a missing semicolon and a variable name, returned the saved entity via `toResponse`, aligned the message with the contract. |
| `AuthController.java` | Accepted | Returns 201 using `ResponseEntity`. |

---

## Slice 2 — Login & JWT (S2)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `LoginRequest` | DTO | Yes |
| `LoginResponse` | DTO | Yes |
| `JwtService.generateToken(User)` | Security | Yes |
| `JwtService.getExpirationSeconds()` | Security | **No** (small extra getter, see Decision) |
| `AuthService.login()` + `JwtService` constructor parameter | Service | Yes |
| `AuthController.login()` | Controller | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/login` | |
| Returns **200** | |
| Response: `{ accessToken, tokenType: "Bearer", expiresIn }` | |
| `expiresIn` matches configured value (not hardcoded) | |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Same 401 message for unknown email and wrong password | |
| No `try/catch` swallowing exceptions silently | |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| JWT secret comes from `@Value`, no string literal secret | |
| Uses jjwt 0.12 API: `.subject()`, `.claim()`, `.id()`, `.issuedAt()`, `.expiration()`, `.signWith(key)` | |
| Rejects deprecated: `setSubject`, `setExpiration`, `signWith(SignatureAlgorithm.HS256, secret)`, `Jwts.parserBuilder()` | |
| `jti` is a random UUID (not the user id) | |
| No password or hash in token claims | |
| Parse/validate method **not** written yet (that is S3) | |

### E. Understanding Questions

1. Why must the error message be identical for "no such email" and "wrong password"?

   > So that we donot allow any other third party or user to know exactly where the bug is or exactly what is wrong otherwise the another will try to guess it.

2. Who can read the claims inside your JWT? Who can change them?

   > Anyone who holds the token can read the claims. Nobody without the sceret key can change them.

3. What does `jti` give us that `sub` doesn't? (Think about S5.)

   > sub is the same for every token a user ever gets, so it can't identify one particular token. jti is unique per token, so logout can revoke just that token while the same user's other sessions and future logins keep working.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `LoginRequest.java`, `LoginResponse.java` | Accepted | Match the contract. |
| `JwtService.java` | Modified | Added `getExpirationSeconds()` (not in the allowed list) so `expiresIn` is not hardcoded in the service. Kept because it's a one-line getter and needed by the contract. |
| `AuthService.java` | Accepted | `login()` added, `signup()` unchanged, same 401 message. |
| `AuthController.java` | Accepted | Login returns 200, only that endpoint added. |


---

## Slice 3 — JWT Filter & Protected API (S3)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `JwtService.parseToken()` | Security | Yes |
| `JwtAuthenticationFilter` | Security | Yes |
| `SecurityConfig.filterChain()` | Config | Yes (kept `PasswordEncoder` bean) |
| `UserController.me()` | Controller | Yes |


### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `GET /api/courses` works **without** token | |
| `GET /api/users/me` returns **401** with no token | |
| `GET /api/users/me` returns **200** with valid token | |
| 401 body is an `ApiError` JSON object | |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Filter does not write a response on error — continues chain | |
| Filter clears SecurityContext on bad token | |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Spring Security 6 style: lambda DSL, `requestMatchers`, `authorizeHttpRequests` | |
| Rejected: `WebSecurityConfigurerAdapter`, `antMatchers`, `authorizeRequests`, `.and()` | |
| `permitAll` covers only the three specific public endpoints | |
| `GET /api/courses` is public but `POST /api/courses` is NOT (watch for broad matcher) | |
| Signature + expiry verified with `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` | |
| Authorities use `ROLE_` prefix | |
| Entry point and access-denied handler wired via `exceptionHandling(...)` | |
| No `UserDetailsService`, `AuthenticationManager`, sessions or CORS added | |
| `GET /api/users/me` returns `UserResponse`, not the entity | |

### E. Understanding Questions

1. Why is CSRF protection disabled here, and when would disabling it be wrong?

   > CSRF attacks rely on the browser automatically attaching credentials (session cookies) to a forged request.Disabling CSRF would be wrong if authentication were cookie-based (session cookie, or a JWT stored in a cookie), because the browser would then attach it automatically.


2. Walk through what happens to a request with an expired token, step by step, until the client sees 401.

   >  1. The request arrives with `Authorization: Bearer <token>`. 2. `JwtAuthenticationFilter` extracts the token and calls `parseToken`. 3. jjwt checks the signature, then the expiry, and throws `ExpiredJwtException` (a `JwtException`). 4. The filter catches it, clears the `SecurityContext` and continues the chain without writing a response. 5. The authorization rule `anyRequest().authenticated()` finds no authentication. 6. `ExceptionTranslationFilter` calls `JsonAuthenticationEntryPoint`. 7. The entry point writes the 401 `ApiError` JSON.


3. Why don't 401/403 errors from the filter chain reach `GlobalExceptionHandler`?

   > `GlobalExceptionHandler` is a controller advice, so it only handles exceptions thrown while Spring MVC is running a controller. The security filters run earlier in the servlet filter chain, before the `DispatcherServlet` and any controller, so these errors never get there. That is why separate handlers (`JsonAuthenticationEntryPoint`, `JsonAccessDeniedHandler`) write the JSON.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `JwtService.java` | Accepted | Uses `Jwts.parser().verifyWith(key)...parseSignedClaims`. |
| `JwtAuthenticationFilter.java` | Accepted | No `@Component`, so it is registered once and does not run twice. |
| `SecurityConfig.java` | Accepted | Kept `PasswordEncoder` bean, only three public endpoints. |
| `UserController.java` | Accepted | Returns `UserResponse`. |


---

## Slice 4 — Role Restriction (S4)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `SecurityConfig` (one `requestMatchers(...).hasRole("ADMIN")` line) | Config | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/courses` with no token → **401** | |
| `POST /api/courses` with STUDENT token → **403** | |
| `POST /api/courses` with ADMIN token → **201** | |
| 403 body is an `ApiError` JSON object | |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Role naming consistent: `hasRole("ADMIN")` ↔ authority `ROLE_ADMIN` | |
| If `@PreAuthorize` used: `@EnableMethodSecurity` is present | |
| Rule placed **before** `anyRequest().authenticated()` if in SecurityConfig | |
| Diff is small — AI did not rewrite the whole config | |

### E. Understanding Questions

1. A STUDENT calls `POST /api/courses`. Why does the server return 403 and not 401?

   > 401 means the server doesn't know who you are (missing or invalid credentials). The STUDENT's token is valid, so they are authenticated, but their role isn't allowed to create courses. That is an authorisation failure, which is 403.

2. Where does the role in the JWT come from, and can the client change it?

   > It comes from the `User` row in the database, copied into the `role` claim by `JwtService.generateToken` at login. The client can read it but can't change it, because editing the payload invalidates the signature and `parseToken` rejects the token.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `SecurityConfig.java` | Accepted | Only the one rule was added, nothing else rewritten. |


---

## Slice 5 — Logout (S5) *(stretch)*

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `TokenDenylist` (`revoke`, `isRevoked`, `cleanExpired`) | Security | Yes |
| `JwtAuthenticationFilter` (inject denylist, check `isRevoked`) | Security | Yes |
| `AuthController.logout()` | Controller | Yes |
| `SecurityConfig` (pass denylist to the filter constructor) | Config | Yes (needed because the filter is created with `new`) |


### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/auth/logout` returns **204** | |
| Same token returns **401** after logout | |
| Fresh login after logout returns a new working token | |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Logout adds entry to server-side denylist (not just tells client to delete token) | |
| Denylist keyed by **jti**, not email | |
| Map is `ConcurrentHashMap` (thread-safe) | |
| Logout endpoint is NOT under `permitAll` | |
| Filter checks the denylist and treats denylisted jti as invalid | |
| (Bonus) Expired entries are removed from the map | |

### E. Understanding Questions

1. Why does logout need server-side state if JWT is "stateless"?

   >  A JWT is valid until `exp` as long as the signature checks out, and nothing inside it can say "logged out". Deleting the token on the client doesn't stop a copy of it from working. So the server has to remember the revoked `jti` and reject it.

2. What happens to the denylist when the server restarts? What if there are two server instances?

   > The map is in memory, so a restart empties it and logged-out tokens work again until they expire. With two instances each has its own map, so a token revoked on one still works on the other. A shared store such as Redis or a database table fixes both problems.

3. Why store the expiry time with each jti?

   >  Once the token expires it is rejected anyway, so its denylist entry is no longer needed. Storing the expiry lets `cleanExpired()` delete old entries, so the map doesn't grow forever.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `TokenDenylist.java` | Accepted | Thread-safe, keyed by `jti`. |
| `JwtAuthenticationFilter.java` | Accepted | Revoked token is handled like an expired one, the filter writes no response. |
| `AuthController.java` | Accepted | Logout returns 204. |
| `SecurityConfig.java` | Modified | Constructor and filter creation changed only to pass the denylist. |


---

## No-AI Change Tasks



**Files changed:**

| File | What changed and why |
|---|---|
| | |

**Explanation (your own words):**
> *Describe what you changed and why it works.*
