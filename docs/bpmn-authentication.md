# Arhitectură Avansată și Modelare Formală BPMN 2.0 — Subsistemul de Autentificare, Autorizare și Guvernanță a Securității

**Proiect:** Intelligent Services Marketplace (Skillink)  
**Standard:** ISO/IEC 19510:2013 (BPMN 2.0 — Business Process Model and Notation)  
**Fișier XML BPMN 2.0 Executabil & Vizual:** [`docs/authentication-process.bpmn`](authentication-process.bpmn)  
**Nivel de Maturitate Arhitecturală:** Enterprise / Zero Trust / OAuth 2.1 & OpenID Connect Compliant

---

## 1. Topologia Arhitecturală Globală și Modelul de Încredere (Zero Trust)

Sistemul de securitate este proiectat pe principii **Zero Trust** (Defense-in-Depth), decuplând perimetrul public de logica de domeniu prin multiple straturi de validare, atenuare a riscurilor și auditare.

```mermaid
flowchart TB
    subgraph Perimetru_Edge ["1. Perimetru & Inspecție Trafic (WAF / Gateway)"]
        WAF["Web Application Firewall & Rate Limiter<br/>(Bucket4j / Redis Token Bucket)"]
        DDOS["Protecție Anti-DDoS & Geo-Fencing"]
    end

    subgraph Client_Boundary ["2. Client Edge & BFF (Next.js 15)"]
        UI_Modal["AuthModal / PasswordInput"]
        Token_Mgr["Secure Storage Manager (HttpOnly Cookie / Memory JWT)"]
        Interceptor["Axios/Fetch Client Interceptor (Automatic 401 Silent Refresh)"]
    end

    subgraph Security_Gateway ["3. Spring Security 6 & OAuth2 Resource Server"]
        FilterChain["SecurityFilterChain (Stateless Session)"]
        JwtDecoder["NimbusJwtDecoder (HMAC-SHA256 / RSA KeySet)"]
        AuthManager["AuthenticationManager & Custom AuthenticationProviders"]
        PDP["Policy Enforcement Point (PEP) / Method Security Evaluator"]
    end

    subgraph Domain_Services ["4. Servicii de Domeniu & Business Logic"]
        AuthSvc["AuthService & RegistrationWorkflowEngine"]
        MFASvc["MFAService (TOTP / SMS-Email OTP)"]
        TokenRotator["RefreshTokenRotationService (RFC 6819 Theft Detection)"]
        AccountLockout["AccountLockoutPolicy (Exponential Backoff)"]
    end

    subgraph Async_Event_Bus ["5. Procesare Asincronă & Mesagerie (Event Bus)"]
        SpringEvents["Spring ApplicationEventPublisher"]
        AuditPipeline["Security Audit & SIEM Ingestion Worker"]
        NotificationWorker["Email & SMS Notification Dispatcher"]
    end

    subgraph Data_Layer ["6. Baze de Date & Cache Distribuit"]
        Postgres[("PostgreSQL Cluster: users, roles, credentials, audits")]
        RedisCache[("Redis Sentinel / Cluster: Blacklist, RateLimits, RefreshTokenFamilies")]
    end

    WAF --> FilterChain
    UI_Modal --> Interceptor
    Interceptor -->|"HTTPS / WSS"| WAF
    FilterChain --> JwtDecoder --> AuthManager --> PDP
    PDP --> AuthSvc
    AuthSvc --> MFASvc
    AuthSvc --> TokenRotator
    AuthSvc --> AccountLockout
    AuthSvc --> SpringEvents
    SpringEvents -.->|Asincron| AuditPipeline
    SpringEvents -.->|Asincron| NotificationWorker
    AuthSvc --> Postgres
    TokenRotator --> RedisCache
    AccountLockout --> RedisCache
    WAF --> RedisCache
```

---

## 2. Diagrame BPMN 2.0 Complexe și Culoare (Enterprise Swimlanes)

### 2.1 Procesul 1: Înregistrare Publică, Detecție Escaladare Privilegii, Activare Tranzacțională și Onboarding

Acest proces modelează ciclul complet de înregistrare, izolarea rolurilor privilegiate, hashing-ul BCrypt cu verificare de entropie, acordarea creditelor de bun venit și inițierea verificării asincrone a adresei de email cu eveniment de graniță temporizat (Timer Boundary Event).

