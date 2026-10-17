# **Platformă digitală inteligentă pentru conectarea clienților cu profesioniștii: dezvoltarea unui marketplace de servicii bazat pe lead-uri**

**Propunere de proiect – Ingineria Programării**  
**Perioada de implementare: săptămânile 3–14**

---

## **Rezumat**

Proiectul propune dezvoltarea unei platforme digitale inteligente care facilitează conectarea persoanelor ce au nevoie de anumite servicii cu profesioniștii și companiile capabile să le ofere. Platforma va funcționa ca un marketplace multidomeniu, acoperind servicii precum dezvoltare software, construcții, reparații, contabilitate, educație, marketing, design și alte activități profesionale.

Modelul economic principal va fi bazat pe comercializarea lead-urilor: clienții vor putea publica gratuit solicitări, iar profesioniștii vor plăti pentru accesul la oportunitățile comerciale relevante pentru activitatea lor.

Dezvoltarea va fi realizată incremental. Prima etapă urmărește livrarea unei aplicații web funcționale end-to-end, care să integreze gestionarea utilizatorilor, publicarea solicitărilor, asocierea automată cu profesioniști, comunicarea în timp real, monetizarea lead-urilor și soluționarea sesizărilor.

Un element central al proiectului îl reprezintă **integrarea inteligenței artificiale pentru îmbunătățirea procesului de identificare, calificare și distribuire a solicitărilor**.

Soluția propusă urmărește dezvoltarea unui sistem denumit provizoriu *Intelligent Lead Qualification & Explainable Matching*, prin care un asistent conversațional bazat pe modele lingvistice de mari dimensiuni (LLM) poate identifica nevoile reale ale clientului, poate solicita clarificări și poate genera automat o descriere structurată a proiectului.

Ulterior, sistemul va putea integra algoritmi de matching semantic și mecanisme de evaluare a calității lead-urilor, oferind profesioniștilor oportunități comerciale mai bine definite și explicații privind compatibilitatea cu solicitările primite.

După finalizarea versiunii web, proiectul poate fi extins către aplicații mobile, comunicare audio-video și integrarea plăților online, transformând platforma într-un ecosistem complet pentru colaborarea dintre clienți și profesioniști.

**Cuvinte-cheie:** marketplace, lead generation, Spring Boot, Next.js, matching algorithms, artificial intelligence, LLM, semantic matching, explainable AI, conversational recommender systems.

---

# **1\. Descrierea proiectului și tehnologiile propuse**

## **1.1. Problema identificată**

Identificarea unui profesionist potrivit poate reprezenta un proces dificil pentru un potențial client, în special atunci când acesta nu cunoaște exact categoria de servicii necesară, prețurile practicate sau criteriile prin care poate evalua un prestator.

În multe situații, clientul nu este suficient de familiarizat cu domeniul în care dorește să contracteze un serviciu pentru a formula o solicitare completă.

De exemplu, o persoană care dorește să își digitalizeze afacerea poate avea nevoie de un magazin online, o aplicație personalizată sau doar un site de prezentare, fără să cunoască diferențele dintre aceste soluții.

Din perspectiva profesioniștilor, atragerea constantă a clienților implică investiții în publicitate, promovare și administrarea relațiilor comerciale.

Mai mult, în cazul platformelor de generare a lead-urilor, profesioniștii pot suporta costuri pentru solicitări care nu corespund serviciilor oferite, au informații insuficiente sau nu exprimă o intenție clară de contractare.

Soluția propusă urmărește reducerea acestor dificultăți prin centralizarea cererii și ofertei într-o platformă comună și, ulterior, prin introducerea unor mecanisme inteligente de calificare a solicitărilor.

Se disting trei tipuri principale de utilizatori:

* **Clientul:** persoana fizică sau juridică ce publică o solicitare, primește propuneri și selectează un profesionist.  
* **Profesionistul:** persoana sau compania care își configurează serviciile, primește oportunități și plătește pentru lead-urile acceptate.  
* **Administratorul:** utilizatorul responsabil de moderare, gestionarea sesizărilor, verificarea solicitărilor și supravegherea activității comerciale.

## **1.2. Arhitectura tehnologică**

Pentru prima versiune a aplicației propunem următorul stack tehnologic:

| Componentă | Tehnologie | Responsabilitate |
| ----- | ----- | ----- |
| Frontend | Next.js, React, TypeScript | Interfața web și interacțiunea cu utilizatorii |
| Backend | Java, Spring Boot | Logica de business și expunerea API-urilor |
| Persistență | PostgreSQL, Spring Data JPA | Stocarea datelor și gestionarea relațiilor |
| Autentificare | Spring Security | Autorizare și controlul accesului |
| Comunicare live | WebSocket | Chat și actualizări în timp real |
| Migrații | Flyway | Versionarea schemei bazei de date |
| Inteligență artificială | LLM prin API, cu validarea rezultatelor | Clarificarea și structurarea solicitărilor |
| Matching semantic (ulterior) | Embeddings, pgvector | Compararea semantică a proiectelor și profilurilor |
| Testare | JUnit, Testcontainers, Playwright | Teste unitare, de integrare și end-to-end |
| Infrastructură | Docker, GitHub Actions | Rulare reproductibilă și integrare continuă |

**Arhitectura recomandată este un monolit modular**, cu module separate pentru autentificare, utilizatori, solicitări, matching, lead-uri, chat, sesizări și administrare.

Această abordare permite izolarea responsabilităților și dezvoltarea independentă a funcționalităților, fără complexitatea operațională asociată unei arhitecturi bazate pe microservicii.

Backend-ul va expune un API REST, utilizat de aplicația Next.js. Comunicarea în timp real va fi realizată prin WebSocket, iar PostgreSQL va reprezenta sursa principală de adevăr pentru datele persistente.

Componenta AI va fi izolată printr-o interfață dedicată, astfel încât aplicația să nu depindă direct de un anumit furnizor de modele lingvistice.

Arhitectura va fi pregătită pentru extindere, astfel încât viitoarele aplicații mobile sau servicii AI mai complexe să poată reutiliza API-urile existente.

## **1.3. Funcționalitățile primei versiuni (MVP)**

### **A. Gestionarea utilizatorilor și profilurilor**

Utilizatorii își vor putea crea conturi și autentifica în platformă. Profesioniștii vor beneficia de profiluri extinse, care includ serviciile oferite, localitățile deservite, disponibilitatea, experiența și informațiile de contact.

Vor exista permisiuni distincte pentru clienți, profesioniști și administratori.

### **B. Publicarea și administrarea solicitărilor**

Clienții vor putea publica cereri descriind problema sau proiectul pe care doresc să îl realizeze.

O solicitare va include categoria serviciului, descrierea, localitatea, bugetul orientativ, termenul de execuție și, opțional, documente sau fotografii.

Cererile vor parcurge un proces de validare înainte să fie distribuite profesioniștilor.

Interfața va permite atât introducerea clasică a informațiilor prin formulare, cât și, în limita prototipului AI disponibil, generarea unei cereri structurate în urma unei conversații.

### **C. Algoritm de matching între clienți și profesioniști**

Una dintre funcționalitățile centrale va fi identificarea automată a profesioniștilor compatibili cu fiecare solicitare.

