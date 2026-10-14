# SecureGate - Login Authentication System
**Oasis Infobyte Internship &bull; Web Development Level 2 &bull; Task 4**  
**Author:** Mudassir Ahmed

---

## 📌 Project Overview
**SecureGate** is a full-stack user registration, login authentication, and protected dashboard application designed with strict security standards, modern engineering practices, and an elegant dark-mode user interface.

Built using Node.js, Express, `express-session`, and `bcryptjs`, SecureGate uses an asynchronous JSON file store with serialized write queue locking to ensure zero dependency on native compilation and full cross-platform compatibility without external database servers.

---

## ✨ Features
1. **User Registration:**
   - Input fields for Username, Email, Password, and Confirm Password.
   - Client and server-enforced validation rules (length, regex patterns, email formatting).
   - Case-insensitive duplicate checking for both usernames and emails with informative HTTP 409 error responses.
2. **Interactive Password Requirements Checklist:**
   - Real-time visual feedback checklist that ticks off requirements dynamically as the user types:
     - Minimum 8 characters.
     - At least 1 numeric digit (0-9).
   - Accessible show/hide password toggle for both password fields.
3. **Secure Login & Credential Verification:**
   - Single flexible input accepting either Username or Email.
   - Timing-attack-resistant authentication: executes a dummy bcrypt comparison when the user is not found to prevent username enumeration via timing analysis.
   - Unified, generic error message: *"Invalid credentials. Please try again."* (never exposes whether the username or password was incorrect).
4. **Protected Dashboard (`/dashboard`):**
   - Server-enforced session authentication middleware (`requireAuth`). Unauthenticated visits are immediately redirected to `/login`.
   - Never exposes password hashes to the browser.
   - Displays authenticated account info: Username, Email address, Member-Since date, encryption status, and active session flags.
   - HTTP response header hardening: delivers `Cache-Control: no-store, no-cache, must-revalidate, private` to prevent browser history navigation from displaying protected pages after logout.
5. **Session Management & Logout:**
   - Session ID regeneration upon login (`req.session.regenerate`) to protect against Session Fixation attacks.
   - Secure cookie attributes: `HttpOnly`, `SameSite=Lax`, custom maxAge (1 hour), and conditional `Secure` flag for production.
   - Dedicated logout action destroys the server-side session, purges the session cookie, and returns the user to the login screen with confirmation.
6. **In-Memory Rate Limiting:**
   - Custom IP-based rate limiter (5 failed attempts per minute per IP), responding with HTTP 429 and a countdown timer.
7. **Strict Security & XSS Defense:**
   - Zero inline JavaScript (`onclick`). Pure event-listener architecture.
   - Text rendering uses `textContent` exclusively to neutralize Cross-Site Scripting (XSS) risks.
   - Request body limit capped at 10KB to mitigate Denial of Service (DoS) memory consumption.
8. **Responsive & Accessible UI:**
   - Fully responsive layout tested at 375px (mobile), 768px (tablet), and 1440px (desktop).
   - High-contrast accessible tap targets ($\ge 44\text{px}$), ARIA live regions (`aria-live="polite"`), and clear keyboard focus rings.

---

## 🛠 Tech Stack
| Component | Technology | Rationale |
| :--- | :--- | :--- |
| **Backend Runtime** | Node.js (v18+) | Non-blocking I/O, fast execution, cross-platform |
| **Server Framework** | Express 5 | Robust routing, middleware pipeline |
| **Session Management** | `express-session` | Cryptographically signed, HTTP-only server sessions |
| **Password Hashing** | `bcryptjs` | Pure-JavaScript bcrypt implementation (no native build tools or C++ toolchains required) |
| **Environment Config** | `dotenv` | Zero-hardcoded credentials, runtime validation |
| **Data Storage** | Asynchronous JSON Store (`fs.promises`) | Lightweight zero-config persistence with serialized write locking |
| **Frontend** | HTML5, Vanilla CSS3, Vanilla ES6 JavaScript | Frameworkless, ultra-fast load time, no build step |
| **Typography** | Google Fonts (*Plus Jakarta Sans*) | Modern, clean aesthetic |

---