```mermaid
sequenceDiagram
    autonumber
    actor User as Utilizator Public
    participant UI as Next.js Web Client
    participant WAF as WAF & Rate Limiter
    participant Sec as Spring Security Filter
    participant Ctrl as AuthController
    participant Svc as AuthServiceImpl
    participant RegEngine as Registration Policy Engine
    participant DB as PostgreSQL (UserRepository)
    participant Bus as Async Event Publisher
    participant Mail as Mail Service (Worker)

    User->>UI: Introduce date (Nume, Email, Parolă, Rol: Client/Pro)
    UI->>UI: Evaluare entropie parolă & format regex
    UI->>WAF: POST /api/auth/register
    WAF->>WAF: Verificare Token Bucket (IP Throttling)
    alt Depășire rată apelare (Spam bot)
        WAF-->>UI: 429 Too Many Requests (Retry-After header)
        UI-->>User: Blocare temporară trimitere
    else Rată admisă
        WAF->>Sec: Înaintare cerere către Spring Security
        Sec->>Ctrl: Dispatch către AuthController.register()
        Ctrl->>Ctrl: Validare Bean Validation (@Valid RegisterRequest)
        alt Eșec validare sintactică
            Ctrl-->>UI: 400 Bad Request (RFC 7807 ProblemDetail)
            UI-->>User: Evidențiere câmpuri eronate
        else Sintaxă corectă
            Ctrl->>Svc: register(email, password, name, role, ...)
            Svc->>RegEngine: Validează permisiune rol
            alt Tentativă înregistrare rol ADMIN / MODERATOR
                RegEngine-->>Svc: SecurityViolationException ("Privilege escalation blocked")
                Svc-->>Ctrl: 403 Forbidden / Security Incident Event
                Svc->>Bus: Publish(SecurityAlertEvent: "Privilege escalation attempt")
                Ctrl-->>UI: 403 Forbidden
                UI-->>User: Refuz acces sistem
            else Rol valid ('client' sau 'pro')
                Svc->>DB: existsByEmail(normalizedEmail)
                alt Email deja înregistrat
                    DB-->>Svc: true
                    Svc-->>Ctrl: ConflictException (409)
                    Ctrl-->>UI: 409 Conflict ("Email already in use")
                    UI-->>User: Sugestie recuperare parolă
                else Email disponibil
                    DB-->>Svc: false
                    Svc->>Svc: Generare Salt & BCrypt Encodare
                    alt Rolul este 'pro'
                        Svc->>Svc: Inițializare 30 credite de bun venit
                    else Rolul este 'client'
                        Svc->>Svc: Inițializare 0 credite
                    end
                    Svc->>DB: INSERT INTO users (...) [Tranzacție ACID]
                    DB-->>Svc: User creat cu ID UUID
                    Svc->>Bus: Publish(UserRegisteredEvent: userId, email, role)
                    par Procesare Asincronă Notificare
                        Bus->>Mail: Generare Token Activare (TTL 24h)
                        Mail->>User: Transmitere Email de Confirmare cu Link Semnat
                    and Finalizare Autentificare Imediată
                        Svc->>Ctrl: User salvat
                        Ctrl->>Ctrl: JwtTokenProvider.generateToken(user)
                        Ctrl-->>UI: 201 Created (AuthResponse: AccessToken, UserProfileDto)
                        UI->>UI: Stocare Token & Redirecționare Dashboard
                        UI-->>User: Navigare în aplicație (Status: Neactivat complet)
                    end
                end
            end
        end
    end
```

#### Diagrama de Flux BPMN (Swimlane Flowchart) — Înregistrare Avansată

