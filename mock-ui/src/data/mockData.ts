import { ServiceCategory, Lead, ProfessionalProfile, DisputeRecord, WalletTransaction, ChatMessage } from '../types';

export const SERVICE_CATEGORIES: ServiceCategory[] = [
  {
    id: 'it-software',
    name: 'IT & Dezvoltare Software',
    iconName: 'Code',
    description: 'Magazine online, aplicații mobile, website-uri de prezentare și integrări API.',
    avgLeadPrice: 35,
    subcategories: ['E-commerce & Plăți Online', 'Website Prezentare', 'Aplicație Mobilă (iOS/Android)', 'Integrare API & Automatizare']
  },
  {
    id: 'constructii',
    name: 'Construcții & Amenajări',
    iconName: 'Hammer',
    description: 'Renovări integrale, finisaje interioare, termoizolații și proiectare arhitecturală.',
    avgLeadPrice: 30,
    subcategories: ['Renovare Apartament / Casă', 'Finisaje & Gips-carton', 'Instalații Sanitare', 'Design Interior']
  },
  {
    id: 'contabilitate',
    name: 'Contabilitate & Fiscalitate',
    iconName: 'Calculator',
    description: 'Evidență contabilă completă, declarații ANAF, audit financiar și salarizare.',
    avgLeadPrice: 25,
    subcategories: ['Contabilitate Lunar SRL', 'Înființare Firmă / Modificări', 'Consultanță Fiscală', 'Audit & Bilanț']
  },
  {
    id: 'marketing',
    name: 'Marketing & Design Digital',
    iconName: 'TrendingUp',
    description: 'Campanii Google Ads/Meta, identitate vizuală de brand, SEO și generare de lead-uri.',
    avgLeadPrice: 30,
    subcategories: ['Campanii Performance Ads', 'Identitate Vizuală & Logo', 'Optimizare SEO Tehnic', 'Social Media Management']
  },
  {
    id: 'instalatii',
    name: 'Instalații & Climatizare',
    iconName: 'Wrench',
    description: 'Centrale termice, aer condiționat, pompe de căldură și intervenții electrice autorizate.',
    avgLeadPrice: 20,
    subcategories: ['Montaj & Service Aer Condiționat', 'Centrale Termice & Gaze', 'Instalații Electrice Rezidențiale', 'Pompe de Căldură']
  }
];

export const INITIAL_PRO_PROFILE: ProfessionalProfile = {
  id: 'pro-teodor-01',
  name: 'Alexandru Munteanu',
  companyName: 'DevCraft Studio SRL',
  rating: 4.9,
  reviewCount: 38,
  categories: ['IT & Dezvoltare Software', 'Marketing & Design Digital'],
  servicesOffered: [
    'Magazine online WooCommerce / Shopify',
    'Integrare procesator plăți Netopia / Stripe',
    'Dezvoltare aplicații web Next.js / React',
    'Optimizare viteză și conversie magazin'
  ],
  coverageArea: ['București & Ilfov', 'Cluj-Napoca', 'Timișoara', 'Național (Remote)'],
  creditsBalance: 145,
  hourlyRate: 180,
  bio: 'Specialist în dezvoltare e-commerce și automatizări pentru IMM-uri românești. Peste 8 ani de experiență și mai mult de 60 de proiecte lansate cu succes.',
  verifiedBadge: true,
  responseTime: '< 15 minute',
  completedJobs: 42
};