Prima implementare va folosi un algoritm determinist, bazat pe criterii precum:

* Compatibilitatea dintre categoria solicitării și serviciile profesionistului.  
* Localizarea geografică și aria de activitate.  
* Bugetul și disponibilitatea profesionistului.  
* Calitatea profilului și istoricul răspunsurilor.

Profesioniștii eligibili vor putea fi ordonați printr-un scor calculat pe baza criteriilor ponderate.

Distribuirea unui lead va fi limitată la un număr configurabil de profesioniști, pentru a evita concurența excesivă și distribuirea necontrolată a datelor clienților.

În prima etapă, vom folosi reguli explicite și ușor de testat. Algoritmii de matching semantic și personalizare vor putea fi introduși ulterior.

### **D. Monetizarea lead-urilor**

Modelul de monetizare propus presupune plata unei taxe de către profesionist atunci când acceptă o oportunitate și obține dreptul de a contacta clientul.

Un flux posibil este:

1. Clientul publică o solicitare gratuită.  
2. Platforma verifică solicitarea și identifică profesioniști compatibili.  
3. Profesioniștii primesc o notificare și pot vizualiza detaliile generale ale proiectului.  
4. Profesionistul acceptă lead-ul și este taxat din soldul său de credite.  
5. Sunt deblocate datele de contact și comunicarea directă cu clientul.

Prețul unui lead poate varia în funcție de categoria serviciului, complexitatea proiectului sau bugetul estimat.

Pentru MVP, vom implementa un sistem de credite virtuale, cu evidența tranzacțiilor, fără a impune inițial integrarea unui procesator de plăți reale.

Taxarea unui lead trebuie să fie atomică, astfel încât sistemul să nu permită debitări duplicate sau achiziții care depășesc soldul disponibil.

### **E. Sistem de chat în timp real**

Clienții și profesioniștii conectați printr-un lead vor putea comunica direct din platformă.

Sistemul va include trimiterea și recepționarea mesajelor, istoricul conversațiilor, notificarea mesajelor noi și restricționarea accesului la conversații doar pentru participanții autorizați.

Mesajele vor fi persistate în baza de date, iar WebSocket va asigura transmiterea lor în timp real.

### **F. Sesizări și soluționarea disputelor**

Un element important al platformei îl reprezintă posibilitatea ca profesioniștii să conteste un lead pentru care au fost taxați.

Exemple de situații:

* Solicitarea aparține unei categorii de servicii greșite.  
* Clientul nu poate fi contactat sau datele furnizate sunt invalide.  
* Clientul nu are un buget compatibil cu cerințele proiectului.  
* Solicitarea este duplicată, falsă sau nu exprimă o intenție reală de achiziție.

Profesionistul va putea trimite o sesizare însoțită de o explicație și eventuale dovezi.

Administratorul va analiza solicitarea și va putea aproba sau respinge rambursarea creditelor.

Sistemul va păstra istoricul deciziilor pentru auditabilitate și prevenirea abuzurilor.

Într-o etapă ulterioară, informațiile rezultate din sesizări vor putea fi utilizate pentru evaluarea și îmbunătățirea mecanismelor de calificare a lead-urilor.

### **G. Administrare și monitorizare**

Administratorii vor beneficia de un dashboard pentru gestionarea utilizatorilor, verificarea cererilor, soluționarea reclamațiilor și monitorizarea lead-urilor distribuite.

Vor fi disponibile statistici privind numărul solicitărilor, lead-urile acceptate, sumele încasate sau simulate și frecvența sesizărilor.

Pentru analiza comportamentului utilizatorilor vom integra PostHog, o platformă de product analytics. Vom instrumenta evenimente relevante pentru parcursul clientului și al profesionistului, precum începerea și finalizarea înregistrării, crearea unei solicitări, acceptarea unui lead, inițierea conversației și trimiterea unei sesizări. Prin analiza funnel-urilor și a punctelor în care utilizatorii abandonează fluxurile, vom putea identifica dificultățile de utilizare și prioritiza îmbunătățirile în iterațiile următoare. Colectarea datelor va respecta cerințele GDPR și va evita trimiterea datelor de contact sau a conținutului conversațiilor în instrumentul de analiză.

## **1.4. Cerințe nefuncționale și calitate software**

Pe lângă dezvoltarea funcționalităților, proiectul va urmări principii de inginerie software privind mentenabilitatea, securitatea și testabilitatea.

Vor fi avute în vedere validarea datelor introduse, protecția informațiilor personale, autentificarea sigură, autorizarea la nivelul fiecărei resurse, tratarea concurenței în tranzacțiile cu credite și jurnalizarea acțiunilor critice.

Datele de contact ale clientului nu vor fi dezvăluite înainte de acceptarea lead-ului, iar conversațiile vor putea fi accesate numai de utilizatorii implicați.

Vor fi documentate regulile privind consimțământul, retenția datelor și drepturile utilizatorilor, în concordanță cu GDPR.

Pentru componenta AI, vor fi urmărite suplimentar minimizarea datelor transmise modelului, validarea rezultatelor generate și prevenirea publicării automate a unor informații incorecte.

Dezvoltarea va utiliza un repository comun, code review, integrare continuă și teste automate.

---

# **2\. Direcții avansate de dezvoltare**

După realizarea și validarea versiunii web end-to-end, proiectul poate continua prin trei direcții majore.

## **2.1. Extinderea ecosistemului către aplicații mobile**

Prima direcție este realizarea unor aplicații mobile pentru Android și iOS, folosind **React Native și Expo**.

Aplicațiile vor reutiliza API-urile implementate în Spring Boot, iar infrastructura backend va rămâne comună.

Funcționalitățile specifice vor include notificări push pentru lead-uri noi, acces rapid la conversații, gestionarea profilului și urmărirea statusului solicitărilor.

Pentru profesioniști, accesul mobil poate fi deosebit de important, deoarece permite reacția imediată la apariția unor oportunități comerciale.

Într-o etapă ulterioară, aplicațiile ar putea integra și funcționalități native precum capturarea fotografiilor, încărcarea documentelor și utilizarea interacțiunilor vocale cu asistentul AI.

## **2.2. Dezvoltarea sistemului inteligent de calificare și matching**

A doua direcție, cu cel mai important potențial tehnologic și de cercetare, constă în dezvoltarea unui sistem AI care să îmbunătățească întregul proces de identificare, calificare și distribuire a solicitărilor.

Această direcție va integra trei componente complementare:

**AI Project Architect:** un asistent conversațional care ajută clientul să își formuleze problema și să identifice serviciile necesare.

**Intelligent Lead Qualification:** un mecanism care evaluează completitudinea și coerența solicitărilor înainte de distribuirea acestora.

**Explainable Semantic Matching:** un sistem de recomandare care combină criteriile tradiționale de filtrare cu analiza semantică a proiectelor și profilurilor profesioniștilor.

În prima versiune, se urmărește realizarea unui prototip funcțional al asistentului conversațional, capabil să transforme descrieri în limbaj natural în solicitări structurate.

Dezvoltarea completă a calificării inteligente și a matching-ului semantic este tratată ca o extensie ulterioară.

Arhitectura, avantajul competitiv și metodologia de evaluare a acestor componente sunt prezentate detaliat în capitolul 5\.