## 📂 Project Structure
```text
OIBSIP/WebDev-L2-LoginAuth/
├── .env.example              # Template for environment configuration
├── .env                      # Active local environment secrets (gitignored)
├── .gitignore                # Git exclusions (node_modules, .env, data/users.json)
├── package.json              # Project dependencies and npm scripts
├── server.js                 # Express server, auth middleware, and API routes
├── data/
│   └── users.json            # Auto-generated encrypted user store (gitignored)
├── public/
│   ├── login.html            # Public login page
│   ├── register.html         # Public registration page with live checklist
│   ├── style.css             # Unified dark design system & responsive styling
│   └── auth.js               # Client-side form handlers, validation, & DOM updates
├── views/
│   └── dashboard.html        # Protected dashboard template (served only via auth middleware)
├── screenshots/              # High-resolution screenshots across viewports
│   ├── desktop-login.png
│   ├── desktop-register.png
│   ├── desktop-password-checklist-active.png
│   ├── desktop-validation-errors.png
│   ├── desktop-login-error.png
│   ├── desktop-dashboard.png
│   ├── desktop-redirect-to-login.png
│   ├── mobile-login.png
│   ├── mobile-register.png
│   ├── mobile-validation-errors.png
│   ├── mobile-login-error.png
│   ├── mobile-dashboard.png
│   └── mobile-redirect-to-login.png
├── capture-screenshots.js    # Automated script to generate test screenshots
├── test-suite.js             # Automated 41-point verification suite
└── README.md                 # Complete documentation
```

---

## 🚀 Getting Started

### 1. Prerequisites
- Node.js (v18 or higher)
- npm (v9 or higher)

### 2. Installation
Navigate to the project directory and install dependencies:
```bash
cd OIBSIP/WebDev-L2-LoginAuth
npm install
```

### 3. Configure Environment Variables
Copy `.env.example` to `.env` and set a secure session secret:
```bash
cp .env.example .env
```
*(On Windows PowerShell, use: `Copy-Item .env.example .env`)*

Inside `.env`:
```env
SESSION_SECRET=e7b4c91a03df6b18974a921d45fae1654bbd7256e30bc06385cf442e97a38f92
PORT=3000
NODE_ENV=development
```

> **Security Note:** If `SESSION_SECRET` is missing, the application terminates immediately on startup with a descriptive error message to prevent unconfigured deployments.

### 4. Run the Application
Start the server:
```bash
npm start
```
Then open your browser at:
```text
http://localhost:3000
```

---

## 📡 API Routes Specification