export const INITIAL_LEADS: Lead[] = [
  {
    id: 'lead-101',
    title: 'Magazin online pentru florărie cu plăți cu cardul și gestiune livrări',
    category: 'IT & Dezvoltare Software',
    subcategory: 'E-commerce & Plăți Online',
    description: 'Am o florărie fizică în București (Sector 2) și doresc lansarea unui magazin online rapid. Clienții trebuie să poată comanda buchete, să plătească instant cu cardul și să aleagă intervalul de livrare la domiciliu.',
    location: 'București & Ilfov',
    budget: {
      min: 5000,
      max: 8500,
      currency: 'RON',
      isNegotiable: true,
      isConfirmed: true
    },
    timeline: '3 - 4 săptămâni (înainte de 1 Martie)',
    leadPrice: 30,
    maxPros: 4,
    claimedPros: [
      {
        proId: 'pro-other-02',
        proName: 'WebImpact Solutions',
        claimedAt: 'Acum 2 ore'
      }
    ],
    client: {
      id: 'client-881',
      name: 'Elena Dobreanu (Florăria Petale de Poveste)',
      phone: '+40 723 891 405',
      email: 'elena.dobreanu@florariapetale.ro',
      city: 'București, Sector 2',
      isVerified: true
    },
    createdAt: 'Astăzi, 09:20',
    status: 'validat',
    leadPassport: {
      completenessScore: 88,
      coherenceStatus: 'Excelentă',
      contactVerified: true,
      intentVerified: true,
      requiredDeliverables: [
        'Design responsive adaptat brandului floral',
        'Catalog produse cu categorii flori & ocazii',
        'Modul selectare dată și interval orar livrare',
        'Integrare procesator plăți online (Stripe / Netopia)',
        'Notificări SMS / Email confirmare comandă'
      ],
      clarificationsRemaining: [
        'Alegere curierat: flotă internă sau integrare curier extern'
      ],
      extractedByAI: true,
      duplicateRisk: 'Scăzut (< 2%)'
    },
    matchExplanation: {
      overallScore: 96,
      deterministicFit: true,
      reasons: [
        'Potrivire 100% categorie: Dezvoltare e-commerce & plăți online',
        'Zonă eligibilă acoperită direct: București & Ilfov',
        'Buget solicitat (5.000 - 8.500 RON) este în linia tarifelor tale medii',
        'Timp mediu de răspuns al tău (<15m) prioritizează distribuirea'
      ],
      semanticKeywords: ['magazin online', 'plăți cu cardul', 'livrare flori', 'Next.js / Shopify', 'București'],
      expectedResponseTime: '< 30 min'
    }
  },
  {
    id: 'lead-102',
    title: 'Renovare completă apartament 3 camere 74mp (bloc nou predare la roșu)',
    category: 'Construcții & Amenajări',
    subcategory: 'Renovare Apartament / Casă',
    description: 'Căutăm echipă serioasă pentru amenajare la cheie apartament 3 camere nou. Include glet, zugrăveli, montaj parchet triplustratificat, faianță baie mare și baie de serviciu, montaj uși interioare.',
    location: 'Cluj-Napoca (Zorilor)',
    budget: {
      min: 28000,
      max: 42000,
      currency: 'RON',
      isNegotiable: true,
      isConfirmed: true
    },
    timeline: 'Începere în maxim 2 săptămâni',
    leadPrice: 35,
    maxPros: 4,
    claimedPros: [],
    client: {
      id: 'client-882',
      name: 'Radu Vasilescu',
      phone: '+40 745 119 832',
      email: 'radu.vasilescu@outlook.com',
      city: 'Cluj-Napoca',
      isVerified: true
    },
    createdAt: 'Ieri, 16:45',
    status: 'validat',
    leadPassport: {
      completenessScore: 92,
      coherenceStatus: 'Excelentă',
      contactVerified: true,
      intentVerified: true,
      requiredDeliverables: [
        'Pregătire pereți, amorsă, glet grosier și de finisaj',
        'Montaj gresie/faianță rectificată format mare (60x120)',
        'Montaj parchet triplustratificat pe folie izolatoare',
        'Montaj 5 uși de interior și plinte MDF'
      ],
      clarificationsRemaining: [],
      extractedByAI: true,
      duplicateRisk: 'Scăzut (< 2%)'
    },
    matchExplanation: {
      overallScore: 84,
      deterministicFit: true,
      reasons: [
        'Potrivire arie de servicii: Renovări integrale locuințe',
        'Locație activă: Cluj-Napoca',
        'Proiect cu specificații clare și releveu disponibil'
      ],
      semanticKeywords: ['renovare la cheie', 'placări ceramice', 'finisaje interioare'],
      expectedResponseTime: '< 1 oră'
    }
  },
  {
    id: 'lead-103',
    title: 'Preluare contabilitate lunară SRL domeniu IT (export servicii UE/SUA)',
    category: 'Contabilitate & Fiscalitate',
    subcategory: 'Contabilitate Lunar SRL',
    description: 'Căutăm expert contabil autorizat CECCAR pentru preluare firmă IT înființată în 2023. Activitate de prestări servicii software cu facturare în Germania și SUA, plătitoare de TVA, 2 asociați.',
    location: 'Timișoara & Remote',
    budget: {
      min: 800,
      max: 1400,
      currency: 'RON/lună',
      isNegotiable: false,
      isConfirmed: true
    },
    timeline: 'Preluare de la 1 a lunii următoare',
    leadPrice: 25,
    maxPros: 3,
    claimedPros: [
      {
        proId: 'pro-other-03',
        proName: 'FinExpert Consulting',
        claimedAt: 'Acum 4 ore'
      }
    ],
    client: {
      id: 'client-883',
      name: 'Mihai Moldovan (ByteLogic SRL)',
      phone: '+40 751 400 918',
      email: 'office@bytelogic.ro',
      city: 'Timișoara',
      isVerified: true
    },
    createdAt: 'Astăzi, 11:05',
    status: 'validat',
    leadPassport: {
      completenessScore: 95,
      coherenceStatus: 'Excelentă',
      contactVerified: true,
      intentVerified: true,
      requiredDeliverables: [
        'Evidență contabilă completă în Saga / WinMentor',
        'Declarații lunare TVA (D300, D390 VIES)',
        'Întocmire și depunere bilanț contabil',
        'Consultanță optimizare fiscală dividende'
      ],
      clarificationsRemaining: [],
      extractedByAI: true,
      duplicateRisk: 'Scăzut (< 2%)'
    },
    matchExplanation: {
      overallScore: 90,
      deterministicFit: true,
      reasons: [
        'Cerință specifică: Facturare externă UE și neplătitor TVA SUA',
        'Contract de colaborare pe termen lung (abonament lunar)',
        'Firma are deja evidența contabilă la zi până luna trecută'
      ],
      semanticKeywords: ['expert contabil CECCAR', 'facturare intracomunitară', 'servicii IT'],
      expectedResponseTime: '< 2 ore'
    }
  },
  {
    id: 'lead-104',
    title: 'Campanii Google Ads & Meta pentru cabinet stomatologic nou',
    category: 'Marketing & Design Digital',
    subcategory: 'Campanii Performance Ads',
    description: 'Cabinet stomatologic modern deschis recent în Brașov. Avem nevoie de promovare locală pe Google Search (urgențe dentare, implantologie) și Meta Ads (Facebook/Instagram) pentru programări noi.',
    location: 'Brașov & Remote',
    budget: {
      min: 2500,
      max: 4000,
      currency: 'RON',
      isNegotiable: true,
      isConfirmed: true
    },
    timeline: 'Lansare campanii în 7 zile',
    leadPrice: 30,
    maxPros: 4,
    claimedPros: [],
    client: {
      id: 'client-884',
      name: 'Dr. Corina Stan',
      phone: '+40 762 901 223',
      email: 'dr.stan@dentallux.ro',
      city: 'Brașov',
      isVerified: true
    },
    createdAt: 'Ieri, 14:10',
    status: 'validat',
    leadPassport: {
      completenessScore: 82,
      coherenceStatus: 'Bună',
      contactVerified: true,
      intentVerified: true,
      requiredDeliverables: [
        'Configurare cont Google Ads & Google Tag Manager',
        'Setare conversii pentru apeluri telefonice și formular',
        'Creare bannere grafice Meta Ads pentru 3 servicii cheie',
        'Raportare lunară CPL (Cost per Lead)'
      ],
      clarificationsRemaining: ['Bugetul de media (buget direct către Google/Meta) este separat de onorariul de administrare'],
      extractedByAI: true,
      duplicateRisk: 'Scăzut (< 2%)'
    },
    matchExplanation: {
      overallScore: 91,
      deterministicFit: true,
      reasons: [
        'Potrivire expertiză: Performance Ads & Marketing medical',
        'Buget aliniat cu cerințele campaniilor locale de conversie'
      ],
      semanticKeywords: ['Google Ads', 'Meta Ads', 'lead generation clinică', 'conversii'],
      expectedResponseTime: '< 1 oră'
    }
  }
];