## **2.3. Comunicare avansată și plăți integrate**

A treia direcție urmărește transformarea platformei dintr-un intermediar de lead-uri într-un mediu complet pentru desfășurarea colaborării.

**Apeluri audio și video.** Integrarea tehnologiilor WebRTC, eventual printr-o soluție precum LiveKit, va permite clienților și profesioniștilor să organizeze întâlniri direct din platformă, fără aplicații externe.

**Plăți online.** Integrarea unui procesator precum Stripe va permite achiziționarea creditelor pentru lead-uri și, într-o etapă distinctă, efectuarea plăților pentru serviciile contractate.

Pentru plățile între clienți și profesioniști poate fi evaluat Stripe Connect, care permite administrarea unor conturi conectate și colectarea comisioanelor platformei.

**Managementul colaborării.** Într-o versiune ulterioară, proiectele ar putea include oferte comerciale, acceptarea propunerilor, termene-limită, livrabile și recenzii verificate.

Aceste extensii vor necesita și analiza responsabilităților legale, fiscale și contractuale asociate procesării plăților.

---

# **3\. Analiza concurenței și potențialul pieței din România**

## **3.1. Contextul pieței**

Piața românească dispune deja de platforme care intermediază cererea și oferta de servicii, ceea ce confirmă existența unor modele comerciale funcționale pentru această categorie de produse.

Cu toate acestea, platformele existente diferă în modul de monetizare, categoriile de servicii acoperite și nivelul de implicare în relația dintre client și profesionist.

Concurenții relevanți identificați sunt:

| Platformă | Model și caracteristici |
| ----- | ----- |
| **Necesit.ro** | Marketplace multidomeniu bazat pe distribuirea contra cost a lead-urilor către profesioniști |
| **HomeRun** | Platformă multidomeniu în care profesioniștii pot plăti pentru contactarea clienților și transmiterea ofertelor |
| **StarOfService** | Platformă internațională prezentă în România, cu profiluri profesionale, solicitări, abonamente și ofertare |
| **Daibau.ro** | Platformă specializată în construcții și amenajări, care conectează clienți cu executanți |
| **CatDam.ro** | Platformă de generare a lead-urilor pentru mai multe categorii de servicii, cu tarifare per lead |

Analiza este calitativă și nu reprezintă o estimare a cotelor de piață ale acestor operatori.

## **3.2. Studiu de caz: Necesit.ro**

Necesit.ro reprezintă unul dintre cele mai relevante exemple pentru proiectul propus, întrucât utilizează un model economic foarte apropiat de cel pe care dorim să îl implementăm.

Clientul descrie serviciul dorit și trimite gratuit o solicitare. Platforma verifică cererea și o distribuie unui număr limitat de profesioniști compatibili.

Conform informațiilor publicate de operator, platforma declară aproximativ 10.000 de clienți noi lunar. Costul unei solicitări este, în general, între 10 și 60 de lei, iar o cerere poate fi distribuită către maximum șase profesioniști.

Profesioniștii plătesc pentru lead-urile primite, nu pentru contractele finalizate. Astfel, venitul platformei nu depinde direct de valoarea proiectului contractat.

**Exemplu ilustrativ al modelului economic:**

Dacă o solicitare este distribuită către patru profesioniști, fiecare plătind 30 de lei, venitul brut generat de acel lead este de 120 de lei.

Acest exemplu demonstrează posibilitatea monetizării unei singure cereri prin intermediul mai multor participanți, dar și necesitatea de a limita distribuirea pentru a păstra valoarea comercială a oportunității.

Totodată, evidențiază importanța calității solicitărilor: distribuirea unei cereri insuficient definite poate genera venit pentru platformă, dar poate afecta încrederea profesioniștilor și disponibilitatea acestora de a cumpăra lead-uri viitoare.

## **3.3. Oportunități de diferențiere**

Existența concurenței validează interesul pentru serviciile de intermediere, dar nu garantează succesul unei platforme noi.

Principala provocare constă în atragerea simultană a cererii și ofertei, precum și în menținerea încrederii profesioniștilor în calitatea lead-urilor cumpărate.

Platformele existente utilizează deja diferite mecanisme de verificare a solicitărilor, filtrare a profesioniștilor și gestionare a ofertelor.

Prin urmare, diferențierea propusă nu se bazează pe presupunerea că aceste funcționalități lipsesc în totalitate din produsele concurente, ci pe integrarea lor într-un proces inteligent, transparent și orientat către calitatea informațiilor.

Proiectul nostru propune diferențiere prin trei direcții:

**Calitatea și transparența lead-urilor.** Solicitările vor fi validate, clasificate și însoțite de informații suficiente pentru ca profesionistul să poată lua o decizie informată înainte de achiziție. Sesizările și rambursările vor urma reguli explicite.

**O experiență completă de colaborare.** Spre deosebire de o abordare orientată exclusiv către distribuirea datelor de contact, platforma va permite comunicarea și urmărirea interacțiunilor într-un mediu comun.

**Automatizarea inteligentă.** Integrarea unui LLM poate reduce dificultatea formulării unei solicitări, poate evidenția informațiile lipsă și poate îmbunătăți precizia clasificării serviciilor.

Oportunitatea principală constă în trecerea de la un model centrat exclusiv pe **cantitatea lead-urilor distribuite** la unul care acordă o importanță ridicată **calității, relevanței și transparenței acestora**.

Din perspectivă comercială, o lansare reală ar putea începe cu un număr restrâns de categorii și regiuni, urmând extinderea după validarea calității lead-urilor și a ratei de conversie.

---

# **4\. State of the Art – Sisteme conversaționale și recomandări explicabile**

## **4.1. Obiectivul analizei literaturii**

Pentru fundamentarea componentei inteligente a marketplace-ului au fost selectate trei lucrări reprezentative: Friedman et al. (2023), Zhang și Chen (2020) și Kim și Cho (2026). Ele tratează, din perspective complementare, recomandarea conversațională, explicarea recomandărilor și integrarea modelelor lingvistice de mari dimensiuni (LLM) în sisteme de recomandare. Analiza urmărește problema investigată, metoda propusă, tipul dovezilor prezentate, limitele și aplicabilitatea pentru platforma noastră.

Cele trei lucrări nu studiază direct piața românească a lead-urilor sau contractarea profesioniștilor. Prin urmare, relevanța lor pentru calificarea solicitărilor și matching-ul prestatorilor constituie o extrapolare tehnică ce va trebui validată experimental în contextul proiectului.

## **4.2. Friedman et al. (2023): Leveraging Large Language Models in Conversational Recommender Systems**

Friedman et al. \[7\] investighează utilizarea LLM-urilor într-un sistem de recomandare conversațională (Conversational Recommender System – CRS). Problema de pornire este că recomandările bazate numai pe istoricul utilizatorului nu reflectă întotdeauna preferințele și intențiile exprimate într-o anumită sesiune. Autorii urmăresc utilizarea dialogului pentru a interpreta cerințe, a cere clarificări și a adapta recomandările pe măsură ce interacțiunea evoluează.