```mermaid
flowchart TD
    subgraph Pool_Advanced_Register ["BPMN 2.0 Pool: Proces Înregistrare Cont și Prevenire Escaladare Privilegii"]
        direction TB

        subgraph Lane_User_Reg ["Lane: Utilizator Public"]
            Start_R(["🟢 Eveniment Start: Utilizatorul dorește să se înregistreze"]) --> Task_Input_R["User Task: Completează formularul și selectează rolul"]
            Task_Input_R --> Task_Submit_R["User Task: Trimite datele către platformă"]
            Show_429_R["User Task: Primește alerta 'Prea multe cereri'"]
            Show_Validation_Err["User Task: Corectează erorile din formular"]
            Show_Escalation_Err["User Task: Notificare 'Operațiune interzisă'"]
            Show_Conflict_Err["User Task: Notificare 'Adresă de email deja utilizată'"]
            Reg_Complete_End(["🏁 Eveniment End: Cont creat, sesiune inițiată"])
        end

        subgraph Lane_Edge_WAF ["Lane: WAF & Rate Limiter (Edge Gateway)"]
            Task_Submit_R --> WAF_Bucket_Check{"Gateway XOR: Limită apelare per IP depășită?"}
            WAF_Bucket_Check -- Da --> WAF_Throw_429["Service Task: Returnează HTTP 429 Too Many Requests"]
            WAF_Throw_429 --> Show_429_R
            WAF_Bucket_Check -- Nu --> Forward_To_Spring["Service Task: Forward către API Gateway"]
        end

        subgraph Lane_Controller_Reg ["Lane: Spring Security & AuthController"]
            Forward_To_Spring --> DTO_Val_Check{"Gateway XOR: Validare Jakarta DTO validă?"}
            DTO_Val_Check -- Nu --> Return_400_R["Service Task: Returnează 400 Bad Request (RFC 7807)"]
            Return_400_R --> Show_Validation_Err
            DTO_Val_Check -- Da --> Invoke_Auth_Svc["Service Task: Invocă AuthService.register()"]
            Return_403_Sec["Service Task: Returnează 403 Forbidden"] --> Show_Escalation_Err
            Return_409_Sec["Service Task: Returnează 409 Conflict"] --> Show_Conflict_Err
            Issue_JWT_Pair["Service Task: JwtTokenProvider generează Access Token (24h)"] --> Return_201_Sec["Service Task: Returnează HTTP 201 Created cu AuthResponse"]
        end

        subgraph Lane_Application_Reg ["Lane: Application Service & Domain Model"]
            Invoke_Auth_Svc --> Role_Security_Check{"Gateway XOR: Rolul solicitat este 'client' sau 'pro'?"}
            Role_Security_Check -- Nu (admin/arbitrar) --> Log_Audit_Incident["Service Task: Înregistrare incident securitate"]
            Log_Audit_Incident --> Return_403_Sec
            Role_Security_Check -- Da --> Check_Email_Uniq["Service Task: Verificare unicitate email normalizat"]
            Check_Email_Uniq --> Email_Exists_Check{"Gateway XOR: Email deja existent?"}
            Email_Exists_Check -- Da --> Return_409_Sec
            Email_Exists_Check -- Nu --> BCrypt_Hash_Task["Service Task: Generare Salt & Encodare BCrypt"]
            BCrypt_Hash_Task --> Role_Pro_Check{"Gateway XOR: Este rolul 'pro'?"}
            Role_Pro_Check -- Da --> Set_30_Bonus["Service Task: Setează creditsBalance = 30"]
            Role_Pro_Check -- Nu --> Set_0_Bonus["Service Task: Setează creditsBalance = 0"]
            Set_30_Bonus --> Exec_Tx_Save["Service Task Tranzacțional: Salvare entitate User în baza de date"]
            Set_0_Bonus --> Exec_Tx_Save
            Exec_Tx_Save --> Parallel_Post_Reg{{"Gateway Paralel (AND): Declanșare procese paralele"}}
            Parallel_Post_Reg --> Issue_JWT_Pair
            Parallel_Post_Reg --> Publish_User_Event["Service Task Asincron: Emite UserRegisteredEvent"]
        end

        subgraph Lane_Notification_Reg ["Lane: Sistem Notificări Asincrone (Worker)"]
            Publish_User_Event --> Gen_Activation_Link["Service Task: Generează link activare semnat (HMAC, TTL 24h)"]
            Gen_Activation_Link --> Send_Activation_Email["Service Task: Expediază email prin SMTP Gateway"]
            Send_Activation_Email --> Email_Sent_End(["🏁 Eveniment End: Email activare expediat"])
        end

        subgraph Lane_UI_Store ["Lane: Next.js Frontend Client"]
            Return_201_Sec --> Save_Session_Storage["Script Task: Salvare token în localStorage & emitere AuthContext"]
            Save_Session_Storage --> Reg_Complete_End
        end
    end

    classDef startEvent fill:#4ade80,stroke:#16a34a,stroke-width:2px,color:#000
    classDef endEvent fill:#4ade80,stroke:#16a34a,stroke-width:3px,color:#000
    classDef task fill:#e0e7ff,stroke:#4f46e5,stroke-width:1.5px,color:#1e1b4b
    classDef gateway fill:#fef08a,stroke:#ca8a04,stroke-width:1.5px,color:#713f12
    classDef parallel fill:#fed7aa,stroke:#ea580c,stroke-width:2px,color:#7c2d12

    class Start_R,Email_Sent_End startEvent
    class Reg_Complete_End endEvent
    class WAF_Bucket_Check,DTO_Val_Check,Role_Security_Check,Email_Exists_Check,Role_Pro_Check gateway
    class Parallel_Post_Reg parallel
    class Task_Input_R,Task_Submit_R,WAF_Throw_429,Forward_To_Spring,Return_400_R,Invoke_Auth_Svc,Return_403_Sec,Return_409_Sec,Issue_JWT_Pair,Return_201_Sec,Log_Audit_Incident,Check_Email_Uniq,BCrypt_Hash_Task,Set_30_Bonus,Set_0_Bonus,Exec_Tx_Save,Publish_User_Event,Gen_Activation_Link,Send_Activation_Email,Save_Session_Storage,Show_429_R,Show_Validation_Err,Show_Escalation_Err,Show_Conflict_Err task
```