| Method | Endpoint | Access | Description | Status Codes |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/` | Public | Redirects to `/dashboard` if logged in; otherwise to `/login` | `302` |
| `GET` | `/login` | Public | Serves the login page (redirects to `/dashboard` if already authenticated) | `200`, `302` |
| `GET` | `/register` | Public | Serves the registration page (redirects to `/dashboard` if already authenticated) | `200`, `302` |
| `GET` | `/dashboard` | Protected | Serves protected dashboard with `Cache-Control: no-store` | `200`, `302` |
| `POST` | `/api/register` | Public (Rate Limited) | Validates input, checks duplicates, hashes password with bcrypt (cost 12), saves user | `201`, `400`, `409`, `429`, `500` |
| `POST` | `/api/login` | Public (Rate Limited) | Validates credentials with dummy timing equalization, regenerates session ID | `200`, `400`, `401`, `429`, `500` |
| `POST` | `/api/logout` | Authenticated | Destroys server session and clears session cookie | `200`, `500` |
| `GET` | `/api/me` | Authenticated | Returns current user profile (Username, Email, Member Since). Never exposes hash | `200`, `401` |

---

## 🔒 Security Architecture & Explanations

### Why Plain-Text Passwords are Catastrophic
Storing passwords in plain text is one of the most severe vulnerabilities in web development:
- **Database Leaks:** If a database backup or file is leaked, compromised, or accidentally exposed, every user's raw password is instantly readable.
- **Credential Stuffing:** Because many users reuse passwords across different platforms, a compromise on one site grants attackers access to the user's email, banking, or corporate accounts.
- **Insider Threats:** Untrusted internal developers or infrastructure personnel can read user passwords without authorization.

### How Hashing & Salt Work (Bcrypt)
SecureGate utilizes **bcrypt** with a salt round cost of **12**:
1. **One-Way Cryptographic Function:** Password hashing is irreversible; the plain-text password cannot be mathematically derived from the resulting hash.
2. **Cryptographic Salt:** Bcrypt generates a unique, pseudorandom 128-bit salt for every password before hashing. This ensures that identical passwords produce completely distinct hashes, defeating precomputed **Rainbow Table** attacks.
3. **Adaptive Work Factor (Cost Factor):** Unlike general-purpose hashing functions (such as MD5 or SHA-256) which are designed for high throughput, bcrypt is intentionally computationally slow and memory-hard. A cost factor of 12 forces brute-force and GPU attackers to perform thousands of CPU-intensive iterations per guess, making dictionary attacks mathematically infeasible.

### Session Management & Fixation Defense
- **Session ID Regeneration:** Attackers can attempt a "Session Fixation" attack by provisioning a known session identifier to an unauthenticated victim and waiting for them to log in. SecureGate calls `req.session.regenerate()` immediately upon successful authentication, issuing a brand-new, unpredictable session ID.
- **Cookie Security:** The session cookie is flagged with `HttpOnly: true` (blocking client-side JavaScript access and preventing session theft via XSS) and `SameSite: 'lax'` (protecting against Cross-Site Request Forgery).

### Timing Attack Mitigation
Standard authentication systems often return immediately when a username is not found, while taking 80–120ms to hash and verify a password for an existing user. Attackers can measure response latencies to enumerate valid usernames. SecureGate eliminates this vulnerability by executing a dummy bcrypt comparison against a precomputed cost-12 hash whenever a user is not found, ensuring uniform response times.

### Concurrent File Write Serialization
To avoid data corruption in a file-based JSON store, all write operations pass through a Promise-based FIFO mutex queue (`serializeWrite`). Writes are made to unique temporary files (`users.<timestamp>.<rand>.tmp`) and committed via atomic renames (`fs.promises.rename`). Two simultaneous registrations can never interleave or corrupt `users.json`.

---

## 🧪 Comprehensive Verification & Test Cases

SecureGate has been validated with an automated 41-assertion test suite (`test-suite.js`):

| Test ID | Scenario | Expected Behavior | Result |
| :--- | :--- | :--- | :--- |
| **TC-01** | Register with valid credentials | User created, saved in `users.json` with bcrypt hash (`$2a...`), returns 201 | **PASSED** |
| **TC-02** | Password validation (< 8 chars) | Rejected with HTTP 400 and inline error message | **PASSED** |
| **TC-03** | Password validation (no numbers) | Rejected with HTTP 400 requiring at least 1 digit | **PASSED** |
| **TC-04** | Confirm password mismatch | Rejected with HTTP 400 | **PASSED** |
| **TC-05** | Duplicate username (case-insensitive) | Rejected with HTTP 409 conflict error | **PASSED** |
| **TC-06** | Duplicate email (mixed case) | Rejected with HTTP 409 conflict error | **PASSED** |
| **TC-07** | XSS payload in username (`<script>`) | Rejected by regex format validation; no script execution | **PASSED** |
| **TC-08** | Malformed email input | Rejected with HTTP 400 invalid email | **PASSED** |
| **TC-09** | Unauthenticated `/dashboard` access | Server returns 302 redirecting to `/login` | **PASSED** |
| **TC-10** | Unauthenticated `/api/me` access | Server returns HTTP 401 Unauthorized | **PASSED** |
| **TC-11** | Login with wrong password | Returns HTTP 401: *"Invalid credentials. Please try again."* | **PASSED** |
| **TC-12** | Login with non-existent username | Returns identical HTTP 401 error message and identical bcrypt latency | **PASSED** |
| **TC-13** | Successful login with Email | Session established, HTTP-only cookie issued, 200 OK | **PASSED** |
| **TC-14** | Successful login with Username | Session established, HTTP-only cookie issued, 200 OK | **PASSED** |
| **TC-15** | Authenticated `/dashboard` access | Renders protected view with user data and `Cache-Control: no-store` | **PASSED** |
| **TC-16** | Authenticated `/login` or `/register` visit | Server automatically redirects to `/dashboard` | **PASSED** |
| **TC-17** | Logout action | Session destroyed, cookie invalidated, redirect to `/login` | **PASSED** |
| **TC-18** | Browser Back button after logout | Protected dashboard cannot be recalled from cache; `/api/me` returns 401 | **PASSED** |
| **TC-19** | Brute-force rate limiting (6+ failures) | Returns HTTP 429 Too Many Requests with retry-after interval | **PASSED** |
| **TC-20** | Concurrent registration requests | Handled safely by write queue without data collision | **PASSED** |
| **TC-21** | Responsive layouts (375px, 768px, 1440px) | Flawless rendering, no horizontal scrolling, touch targets $\ge 44\text{px}$ | **PASSED** |

To run the automated test suite at any time:
```bash
node test-suite.js
```

---

## 📸 Screenshots

All screenshots are stored in the `screenshots/` directory:

### Desktop Views (1440px Viewport)
| View | Description | Screenshot |
| :--- | :--- | :--- |
| **Registration Page** | Clean registration form with password requirements checklist and visibility toggle | `screenshots/desktop-register.png` |
| **Live Checklist Active** | Password checklist dynamically updating with green verification marks as criteria are met | `screenshots/desktop-password-checklist-active.png` |
| **Validation Errors** | Form error states with inline field warnings | `screenshots/desktop-validation-errors.png` |
| **Login Page** | Clean login interface with flexible Username/Email identification | `screenshots/desktop-login.png` |
| **Login Error Banner** | Generic credential error alert for security | `screenshots/desktop-login-error.png` |
| **Protected Dashboard** | Authenticated dashboard greeting user, displaying account details and security cards | `screenshots/desktop-dashboard.png` |
| **Logout Confirmation** | Automatic redirect to login with green logout status alert | `screenshots/desktop-redirect-to-login.png` |

### Mobile Views (375px Viewport)
| View | Description | Screenshot |
| :--- | :--- | :--- |
| **Mobile Registration** | Centered mobile-optimized registration card | `screenshots/mobile-register.png` |
| **Mobile Validation Errors** | Inline validation states on mobile screen | `screenshots/mobile-validation-errors.png` |
| **Mobile Login** | Accessible touch targets and input layout | `screenshots/mobile-login.png` |
| **Mobile Error Alert** | Responsive alert banner on small viewports | `screenshots/mobile-login-error.png` |
| **Mobile Dashboard** | Full responsive vertical stacking of account information and security overview | `screenshots/mobile-dashboard.png` |
| **Mobile Logout Redirect** | Logout confirmation alert on mobile screen | `screenshots/mobile-redirect-to-login.png` |

---

## ⚠️ Known Limitations
1. **JSON File Data Store:** The file-based JSON store is implemented strictly for educational and internship demonstration purposes to eliminate external database installation requirements. For production deployments with thousands of concurrent operations, a dedicated database such as PostgreSQL or MongoDB should be used.
2. **In-Memory Session Store:** By default, Express Session uses `MemoryStore`. Sessions are stored in server RAM and reset upon application restart. In a production cluster, a distributed session store like Redis or Memcached would be paired with the application.

---

## 💡 What I Learned
Through this project, I deepened my understanding of:
1. **Defense-in-Depth Authentication:** Implementing authentication security not only at the UI layer, but fundamentally enforcing every restriction on the server.
2. **Cryptographic Best Practices:** The crucial role of adaptive hashing algorithms (bcrypt), work factor costs, and why nonces and salts prevent precomputed attacks.
3. **Session Hardening:** Mitigating Session Fixation by regenerating IDs, enforcing strict cookie policies (`HttpOnly`, `SameSite=Lax`), and preventing cache-based back navigation.
4. **Timing Attack Resilience:** Practical techniques to equalize cryptographic execution time when rejecting nonexistent accounts.
5. **Concurrency & Safe File I/O:** Serializing asynchronous filesystem writes to prevent data race conditions in Node.js.

---

## 👨‍💻 Author
**Mudassir Ahmed**  
Web Development Intern &bull; Oasis Infobyte (Level 2, Task 4)