Lucrarea propune o arhitectură integrată care separă gestionarea conversației, reprezentarea preferințelor, recuperarea candidaților și generarea recomandărilor explicabile. Sunt descrise profiluri ale utilizatorilor în limbaj natural și tehnici de simulare a conversațiilor pentru situațiile în care datele reale de antrenare sunt limitate. Ca demonstrație de fezabilitate, autorii prezintă RecLLM, un prototip de recomandare a videoclipurilor YouTube construit cu LaMDA.

Contribuția principală este arhitecturală: un LLM poate gestiona dialogul fără a înlocui neapărat mecanismul de recuperare a elementelor dintr-un catalog real. Totuși, dovezile prezentate constau în principal în exemple ilustrative de conversații; lucrarea nu stabilește printr-un experiment cantitativ cuprinzător superioritatea RecLLM față de toate abordările clasice.

Aplicabilitate: modulul nostru AI Project Architect poate folosi conversația pentru identificarea serviciului necesar și clarificarea bugetului, termenului și cerințelor. Spre deosebire de recomandarea videoclipurilor, rezultatul dialogului va fi o solicitare structurată, confirmată de client, care poate fi transmisă algoritmului de matching. Separarea între conversație și baza de date a profesioniștilor limitează riscul de recomandare a unor prestatori inexistenți.

## **4.3. Zhang și Chen (2020): Explainable Recommendation: A Survey and New Perspectives**

Zhang și Chen \[8\] analizează literatura despre sistemele de recomandare explicabile. Problema investigată este lipsa transparenței: un algoritm poate furniza recomandări relevante, dar utilizatorul sau administratorul nu înțelege de ce au fost selectate anumite rezultate. Lucrarea este un studiu de sinteză (survey), nu o prezentare a unui unic sistem nou.

Autorii dezvoltă o perspectivă asupra explicațiilor de tip «de ce?» în recomandare și o taxonomie bidimensională: sursa informațiilor folosite în explicație și mecanismul algoritmic prin care acestea sunt produse. Sunt discutate explicațiile provenite direct din modele interpretabile și explicațiile post-hoc, precum și diferite metode bazate pe caracteristici, relații între utilizatori sau elemente și reprezentări textuale.

Studiul subliniază rolul explicațiilor pentru transparență, încredere, satisfacția utilizatorului și depanarea sistemului, dar nu identifică o metodă universal optimă. O observație importantă pentru proiect este distincția dintre o explicație convingătoare și una fidelă deciziei reale a algoritmului. O justificare generată fluent de un LLM poate fi greșită dacă nu este ancorată în criteriile efectiv folosite la ranking.

Aplicabilitate: în Explainable Semantic Matching, recomandarea unui profesionist va fi însoțită de motive verificabile, precum compatibilitatea categoriei, disponibilitatea, experiența relevantă și bugetul acceptat. Explicația trebuie să reflecte filtrele și factorii reali ai scorului, nu să fie construită independent de modelul de matching.

## **4.4. Kim și Cho (2026): Conversational Recommender Systems with Large Language Models**

Kim și Cho \[9\] oferă o revizuire recentă a cercetărilor despre sisteme conversaționale de recomandare bazate pe LLM. Pe baza clasificării prezentate în rezumatul și secțiunile public accesibile ale articolului, autorii identifică trei paradigme: (1) LLM as Conversational Recommender, în care modelul este motorul central de recomandare; (2) LLM-Augmented CRS, în care modelul îmbunătățește componentele unui sistem existent; și (3) LLM-Assisted CRS Evaluation, în care LLM-ul contribuie la simularea utilizatorilor și evaluarea sistemului.

Lucrarea evidențiază că LLM-ul nu trebuie să controleze întregul proces de recomandare. El poate fi utilizat pentru extragerea preferințelor, orchestrarea dialogului, augmentarea datelor sau generarea de scenarii de test. Autorii discută și provocări de implementare, precum costul inferenței, latența, scalabilitatea, încrederea în răspunsuri și confidențialitatea informațiilor.

Fiind un survey, articolul organizează și compară direcții de cercetare; nu reprezintă o dovadă experimentală că toate sistemele cu LLM depășesc sistemele tradiționale. În plus, analiza noastră se bazează pe conținutul public disponibil, nu pe verificarea integrală a tuturor studiilor discutate de autori.

Aplicabilitate: paradigma LLM-Augmented CRS este cea mai potrivită pentru marketplace-ul propus. LLM-ul poate clarifica nevoile și extrage cerințele, în timp ce Spring Boot, PostgreSQL și regulile deterministe controlează eligibilitatea profesioniștilor, monetizarea lead-urilor și protecția datelor. Paradigma de evaluare asistată de LLM poate sprijini generarea scenariilor sintetice, cu validare umană obligatorie.

## **4.5. Studiu de caz practic – Zenyra: asistentă virtuală pentru matching terapeut–client**

O experiență practică relevantă pentru proiectul propus este Zenyra, o asistentă virtuală dezvoltată în contextul proiectului Mindtale, având rolul de a facilita asocierea dintre persoanele care caută un terapeut și profesioniștii care pot răspunde nevoilor lor. Exemplul este util deoarece ilustrează aplicarea interacțiunii conversaționale într-un proces real de conectare între un client și un prestator de servicii.

Fluxul Zenyra urmărește realizarea matching-ului terapeut–client pe baza unui dialog de până la 21 de replici schimbate între client și asistenta virtuală. Această limită descrie lungimea maximă a schimbului conversațional, nu existența a 21 de întrebări obligatorii. Interacțiunea îi permite utilizatorului să își exprime în limbaj natural nevoile și preferințele, care constituie baza procesului de asociere cu terapeuți potriviți.

Din perspectiva ingineriei software, cazul este relevant pentru trei decizii de proiectare: colectarea progresivă a informațiilor în locul completării unui formular rigid, limitarea explicită a dialogului pentru a menține experiența previzibilă și utilizarea informațiilor obținute din conversație pentru a sprijini o decizie de matching. Particularitățile exacte ale algoritmului intern de recomandare și ale tehnologiei modelului folosit de Zenyra nu sunt documentate aici și, în consecință, nu sunt atribuite sistemului metode precum embeddings, clasificare semantică sau ranking hibrid fără o verificare suplimentară.

În raport cu literatura analizată, Zenyra oferă un exemplu aplicativ apropiat de direcția discutată de Friedman et al. \[7\]: dialogul devine un mecanism de descoperire a cerințelor, nu doar o interfață pentru o căutare obișnuită. Perspectiva lui Kim și Cho \[9\] susține conceptual integrarea unei astfel de componente conversaționale într-o aplicație mai largă. Principiile explicabilității prezentate de Zhang și Chen \[8\] pot ghida o dezvoltare ulterioară, în care recomandările sunt însoțite de motive verificabile; nu presupunem că această funcționalitate este deja implementată în Zenyra.

Este importantă și delimitarea domeniului: asocierea terapeut–client implică informații potențial sensibile, iar asistentul nu trebuie confundat cu un instrument de diagnostic sau de înlocuire a judecății profesionale. Pentru proiectul nostru multidomeniu, preluăm principiul clarificării conversaționale și al recomandării controlate, nu regulile specifice domeniului terapeutic.