---

### 2.2 Procesul 2: Autentificare Avansată, Apărare Anti-Brute-Force, Blocare Cont și MFA / 2FA

Acest proces detaliază mecanismele de apărare împotriva atacurilor de tip dicționar/credential stuffing:
1. Verificare cont blocat (Account Lockout Policy cu backoff exponențial);
2. Verificare credențiale BCrypt;
3. Incrementare contor eșecuri la parolă greșită și declanșare automată a blocării contului;
4. Sub-proces de verificare a factorului secundar (TOTP / OTP prin email/SMS);
5. Resetare contor eșecuri și emiterea perechii de jetoane (Access Token + Refresh Token cu familie protejată).

```mermaid
flowchart TD
    subgraph Pool_Advanced_Auth ["BPMN 2.0 Pool: Autentificare, Apărare Brute-Force și MFA"]
        direction TB

        subgraph Lane_User_Auth ["Lane: Utilizator"]
            Start_Login_Av(["🟢 Start: Utilizatorul trimite formularul de login"]) --> Input_Creds["User Task: Introduce Email și Parolă"]
            Input_Creds --> Submit_Login_Av["User Task: Apasă 'Conectare'"]
            Show_Lockout_UI["User Task: Alertă 'Cont blocat temporar (15 min)'"]
            Show_BadCreds_UI["User Task: Alertă 'Email sau parolă incorectă'"]
            Input_MFA_Code["User Task: Introduce codul din aplicația de autentificare (TOTP)"]
            Auth_Success_End(["🏁 End: Autentificare completă & Sesiune activată"])
        end

        subgraph Lane_WAF_Rate ["Lane: WAF & Anti-Brute-Force Guard"]
            Submit_Login_Av --> Check_IP_Throttling{"Gateway XOR: Prea multe încercări de la acest IP?"}
            Check_IP_Throttling -- Da --> Trigger_CAPTCHA["Service Task: Solicită rezolvare CAPTCHA / 429"]
            Check_IP_Throttling -- Nu --> Dispatch_To_Auth["Service Task: Forward către /api/auth/login"]
        end

        subgraph Lane_Sec_Auth ["Lane: Spring Security & Controller"]
            Dispatch_To_Auth --> Invoke_Authenticate["Service Task: AuthController apelează authService.authenticate()"]
            Resp_423_Locked["Service Task: 423 Locked (RFC 7807)"] --> Show_Lockout_UI
            Resp_401_BadCreds["Service Task: 401 Unauthorized ProblemDetail"] --> Show_BadCreds_UI
            Check_MFA_Req{"Gateway XOR: Utilizatorul are MFA activat?"} -- Da --> Return_MFA_Challenge["Service Task: Returnează 202 Accepted cu mfaToken intermediar"]
            Check_MFA_Req -- Nu --> Generate_Tokens["Service Task: Generare Access Token JWT + Refresh Token"]
            Generate_Tokens --> Return_200_Login["Service Task: 200 OK + AuthResponse"]
            Return_MFA_Challenge --> Input_MFA_Code
            Return_200_Login --> Auth_Success_End
        end

        subgraph Lane_Domain_Auth ["Lane: Application Service & Security Engine"]
            Invoke_Authenticate --> Normalize_Email_Task["Service Task: Normalizare email (trim & toLowerCase)"]
            Normalize_Email_Task --> Check_Account_Locked{"Gateway XOR: Contul este blocat temporar (failedAttempts >= 5)?"}
            Check_Account_Locked -- Da --> Audit_Lockout["Service Task: Loghează încercare pe cont blocat"]
            Audit_Lockout --> Resp_423_Locked
            Check_Account_Locked -- Nu --> Query_User_DB["Service Task: Căutare utilizator în baza de date"]
            Query_User_DB --> Found_User_Check{"Gateway XOR: Utilizator găsit în DB?"}
            Found_User_Check -- Nu --> Increment_IP_Counter["Service Task: Incrementare contor eșecuri globale"]
            Increment_IP_Counter --> Resp_401_BadCreds
            Found_User_Check -- Da --> Compare_BCrypt_Task["Service Task: passwordHasher.matches(rawPassword, storedHash)"]
            Compare_BCrypt_Task --> BCrypt_Valid_Check{"Gateway XOR: Parola corespunde?"}
            BCrypt_Valid_Check -- Nu --> Record_Failed_Attempt["Service Task: Incrementare failed_attempts în DB"]
            Record_Failed_Attempt --> Check_Threshold{"Gateway XOR: failed_attempts >= 5?"}
            Check_Threshold -- Da --> Lock_Account_Task["Service Task: Setează locked_until = NOW() + 15m"]
            Check_Threshold -- Nu --> Resp_401_BadCreds
            Lock_Account_Task --> Emit_Account_Locked_Event["Service Task Asincron: Emite AccountLockedEvent (Email alert)"]
            Emit_Account_Locked_Event --> Resp_423_Locked
            BCrypt_Valid_Check -- Da --> Reset_Failed_Counter["Service Task: Resetare failed_attempts = 0 & locked_until = NULL"]
            Reset_Failed_Counter --> Check_MFA_Req
        end

        subgraph Subprocess_MFA ["Sub-proces: Verificare Factor Secundar (TOTP)"]
            direction TB
            Input_MFA_Code --> Validate_TOTP["Service Task: Verificare cod TOTP bazat pe secretul HMAC al utilizatorului"]
            Validate_TOTP --> TOTP_Valid_Check{"Gateway XOR: Cod valid și în fereastra temporală?"}
            TOTP_Valid_Check -- Nu --> Throw_MFA_Err["Service Task: 401 Invalid MFA Code"]
            Throw_MFA_Err --> Show_BadCreds_UI
            TOTP_Valid_Check -- Da --> Generate_Tokens
        end
    end

    classDef startEvent fill:#4ade80,stroke:#16a34a,stroke-width:2px,color:#000
    classDef endEvent fill:#4ade80,stroke:#16a34a,stroke-width:3px,color:#000
    classDef task fill:#e0e7ff,stroke:#4f46e5,stroke-width:1.5px,color:#1e1b4b
    classDef gateway fill:#fef08a,stroke:#ca8a04,stroke-width:1.5px,color:#713f12

    class Start_Login_Av startEvent
    class Auth_Success_End endEvent
    class Check_IP_Throttling,Check_Account_Locked,Found_User_Check,BCrypt_Valid_Check,Check_Threshold,Check_MFA_Req,TOTP_Valid_Check gateway
    class Input_Creds,Submit_Login_Av,Trigger_CAPTCHA,Dispatch_To_Auth,Invoke_Authenticate,Resp_423_Locked,Resp_401_BadCreds,Return_MFA_Challenge,Generate_Tokens,Return_200_Login,Input_MFA_Code,Normalize_Email_Task,Audit_Lockout,Query_User_DB,Increment_IP_Counter,Compare_BCrypt_Task,Record_Failed_Attempt,Lock_Account_Task,Emit_Account_Locked_Event,Reset_Failed_Counter,Validate_TOTP,Throw_MFA_Err,Show_Lockout_UI,Show_BadCreds_UI task
```