export const INITIAL_DISPUTES: DisputeRecord[] = [
  {
    id: 'disp-001',
    leadId: 'lead-999',
    leadTitle: 'Reparație centrală termică în condensație (Ariston)',
    proId: 'pro-teodor-01',
    proName: 'Alexandru Munteanu',
    reason: 'unreachable_contact',
    details: 'Am cumpărat lead-ul cu 25 credite, însă numărul de telefon indicat are căsuța vocală plină și nu este în funcțiune de 3 zile. La email nu s-a primit niciun răspuns.',
    creditsRequested: 25,
    createdAt: '2026-10-24 15:30',
    status: 'in_analiza'
  },
  {
    id: 'disp-002',
    leadId: 'lead-998',
    leadTitle: 'Creare aplicație Uber pentru livrări locale cu 400 RON',
    proId: 'pro-teodor-01',
    proName: 'Alexandru Munteanu',
    reason: 'incompatible_budget',
    details: 'Clientul solicită o aplicație complexă de tip Uber/Glovo cu hărți live și sistem curieri, dar susține că are buget maxim 400 RON și refuză să discute. Solicitarea conține neconcordanță majoră.',
    creditsRequested: 35,
    createdAt: '2026-10-23 10:15',
    status: 'aprobat',
    adminDecisionNote: 'Rambursare credit integrală aprobată. Criteriul de coerență buget/complexitate a fost încălcat de client.',
    resolvedAt: '2026-10-23 12:40'
  }
];