Ca studiu de caz personal, Zenyra documentează o experiență de implementare relevantă, dar nu constituie, în lipsa unor rezultate măsurate printr-un protocol comun, dovada că dialogul de până la 21 de replici oferă recomandări mai bune decât formularele tradiționale. Nu raportăm rate de conversie, acuratețe sau satisfacție fără date empirice. Tocmai această limitare motivează evaluarea comparativă propusă pentru noul marketplace: completitudinea cererilor, precizia clasificării, relevanța matching-ului și efortul necesar utilizatorului.

## **4.6. Comparație critică a celor trei lucrări**

Friedman et al. \[7\] oferă un model de arhitectură conversațională și un prototip ilustrativ; Zhang și Chen \[8\] oferă o taxonomie a explicațiilor și criterii pentru interpretarea recomandărilor; Kim și Cho \[9\] clarifică rolurile posibile ale LLM-urilor în sistemele moderne de recomandare. Cele trei lucrări sunt complementare, dar folosesc obiective și tipuri de dovezi diferite, astfel că nu este justificată o comparație numerică directă între rezultatele lor.

Limitele comune pentru aplicația noastră sunt lipsa unei evaluări specifice lead-urilor comerciale din România, absența unor date reale despre calitatea solicitărilor și necesitatea verificării practice a relației dintre conversație, completitudinea cererii și relevanța prestatorilor recomandați. Recomandarea de videoclipuri sau produse nu este echivalentă cu alocarea unei oportunități comerciale contra cost, unde o clasificare eronată poate produce un prejudiciu economic.

## **4.7. Lacuna identificată și direcția de cercetare a proiectului**

Literatura analizată susține separat conversația bazată pe LLM, explicațiile algoritmice și integrarea hibridă a modelelor lingvistice. Proiectul nostru urmărește să investigheze aplicarea lor împreună într-un flux de generare și comercializare a lead-urilor multidomeniu: conversație adaptivă → solicitare structurată → verificarea completitudinii → filtrare și ranking → explicația recomandării.

Întrebarea principală de cercetare este: «În ce măsură calificarea conversațională asistată de LLM îmbunătățește completitudinea solicitărilor și relevanța matching-ului față de colectarea prin formulare și matching-ul determinist?» Ipoteza este una de testat, nu un rezultat demonstrat de lucrările citate.

Pentru evaluare, propunem un set de scenarii adnotate de oameni, cu categorii, bugete și cerințe cunoscute. Vom compara completitudinea câmpurilor, corectitudinea clasificării, numărul de întrebări, erorile de extragere și relevanța profesioniștilor recomandați. Pentru explicații, vom verifica dacă fiecare motiv afișat este susținut de datele și factorii reali ai algoritmului. Datele sintetice pot ajuta la testare, dar nu vor fi tratate ca dovadă a performanței în producție.

Astfel, contribuția urmărită nu este inventarea unui nou LLM sau a unui algoritm universal de recomandare, ci proiectarea și evaluarea unui proces integrat de calificare conversațională, validare transparentă și matching explicabil pentru marketplace-uri de servicii. Soluția concretă este descrisă în capitolul 5\.

# **5\. Avantaj competitiv propus: Intelligent Lead Qualification & Explainable Matching**

## **5.1. Limitările abordărilor tradiționale**

Majoritatea marketplace-urilor de servicii utilizează un proces de colectare a solicitărilor prin intermediul unor formulare, în care clientul selectează o categorie și răspunde la întrebări predefinite.

Această metodă prezintă avantaje importante: este simplă, ușor de implementat, predictibilă și permite colectarea unor informații structurate.

Totuși, formularele tradiționale pot deveni restrictive în situațiile în care utilizatorul nu cunoaște exact serviciul necesar sau problema sa implică mai multe domenii de expertiză.

De exemplu, un utilizator care dorește să dezvolte o afacere online poate avea nevoie simultan de dezvoltare web, procesarea plăților, logistică și promovare digitală.

Dacă acesta selectează doar categoria „Creare site web”, cererea poate ajunge la profesioniști care nu oferă toate serviciile necesare.

În plus, simpla completare a unui formular nu garantează că solicitarea este suficient de clară sau că există o intenție reală de contractare.

Aceste limitări motivează investigarea unei abordări în care sistemul nu se limitează la colectarea pasivă a datelor, ci participă activ la clarificarea nevoilor clientului.

Direcția este susținută de cercetările privind sistemele de recomandare conversaționale, în care interacțiunea în limbaj natural permite identificarea progresivă a preferințelor și cerințelor utilizatorilor.

## **5.2. AI Project Architect – Identificarea inteligentă a nevoilor clientului**

Prima componentă propusă este un asistent conversațional bazat pe un Large Language Model (LLM), capabil să interacționeze cu utilizatorul pentru a identifica problema pe care acesta dorește să o rezolve.

În loc să solicite utilizatorului alegerea imediată a unei categorii, platforma va permite descrierea liberă a nevoilor în limbaj natural.

**Exemplu de interacțiune:**

Client: „Am o florărie și vreau să pot primi comenzi online, dar nu știu exact ce trebuie să fac.”

Asistent AI: „Dorești ca utilizatorii să poată plăti online sau doar să îți trimită solicitări pentru buchete?”

Client: „Vreau să poată plăti online și să aleagă livrarea la domiciliu.”

Asistent AI: „În acest caz, probabil ai nevoie de un magazin online, integrarea plăților și un sistem de gestionare a livrărilor. Ai un buget aproximativ și un termen în care ai dori să lansezi magazinul?”

Prin intermediul unui astfel de dialog, asistentul poate identifica cerințe pe care utilizatorul nu le-ar fi specificat inițial.

Ulterior, informațiile vor fi transformate într-o structură standardizată, care poate include categoria principală, subcategoriile relevante, obiectivul proiectului, funcționalitățile necesare, bugetul, localizarea și termenul estimat.

**Rezultatul urmărit este o solicitare structurată, nu doar o conversație.**

Asistentul va putea evidenția informațiile lipsă și va solicita confirmarea utilizatorului înainte de publicarea cererii.

Modelul nu va putea inventa un buget, o dată-limită sau o caracteristică a proiectului fără să marcheze informația ca neconfirmată.

Această abordare reprezintă o aplicare a conceptului de *Conversational Requirements Elicitation*, prin care cerințele sunt descoperite progresiv, în urma unui dialog.

## **5.3. Intelligent Lead Qualification – Evaluarea calității solicitărilor**

A doua componentă urmărește evaluarea calității unei solicitări înainte ca aceasta să fie distribuită profesioniștilor.

Într-un sistem tradițional, verificarea poate urmări existența datelor de contact, completarea unor câmpuri obligatorii și absența conținutului evident invalid.

Sistemul propus va adăuga o analiză a calității informațiilor furnizate.

Aceasta va lua în considerare:

* **Completitudinea:** existența informațiilor relevante pentru categoria de servicii.  
* **Coerența:** identificarea contradicțiilor între cerințe, buget și termenul dorit.  
* **Claritatea:** măsura în care obiectivul proiectului și rezultatul așteptat sunt descrise.  
* **Intenția declarată:** confirmarea de către client că urmărește obținerea unor oferte sau contractarea unui serviciu.  
* **Validitatea datelor:** verificarea separată a informațiilor de contact și identificarea solicitărilor duplicate.

Este important de menționat că un LLM nu poate confirma singur identitatea clientului sau garanta intenția reală de achiziție. Aceste aspecte necesită verificări explicite și mecanisme suplimentare de validare.