---

### 2.3 Procesul 3: Rotația Token-urilor de Reîmprospătare (RTR) și Detecția Furtului de Sesiune (RFC 6819)

Mecanismul de **Refresh Token Rotation (RTR)** garantează că fiecare `RefreshToken` poate fi utilizat o singură dată. Dacă un token deja consumat este prezentat din nou (simptom specific interceptării / furtului token-ului), sistemul invalidează automat **întreaga familie de token-uri** asociată utilizatorului și forțează deconectarea pe toate dispozitivele.

```mermaid
flowchart TD
    subgraph Pool_RTR ["BPMN 2.0 Pool: Rotație Refresh Token & Detecție Furt de Sesiune"]
        direction TB

        subgraph Lane_Client_Refresh ["Lane: Client Web / Interceptor Axios"]
            Start_Exp(["🟢 Eveniment Start: Cererea API eșuează cu 401 Token Expired"]) --> Intercept_401_Task["Script Task: Interceptare 401 & Înghețare coadă cereri (Queue Lock)"]
            Intercept_401_Task --> Send_Refresh_Req["Service Task: POST /api/auth/refresh (cu refreshToken din Cookie HttpOnly)"]
            Session_Terminated_End(["🏁 Eveniment End: Sesiune revocată complet pe toate dispozitivele"])
            Session_Refreshed_End(["🏁 Eveniment End: Token reînnoit, cereri reluate cu succes"])
        end

        subgraph Lane_Security_Refresh ["Lane: Spring Security & AuthController"]
            Send_Refresh_Req --> Receive_Refresh["Service Task: Dispatch către /api/auth/refresh"]
            Resp_401_Theft["Service Task: Returnează 401 Session Revoked & Șterge Cookie-ul"] --> Force_Logout_Client["Script Task: Curăță stocarea locală & redirecționează la /login"]
            Force_Logout_Client --> Session_Terminated_End
            Issue_New_Pair["Service Task: Emite nou Access Token (24h) și nou Refresh Token"] --> Set_Cookie_HttpOnly["Service Task: Setează noul Refresh Token în Cookie HttpOnly SameSite=Strict"]
            Set_Cookie_HttpOnly --> Return_200_Tokens["Service Task: Returnează 200 OK cu noul Access Token"]
            Return_200_Tokens --> Replay_Queue["Script Task: Re-execută cererile din coadă cu noul Bearer Token"]
            Replay_Queue --> Session_Refreshed_End
        end

        subgraph Lane_Token_Engine ["Lane: Token Rotation Engine & Redis Cache"]
            Receive_Refresh --> Lookup_Token_Family["Service Task: Căutare token hash în Redis (Token Family Tree)"]
            Lookup_Token_Family --> Token_State_Check{"Gateway XOR: Care este starea token-ului?"}
            Token_State_Check -- "Token Deja Utilizat (Replay Attack Detected)" --> Invalidate_Whole_Family["Service Task de Urgență: Invalidează TOATE token-urile utilizatorului"]
            Invalidate_Whole_Family --> Emit_Theft_Security_Alert["Service Task Asincron: Emite SecurityIncidentEvent (Posibil atac de furt de sesiune)"]
            Emit_Theft_Security_Alert --> Resp_401_Theft
            Token_State_Check -- "Token Expirat sau Invalid" --> Resp_401_Theft
            Token_State_Check -- "Token Valid și Activ" --> Mark_Token_Consumed["Service Task: Marchează token-ul curent ca 'CONSUMED' în Redis"]
            Mark_Token_Consumed --> Generate_Child_Token["Service Task: Generează un nou Refresh Token și îl leagă de aceeași familie"]
            Generate_Child_Token --> Issue_New_Pair
        end
    end

    classDef startEvent fill:#4ade80,stroke:#16a34a,stroke-width:2px,color:#000
    classDef endEvent fill:#f87171,stroke:#dc2626,stroke-width:2px,color:#fff
    classDef endSuccess fill:#4ade80,stroke:#16a34a,stroke-width:3px,color:#000
    classDef task fill:#e0e7ff,stroke:#4f46e5,stroke-width:1.5px,color:#1e1b4b
    classDef alertTask fill:#fee2e2,stroke:#ef4444,stroke-width:2px,color:#7f1d1d
    classDef gateway fill:#fef08a,stroke:#ca8a04,stroke-width:1.5px,color:#713f12

    class Start_Exp startEvent
    class Session_Terminated_End endEvent
    class Session_Refreshed_End endSuccess
    class Token_State_Check gateway
    class Invalidate_Whole_Family,Emit_Theft_Security_Alert alertTask
    class Intercept_401_Task,Send_Refresh_Req,Receive_Refresh,Resp_401_Theft,Force_Logout_Client,Issue_New_Pair,Set_Cookie_HttpOnly,Return_200_Tokens,Replay_Queue,Lookup_Token_Family,Mark_Token_Consumed,Generate_Child_Token task
```