export const INITIAL_TRANSACTIONS: WalletTransaction[] = [
  {
    id: 'tx-101',
    type: 'top_up',
    amount: 150,
    description: 'Reîncărcare pachet Pro (150 Credite)',
    timestamp: '2026-10-20 14:00',
    balanceAfter: 200
  },
  {
    id: 'tx-102',
    type: 'lead_purchase',
    amount: -30,
    description: 'Achiziție lead #101: Magazin online florărie',
    timestamp: '2026-10-25 09:45',
    leadId: 'lead-101',
    balanceAfter: 170
  },
  {
    id: 'tx-103',
    type: 'refund',
    amount: 35,
    description: 'Rambursare sesizare aprobată #disp-002',
    timestamp: '2026-10-23 12:40',
    balanceAfter: 145
  }
];

export const INITIAL_CHAT_MESSAGES: Record<string, ChatMessage[]> = {
  'lead-101': [
    {
      id: 'm-1',
      leadId: 'lead-101',
      senderId: 'system',
      senderName: 'Sistem Skillink',
      senderRole: 'system',
      content: 'Lead deblocat cu succes. Datele de contact directe au fost verificate și transmise ambelor părți.',
      timestamp: '09:45'
    },
    {
      id: 'm-2',
      leadId: 'lead-101',
      senderId: 'pro-teodor-01',
      senderName: 'Alexandru Munteanu',
      senderRole: 'pro',
      content: 'Bună ziua, doamna Dobreanu! Am văzut solicitarea dvs. pentru magazinul online de florărie cu livrare programată. Am realizat proiecte similare cu integrare Stripe și modul de intervale orare.',
      timestamp: '09:46'
    },
    {
      id: 'm-3',
      leadId: 'lead-101',
      senderId: 'client-881',
      senderName: 'Elena Dobreanu',
      senderRole: 'client',
      content: 'Bună ziua, Alexandru! Ne bucurăm că ați răspuns atât de repede. Pentru noi este critic să putem limita numărul de buchete per interval orar de Valentine\'s Day și 1-8 Martie. Este posibil?',
      timestamp: '09:49'
    },
    {
      id: 'm-4',
      leadId: 'lead-101',
      senderId: 'pro-teodor-01',
      senderName: 'Alexandru Munteanu',
      senderRole: 'pro',
      content: 'Absolut! Putem seta capacități maxime per slot orar (ex. max 10 livrări între 09:00 - 11:00). Când doriți să stabilim un apel scurt de 15 minute pentru a parcurge câteva mostre de design?',
      timestamp: '09:51'
    }
  ]
};

export const AI_PROJECT_ARCHITECT_SAMPLE_DIALOGS = [
  {
    userPrompt: 'Am o florărie și vreau să primesc comenzi și plăți online, dar nu știu ce platformă e mai bună.',
    aiResponse: 'Excelent! O florărie are câteva cerințe particulare: selecția datei de livrare, interval orar și mesaje personalizate pe felicitare. Ai nevoie ca plata să fie doar online cu cardul sau și la livrare? Și curieratul va fi asigurat de șoferii tăi proprii sau printr-o firmă de curierat?'
  },
  {
    userPrompt: 'Vreau să renovez un apartament de 3 camere în bloc vechi și să schimb toată instalația electrică.',
    aiResponse: 'Un proiect important! Pentru ca meșterii și inginerii să vă ofere oferte precise: pereții necesită doar glet și vopsea sau și refacere tencuială? De asemenea, doriți tablou electric nou cu siguranțe diferențiale și circuit separat pentru electrocasnicele mari din bucătărie?'
  }
];