O posibilă extensie este introducerea unui sistem denumit **Lead Passport**.

Acesta reprezintă o fișă de evaluare a solicitării, disponibilă profesionistului înainte de achiziționarea lead-ului.

Fișa ar putea include:

| Indicator | Exemplu de informație |
| ----- | ----- |
| Categoria identificată | Dezvoltare e-commerce |
| Servicii necesare | Magazin online, plăți, livrări |
| Buget | Confirmat / Necunoscut |
| Termen | Confirmat / Necunoscut |
| Date de contact | Verificate / Neverificate |
| Completitudinea cererii | 85% |
| Clarificări restante | Modalitatea de gestionare a livrărilor |

Valoarea de 85% este un exemplu ilustrativ, nu rezultatul unei evaluări reale.

Gradul de completitudine va fi calculat pe baza unui set explicit de câmpuri necesare, ponderate în funcție de categoria proiectului.

Acesta nu va reprezenta o probabilitate de contractare și nu va garanta că solicitarea va deveni un contract.

Prin acest mecanism, profesionistul poate înțelege mai bine oportunitatea comercială înainte de a utiliza creditele disponibile.

Din perspectivă economică, sistemul urmărește diminuarea riscului perceput de profesioniști și creșterea transparenței procesului de comercializare a lead-urilor.

## **5.4. Explainable Semantic Matching – Asocierea inteligentă cu profesioniștii**

A treia componentă propusă constă în extinderea algoritmului tradițional de matching prin utilizarea analizei semantice.

În versiunea inițială, asocierea dintre solicitări și profesioniști se va baza pe criterii deterministe, precum categoria, localitatea, bugetul și disponibilitatea.

Această abordare este eficientă atunci când informațiile sunt bine structurate, dar poate avea limitări în identificarea relațiilor dintre competențe și cerințe exprimate diferit.

De exemplu, un profesionist poate avea în portofoliu „dezvoltarea magazinelor Shopify și integrarea procesatorilor de plăți”, în timp ce clientul solicită „un site prin care să vând produse și să primesc bani online”.

Deși formulările sunt diferite, există o compatibilitate semantică evidentă.

Pentru a identifica astfel de relații, propunem utilizarea unor modele de embeddings, care transformă descrierile textuale în reprezentări vectoriale.

Sistemul va putea compara semantic descrierea proiectului cu serviciile și experiența profesioniștilor.

Arhitectura de matching va combina două mecanisme:

**1\. Filtrare deterministă:** eliminarea profesioniștilor care nu îndeplinesc constrângerile obligatorii, precum categoria autorizată, zona geografică sau disponibilitatea.

**2\. Ranking semantic:** ordonarea profesioniștilor eligibili pe baza gradului de compatibilitate dintre cerințele proiectului și informațiile din profilurile lor.

Scorul rezultat va putea include atât similaritatea semantică, cât și factori suplimentari precum experiența, disponibilitatea și relevanța lucrărilor anterioare.

Un element esențial este explicabilitatea recomandărilor.

În loc să afișeze exclusiv un scor numeric, sistemul va putea comunica motivele principale ale asocierii.

De exemplu:

„Acest profesionist este recomandat deoarece oferă servicii de dezvoltare e-commerce, are experiență în integrarea plăților online și acceptă proiecte din categoria de buget selectată.”

Explicațiile vor fi generate folosind criteriile și datele care au contribuit efectiv la recomandare, nu afirmații inventate de modelul lingvistic.

Această abordare urmărește creșterea transparenței și a încrederii utilizatorilor în deciziile platformei.

## **5.5. Arhitectura tehnică a sistemului AI**

Integrarea AI va fi realizată astfel încât să nu afecteze independența funcționalităților principale ale marketplace-ului.

Propunem următorul flux de procesare:

**Etapa 1 – Conversație și colectarea cerințelor**

Utilizatorul descrie problema prin interfața Next.js. Mesajul este transmis backend-ului Spring Boot, care comunică printr-un adaptor dedicat cu un model LLM.

**Etapa 2 – Extragerea informațiilor structurate**

Modelul va genera un obiect JSON care conține informațiile identificate, precum categoria proiectului, serviciile necesare, bugetul și termenul estimat.

Răspunsul va fi verificat pe baza unei scheme definite, pentru a evita introducerea unor câmpuri invalide sau a unor categorii inexistente.

**Etapa 3 – Clarificare și confirmare**

În cazul în care lipsesc informații importante, asistentul va genera întrebări suplimentare.

Utilizatorul va putea revizui și modifica solicitarea înainte de publicare.

**Etapa 4 – Evaluarea calității**

Solicitarea va fi analizată pentru a determina gradul de completitudine și existența unor informații contradictorii.

Validarea contactelor și verificările de duplicat vor fi realizate prin mecanisme separate, care nu depind exclusiv de LLM.

**Etapa 5 – Matching și distribuire**

Solicitarea validată va fi transmisă modulului de matching.

În versiunea inițială vor fi utilizate reguli deterministe, urmând ca algoritmul să fie extins ulterior cu reprezentări vectoriale și ranking semantic.

**Etapa 6 – Feedback și îmbunătățire**

Datele privind acceptarea lead-urilor, rezultatele colaborărilor și sesizările vor putea fi agregate pentru evaluarea calității recomandărilor și îmbunătățirea regulilor de matching.

Din punct de vedere tehnologic, componentele principale vor fi:

| Componentă | Tehnologie propusă |
| ----- | ----- |
| Interfață conversațională | Next.js, React |
| Orchestrarea conversațiilor | Spring Boot |
| Model lingvistic | LLM accesat prin API |
| Structurarea răspunsurilor | JSON Schema și validare backend |
| Persistența solicitărilor | PostgreSQL |
| Reprezentări semantice | Model de embeddings |
| Căutare vectorială | PostgreSQL cu pgvector |
| Algoritm de recomandare | Matching hibrid: reguli \+ similaritate semantică |

Componenta AI trebuie să poată fi dezactivată fără a împiedica publicarea solicitărilor prin formularele tradiționale.

Această decizie permite compararea celor două abordări și asigură continuitatea funcționării platformei în cazul indisponibilității serviciului AI.

## **5.6. Evaluarea avantajului competitiv**

Pentru a demonstra utilitatea componentei AI, nu este suficientă prezentarea unui chatbot funcțional.

Propunem evaluarea experimentală a sistemului în raport cu o implementare de referință bazată pe formulare tradiționale și matching determinist.

Vom defini două abordări:

**Baseline:** utilizatorul completează un formular clasic, iar solicitarea este asociată profesioniștilor pe baza categoriei și a celorlalte criterii deterministe.

**AI-assisted:** utilizatorul descrie problema în limbaj natural, asistentul solicită clarificări și generează cererea structurată, care este transmisă algoritmului de matching.

În cazul dezvoltării ulterioare a componentei semantice, se va putea realiza și o comparație între matching-ul determinist și cel hibrid.

Evaluarea poate utiliza un set de scenarii reprezentative din mai multe categorii de servicii, pentru care sunt definite în prealabil cerințele corecte și profesioniștii eligibili.

Indicatorii propuși sunt:

| Indicator | Obiectivul evaluării |
| ----- | ----- |
| Acuratețea clasificării | Verificarea identificării corecte a categoriei |
| Completitudinea cererilor | Compararea proporției de informații necesare colectate |
| Numărul de clarificări | Evaluarea efortului conversațional |
| Relevanța recomandărilor | Compararea profesioniștilor identificați cu un set de referință |
| Erori de structurare | Identificarea informațiilor inventate sau extrase incorect |
| Feedback-ul utilizatorilor | Evaluarea clarității și utilității procesului |

Pentru prototipul semestrial, evaluarea se poate realiza pe un set redus de scenarii de test, adnotate manual.

Ulterior, pe baza unor date reale și cu respectarea cerințelor privind protecția datelor, se pot analiza și rata de conversie a lead-urilor, frecvența sesizărilor și costul de achiziție al clienților.

**Ipoteza principală a proiectului** este că introducerea unui proces conversațional de identificare și clarificare a nevoilor poate îmbunătăți completitudinea solicitărilor și relevanța asocierii cu profesioniștii, în comparație cu utilizarea exclusivă a formularelor tradiționale.

Această ipoteză va fi verificată experimental, fără a presupune anticipat că soluția AI va obține rezultate superioare în toate situațiile.

## **5.7. Contribuția propusă și potențialul de cercetare**

Utilizarea LLM-urilor în sisteme conversaționale și recomandări personalizate reprezintă deja un domeniu activ de cercetare.

Contribuția proiectului nu constă în dezvoltarea unui nou model lingvistic, ci în proiectarea și evaluarea unui mecanism integrat de identificare a nevoilor, calificare a lead-urilor și matching explicabil într-un marketplace multidomeniu.

Elementul de interes îl reprezintă relația dintre calitatea informațiilor colectate de la client și calitatea recomandărilor generate de platformă.

Spre deosebire de un chatbot generalist, asistentul propus va funcționa într-un proces bine definit, orientat către producerea unei solicitări structurate, validabile și utilizabile de sistemul de matching.

Astfel, direcția AI poate constitui atât un avantaj competitiv potențial pentru produs, cât și un subiect de cercetare aplicată.

---

# **6\. Metodologia de dezvoltare și planul de iterații**

## **6.1. Abordarea metodologică**

Dezvoltarea va urma un model **Agile, incremental și iterativ**, organizat în șase iterații de aproximativ două săptămâni.

Fiecare iterație va produce funcționalități verificabile și va include proiectare, implementare, testare și integrare.

Vom urmări principiul livrării verticale: o funcționalitate nu va fi considerată finalizată doar după implementarea interfeței sau a backend-ului, ci după integrarea completă dintre interfață, logica de business și persistență.

Intervalul calendaristic cuprinde săptămânile 3–14, cu activități de pregătire în săptămâna 3 și 11 săptămâni ulterioare pentru implementare și validare.

Dezvoltarea marketplace-ului de bază va avea prioritate, deoarece funcționalitățile AI depind de existența unui model coerent al solicitărilor, serviciilor și profesioniștilor.

## **6.2. Planificarea iterațiilor**

| Iterație | Săptămâni | Obiective și livrabile |
| ----- | ----- | ----- |
| **I. Analiză și fundamentare** | 3–4 | Analiza cerințelor, user stories, cazuri de utilizare, arhitectură, model de date, repository, CI/CD, schelet Spring Boot și Next.js |
| **II. Utilizatori și solicitări** | 5–6 | Autentificare, roluri, profiluri profesionale, categorii de servicii, formulare de publicare și validarea datelor |
| **III. Matching și monetizare** | 7–8 | Matching determinist, distribuirea lead-urilor, acceptarea solicitărilor, sistemul de credite și istoricul tranzacțiilor |
| **IV. Comunicare și sesizări** | 9–10 | Chat WebSocket, persistența mesajelor, sesizări, dashboard de administrare și rambursarea creditelor |
| **V. Integrare AI și stabilizare** | 11–12 | Prototip de asistent LLM, extragerea cerințelor în JSON, clarificarea solicitărilor, teste de integrare și corectarea problemelor |
| **VI. Validare și livrare** | 13–14 | Testare finală, evaluare comparativă AI versus formular, optimizări, deployment, documentație și demonstrarea produsului |

## **6.3. Delimitarea funcționalităților obligatorii și opționale**

Pentru a evita extinderea necontrolată a complexității proiectului, funcționalitățile vor fi împărțite în trei niveluri.

**Nivelul 1 – MVP obligatoriu**

Include autentificarea, gestionarea profilurilor, publicarea solicitărilor, matching-ul determinist, achiziționarea lead-urilor prin credite virtuale, chat-ul, sesizările și administrarea.

**Nivelul 2 – Obiectiv AI pentru semestrul curent**

Include dezvoltarea unui prototip conversațional care poate clarifica cerințele utilizatorului și genera o solicitare structurată, pentru un număr restrâns de categorii de servicii.

Se urmărește și realizarea unei evaluări comparative preliminare față de introducerea tradițională a solicitărilor.

**Nivelul 3 – Extensii ulterioare**

Include calificarea inteligentă avansată, matching-ul semantic pe baza embeddings, aplicațiile mobile, apelurile audio-video, plățile reale și învățarea din rezultatele colaborărilor.

Această delimitare permite menținerea unui obiectiv realizabil pentru semestrul curent, fără renunțarea la potențialul de cercetare și extindere al proiectului.

## **6.4. Rezultatul urmărit la finalul semestrului**

La finalul săptămânii 14, obiectivul este existența unui MVP web accesibil și funcțional, capabil să susțină următorul scenariu:

1. Un client își creează un cont și publică o solicitare pentru dezvoltarea unui site.  
2. Opțional, clientul utilizează asistentul AI pentru a-și clarifica nevoile și pentru a genera o cerere structurată.  
3. Sistemul validează cererea și identifică profesioniști compatibili.  
4. Profesioniștii primesc oportunitatea și pot decide să achiziționeze lead-ul.  
5. Soldul profesionistului este actualizat, iar acesta poate iniția comunicarea cu clientul.  
6. Clientul și profesionistul discută prin chat și pot marca rezultatul interacțiunii.  
7. În cazul unei probleme, profesionistul trimite o sesizare, iar administratorul poate aproba restituirea creditelor.

Scenariul va fi demonstrat utilizând date de test și credite virtuale.

Nu este necesară lansarea comercială efectivă pentru validarea funcționalităților proiectului.

Prototipul AI va avea un domeniu de aplicare limitat și nu va condiționa funcționarea fluxului clasic de publicare a solicitărilor.

## **6.5. Criterii de evaluare**

Succesul proiectului va fi evaluat pe baza unor rezultate verificabile:

**Corectitudinea funcțională:** scenariul end-to-end poate fi executat fără intervenții manuale în baza de date.

**Integritatea tranzacțiilor:** o achiziție nu produce debitări duplicate, iar rambursările păstrează un istoric auditabil.

**Calitatea matching-ului:** pe un set de solicitări și profesioniști de test, algoritmul respectă constrângerile de categorie și localitate.

**Securitatea:** accesul neautorizat la conversații, date de contact și operațiuni administrative este blocat.