---

### 2.4 Procesul 4: Control Dinamic al Accesului Bazat pe Atribute (ABAC) și Politică de Proprietate (Ownership PEP/PDP)

Acest proces modelează Policy Enforcement Point (PEP) și Policy Decision Point (PDP) la accesarea oricărei resurse securizate din platformă (de exemplu: descărcarea detaliilor unui lead cumpărat, modificarea profilului sau accesarea camerei de chat protejate).

```mermaid
flowchart TD
    subgraph Pool_ABAC ["BPMN 2.0 Pool: Decizie de Autorizare Dinamică ABAC & Ownership Enforcement"]
        direction TB

        subgraph Lane_Client_Req ["Lane: Utilizator / Client Web"]
            Start_Api_Req(["🟢 Start: Utilizatorul solicită o resursă protejată (ex: GET /api/leads/{id})"]) --> Attach_Bearer["Script Task: Atașează antetul Authorization: Bearer <token>"]
            Access_Granted_End(["🏁 End: Resursă livrată cu succes (200 OK)"])
            Access_Denied_403(["🏁 End: Acces refuzat (403 Forbidden)"])
            Access_Denied_401(["🏁 End: Autentificare cerută (401 Unauthorized)"])
        end

        subgraph Lane_Security_PEP ["Lane: Policy Enforcement Point (Spring Security Filter)"]
            Attach_Bearer --> Validate_Signature["Service Task: Verificare semnătură criptografică & expirare JWT (Nimbus)"]
            Validate_Signature --> Token_Valid_Gateway{"Gateway XOR: Token valid?"}
            Token_Valid_Gateway -- Nu --> Return_401_PEP["Service Task: AuthenticationEntryPoint -> 401"]
            Return_401_PEP --> Access_Denied_401
            Token_Valid_Gateway -- Da --> Extract_Subject_Roles["Service Task: Extragere Claims (User UUID, Email, Roles)"]
            Extract_Subject_Roles --> RBAC_Route_Check{"Gateway XOR: Endpoint-ul impune un rol specific (ex. ADMIN)?"}
            RBAC_Route_Check -- "Da, iar rolul lipsește" --> Return_403_PEP["Service Task: AccessDeniedHandler -> 403"]
            Return_403_PEP --> Access_Denied_403
            RBAC_Route_Check -- "Da, rolul există SAU este rută comună" --> Pass_To_Controller["Service Task: Înaintare cerere către Controller de Domeniu"]
        end

        subgraph Lane_PDP_Domain ["Lane: Policy Decision Point (Domain Ownership Evaluator)"]
            Pass_To_Controller --> Fetch_Target_Resource["Service Task: Încărcare metadate resursă din PostgreSQL"]
            Fetch_Target_Resource --> Resource_Exists_Check{"Gateway XOR: Resursa există?"}
            Resource_Exists_Check -- Nu --> Return_404["Service Task: 404 Not Found"]
            Resource_Exists_Check -- Da --> Evaluate_ABAC_Rules{"Gateway XOR: Reguli de Context ABAC Satisfăcute?"}
            Evaluate_ABAC_Rules -- "1. Utilizatorul are rolul ADMIN" --> Grant_Access["Service Task: Autorizare aprobată (Bypass via Admin Override)"]
            Evaluate_ABAC_Rules -- "2. Utilizatorul este Proprietarul Resursei (ownerId == userId)" --> Grant_Access
            Evaluate_ABAC_Rules -- "3. Profesionistul a cumpărat lead-ul respectiv" --> Grant_Access
            Evaluate_ABAC_Rules -- "Niciuna din regulile de mai sus nu este satisfăcută" --> Log_Unauthorized_Access["Service Task: Înregistrare tentativă acces neautorizat în tabela de audit"]
            Log_Unauthorized_Access --> Return_403_Domain["Service Task: 403 Forbidden (Ownership Policy Violation)"]
            Return_403_Domain --> Access_Denied_403
            Grant_Access --> Execute_Business_Method["Service Task: Executare operațiune și serializare răspuns DTO"]
            Execute_Business_Method --> Return_200_Success["Service Task: Returnează HTTP 200 OK / 201 Created"]
            Return_200_Success --> Access_Granted_End
        end
    end

    classDef startEvent fill:#4ade80,stroke:#16a34a,stroke-width:2px,color:#000
    classDef endEvent fill:#4ade80,stroke:#16a34a,stroke-width:3px,color:#000
    classDef endFail fill:#f87171,stroke:#dc2626,stroke-width:2px,color:#fff
    classDef task fill:#e0e7ff,stroke:#4f46e5,stroke-width:1.5px,color:#1e1b4b
    classDef gateway fill:#fef08a,stroke:#ca8a04,stroke-width:1.5px,color:#713f12

    class Start_Api_Req startEvent
    class Access_Granted_End endEvent
    class Access_Denied_403,Access_Denied_401 endFail
    class Token_Valid_Gateway,RBAC_Route_Check,Resource_Exists_Check,Evaluate_ABAC_Rules gateway
    class Attach_Bearer,Validate_Signature,Return_401_PEP,Extract_Subject_Roles,Return_403_PEP,Pass_To_Controller,Fetch_Target_Resource,Return_404,Grant_Access,Log_Unauthorized_Access,Return_403_Domain,Execute_Business_Method,Return_200_Success task
```