**Calitatea componentei AI:** asistentul poate extrage informațiile relevante și produce solicitări structurate conforme cu schema definită, fără a inventa valori pentru câmpurile necunoscute.

**Testabilitatea și mentenabilitatea:** modulele principale beneficiază de teste automate și documentație tehnică.

**Livrarea:** aplicația poate fi pornită într-un mediu reproductibil și prezentată printr-un deployment funcțional.

Analiza utilizării produsului: evenimentele instrumentate în PostHog permit urmărirea etapelor principale ale utilizatorilor, identificarea abandonului în fluxurile importante și fundamentarea deciziilor de îmbunătățire a aplicației.

Evaluarea sistemului AI va fi realizată separat de verificarea funcționalităților de bază ale marketplace-ului.

---

# **7\. Riscuri și provocări**

Principalele riscuri tehnice sunt complexitatea algoritmului de distribuire, consistența soldurilor, gestionarea concurenței și sincronizarea comunicațiilor în timp real.

Pentru a limita aceste riscuri, vom utiliza inițial un algoritm determinist și o arhitectură monolitică modulară, cu tranzacții controlate la nivelul bazei de date.

Un alt risc îl reprezintă extinderea excesivă a domeniului proiectului.

Din acest motiv, aplicațiile mobile, matching-ul semantic avansat și plățile pentru proiectele contractate vor fi considerate extensii ulterioare, nu cerințe obligatorii ale MVP-ului.

Integrarea inteligenței artificiale introduce provocări suplimentare, precum:

* **Generarea informațiilor incorecte:** LLM-ul poate interpreta greșit solicitarea sau poate produce detalii care nu au fost furnizate de utilizator.  
* **Clasificarea eronată:** sistemul poate identifica o categorie nepotrivită sau poate omite un serviciu important.  
* **Costurile de inferență:** utilizarea unui model comercial poate introduce costuri operaționale variabile.  
* **Confidențialitatea datelor:** conversațiile pot conține informații personale sau comerciale care necesită protecție.  
* **Bias-ul algoritmic:** recomandările pot favoriza anumiți profesioniști fără justificare relevantă.  
* **Dependența de furnizori externi:** indisponibilitatea unui API AI poate afecta funcționalitatea asistentului.

Pentru reducerea acestor riscuri, vom utiliza validarea strictă a rezultatelor generate, confirmarea explicită a cererilor de către clienți și separarea componentei AI de fluxurile critice de business.

Deciziile comerciale, precum debitarea creditelor sau rambursarea acestora, nu vor fi realizate autonom de modelul lingvistic.

Din perspectiva produsului, riscurile relevante includ solicitările false, conflictele privind calitatea lead-urilor și necesitatea obținerii unei mase critice de clienți și profesioniști.

Aceste probleme vor fi abordate prin validarea solicitărilor, reguli de eligibilitate, mecanisme de sesizare și colectarea unor indicatori de calitate.

---

# **8\. Concluzii**

Proiectul propus reprezintă o oportunitate de aplicare practică a conceptelor fundamentale de inginerie software într-un sistem cu potențial comercial real.

Prin utilizarea Spring Boot și Next.js, echipa va dezvolta o platformă web completă, abordând autentificarea, modelarea datelor, algoritmii de matching, procesarea tranzacțiilor, comunicarea în timp real și administrarea disputelor.

Structurarea incrementală permite obținerea unui produs utilizabil în intervalul disponibil, păstrând totodată posibilitatea extinderii către aplicații mobile, asistenți AI și infrastructuri tranzacționale avansate.

**Principalul element de diferențiere propus este dezvoltarea unui mecanism inteligent de identificare, calificare și distribuire a solicitărilor, care să îmbunătățească relația dintre clienți și profesioniști.**

Prin integrarea unui asistent conversațional, platforma poate ajuta utilizatorii să își identifice nevoile chiar și atunci când nu cunosc serviciile de care au nevoie.

Ulterior, calificarea inteligentă a lead-urilor și matching-ul semantic explicabil pot contribui la o distribuire mai relevantă a oportunităților comerciale și la creșterea transparenței pentru profesioniști.

Din perspectivă academică, proiectul permite evaluarea comparativă a abordărilor tradiționale și a celor asistate de inteligență artificială, oferind posibilitatea investigării unor ipoteze privind completitudinea solicitărilor, precizia clasificării și relevanța recomandărilor.

Obiectivul principal nu este doar realizarea unei aplicații asemănătoare celor existente, ci **construirea unei baze software solide, extensibile și verificabile, care să permită dezvoltarea și evaluarea unui marketplace inteligent orientat către calitatea lead-urilor**.

---

# **Referințe**

\[1\] Necesit.ro – Înscrierea profesioniștilor și modelul de tarifare.  
[https\://www\.necesit.ro/join-us](https://www.necesit.ro/join-us)

\[2\] Necesit.ro – Întrebări frecvente pentru profesioniști.  
[https\://www\.necesit.ro/faq-profesionisti](https://www.necesit.ro/faq-profesionisti)

\[3\] HomeRun – Termeni și condiții.  
[https\://homerun.ro/terms-of-service](https://homerun.ro/terms-of-service)

\[4\] StarOfService – Cum funcționează platforma pentru profesioniști.  
[https\://www\.starofservice.ro/pro/how-it-works](https://www.starofservice.ro/pro/how-it-works)

\[5\] Daibau – Platforma de servicii pentru construcții.  
[https\://www\.daibau.ro/](https://www.daibau.ro/)

\[6\] CatDam – Programul pentru parteneri și tarifarea lead-urilor.  
[https\://catdam.ro/parteneri](https://catdam.ro/parteneri)

\[7\] Friedman, L., et al. (2023). *Leveraging Large Language Models in Conversational Recommender Systems*. arXiv:2305.07961.  
[https\://arxiv.org/abs/2305.07961](https://arxiv.org/abs/2305.07961)

\[8\] Zhang, Y., & Chen, X. (2020). *Explainable Recommendation: A Survey and New Perspectives*. Foundations and Trends in Information Retrieval, 14(1), 1–101.  
[https\://doi.org/10.1561/1500000066](https://doi.org/10.1561/1500000066)

\[9\] Kim, Y.-H., & Cho, Y.-S. (2026). *Conversational recommender systems with large language models: A review of current developments*. Expert Systems with Applications.  
[https\://doi.org/10.1016/j.eswa.2026.132508](https://doi.org/10.1016/j.eswa.2026.132508)

\[10\] European Data Protection Board – Principiile fundamentale GDPR.  
[https\://www\.edpb.europa.eu/topics/key-gdpr-concepts/basic-principles\_en](https://www.edpb.europa.eu/topics/key-gdpr-concepts/basic-principles_en)

\[11\] Stripe – Documentația Stripe Connect.  
[https\://docs.stripe.com/connect](https://docs.stripe.com/connect)

\[12\] Spring – Documentația Spring Boot.  
[https\://docs.spring.io/spring-boot/reference/](https://docs.spring.io/spring-boot/reference/)

\[13\] Next.js – Documentația oficială.  
[https\://nextjs.org/docs](https://nextjs.org/docs)

\[14\] pgvector – Vector Similarity Search for PostgreSQL.  
[https\://github.com/pgvector/pgvector](https://github.com/pgvector/pgvector)