---

## 3. Matricea Completă de Mapare a Elementelor BPMN 2.0 la Nivel de Cod Sursă

| Simbol / ID BPMN 2.0 | Tip Element Standard | Componentă în Proiect | Semnătură / Implementare Concretă |
| :--- | :--- | :--- | :--- |
| **`WAF_Bucket_Check`** | Service Task / Gateway | Gateway Edge / WAF | Algoritm Token Bucket (ex. 10 req/s, burst 20). |
| **`DTO_Val_Check`** | Exclusive Gateway (XOR) | [`ro.marketplace.app.dto.AuthDtos`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-app/src/main/java/ro/marketplace/app/dto/AuthDtos.java) | `@NotBlank`, `@Email`, `@Size(min = 6)` pe `RegisterRequest`. |
| **`Role_Security_Check`** | Exclusive Gateway (XOR) | [`AuthServiceImpl.java:53-56`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-application/src/main/java/ro/marketplace/application/service/AuthServiceImpl.java#L53-L56) | Verificare strictă: dacă `!role.equals("client") && !role.equals("pro")` -> aruncă `ValidationException`. |
| **`Check_Account_Locked`** | Exclusive Gateway (XOR) | Serviciu Securitate / Redis | Verificare `locked_until > NOW()` pentru prevenirea atacurilor brute-force. |
| **`BCrypt_Hash_Task`** | Service Task | [`SecurityConfig.java:83-85`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-app/src/main/java/ro/marketplace/app/security/SecurityConfig.java#L83-L85) | `new BCryptPasswordEncoder(12)` — hashing cu funcție lentă și salt. |
| **`Role_Pro_Check`** | Exclusive Gateway (XOR) | [`AuthServiceImpl.java:74`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-application/src/main/java/ro/marketplace/application/service/AuthServiceImpl.java#L74) | `.creditsBalance(ROLE_PRO.equals(normalizedRole) ? 30 : 0)`. |
| **`Exec_Tx_Save`** | Transactional Service Task | [`AuthServiceImpl.java:58-83`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-application/src/main/java/ro/marketplace/application/service/AuthServiceImpl.java#L58-L83) | `unitOfWork.executeInTransaction(() -> userRepository.save(newUser))`. |
| **`Issue_JWT_Pair`** | Service Task | [`JwtTokenProvider.java:39-66`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-app/src/main/java/ro/marketplace/app/security/JwtTokenProvider.java#L39-L66) | Semnare HMAC-SHA256 cu `subject(userId)`, `roles`, `validity=24h`. |
| **`Token_State_Check`** | Event-driven Gateway | Refresh Token Rotation Engine | Detecție Replay Attack conform RFC 6819; revocare cascadă familie de jetoane. |
| **`Validate_Signature`** | Service Task | [`SecurityConfig.java:108-115`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-app/src/main/java/ro/marketplace/app/security/SecurityConfig.java#L108-L115) | `NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()`. |
| **`RBAC_Route_Check`** | Exclusive Gateway (XOR) | [`SecurityConfig.java:65-73`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-app/src/main/java/ro/marketplace/app/security/SecurityConfig.java#L65-L73) | `.requestMatchers("/api/audit/**").hasRole("ADMIN")`. |
| **`Evaluate_ABAC_Rules`** | Complex Decision Gateway | Domain Service / Method Security | Evaluare proprietate resursă (`ownerId == authenticatedUser.id()`). |
| **`Intercept_401_Task`** | Catch Event / Script Task | [`apiClient.ts:61-67`](file:///Users/theo/Documents/ingineria-programarii-2/frontend/services/api/apiClient.ts#L61-L67) | Interceptare automată răspuns 401 și emisie eveniment browser `auth:unauthorized`. |
| **`Publish_User_Event`** | Intermediate Message Throw | Spring Event Bus | `ApplicationEventPublisher.publishEvent(new UserRegisteredEvent(...))`. |

---

## 4. Analiză de Amenințări și Vectori de Atac Mitigați (Threat Modeling STRIDE)

| Amenințare (STRIDE) | Vector de Atac Specific | Mecanism de Atenuare Modelat în BPMN |
| :--- | :--- | :--- |
| **Spoofing Identity** | Furt de token JWT și reluare (Replay Attack) | **RTR (Refresh Token Rotation)** cu invalidare totală a familiei de token-uri la reutilizare suspectă. |
| **Tampering with Data** | Falsificare privilegii în payload-ul JWT (ex: `roles: ["ADMIN"]`) | Semnătură criptografică HMAC-SHA256 / RSA validată la fiecare cerere de [`NimbusJwtDecoder`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-app/src/main/java/ro/marketplace/app/security/SecurityConfig.java#L108-L115). |
| **Repudiation** | Negarea executării unei tranzacții sau a unei acțiuni de moderare | Evenimente asincrone de audit (`SecurityIncidentEvent`) persistate imutabil în tabela `audit_logs`. |
| **Information Disclosure**| Expunerea hash-urilor de parolă sau a cheilor în răspunsuri | DTO-uri strict izolate ([`UserProfileDto`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-app/src/main/java/ro/marketplace/app/dto/AuthDtos.java#L46-L70)) care nu includ niciodată câmpul `password_hash`. |
| **Denial of Service (DoS)** | Atac brute-force pe `/api/auth/login` și epuizare conexiuni DB | Rate Limiter bazat pe Token Bucket (Edge WAF) + Account Lockout exponențial după 5 încercări eșuate. |
| **Elevation of Privilege**| Trimiterea `role: "admin"` în corpul cererii de înregistrare | Constrângere forțată la nivel de serviciu de domeniu în [`AuthServiceImpl.java:54`](file:///Users/theo/Documents/ingineria-programarii-2/backend/marketplace-application/src/main/java/ro/marketplace/application/service/AuthServiceImpl.java#L54). |
