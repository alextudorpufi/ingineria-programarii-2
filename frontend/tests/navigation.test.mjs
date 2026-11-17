import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';
import { runInNewContext } from 'node:vm';
import { test } from 'node:test';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import ts from 'typescript';
import * as policy from '../config/navigation.ts';

const require = createRequire(import.meta.url);
const expected = {
  client: ['leads', 'ai', 'chat', 'profile'],
  pro: ['leads', 'chat', 'disputes', 'analytics', 'profile'],
  admin: ['leads', 'disputes', 'analytics', 'profile'],
};
const tabs = ['leads', 'ai', 'chat', 'disputes', 'analytics', 'profile'];

// Render real TSX components with explicit boundary fixtures. No browser, network,
// auth storage or extra test framework is needed; production modules are unchanged.
function loadComponent(path, imports) {
  const filename = fileURLToPath(new URL(path, import.meta.url));
  const { outputText } = ts.transpileModule(readFileSync(filename, 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, jsx: ts.JsxEmit.ReactJSX },
    fileName: filename,
  });
  const cjsModule = { exports: {} };
  const resolveImport = name => {
    if (Object.hasOwn(imports, name)) return imports[name];
    if (name.startsWith('@/')) throw new Error(`Missing test boundary: ${name}`);
    return require(name);
  };
  runInNewContext('(function(require, module, exports) {\n' + outputText + '\n})', { console })(resolveImport, cjsModule, cjsModule.exports);
  return cjsModule.exports;
}

const marker = name => function ViewMarker() { return React.createElement('div', { 'data-view': name }); };
const button = ({ children, onClick, className }) => React.createElement('button', { onClick, className }, children);

for (const [role, permittedTabs] of Object.entries(expected)) {
  test(`${role}: exact order, unique items, safe default and denied tabs`, () => {
    const actual = policy.getNavigation(role).map(item => item.tab);
    assert.deepEqual(actual, permittedTabs);
    assert.equal(new Set(actual).size, actual.length);
    for (const tab of tabs) {
      assert.equal(policy.canAccessTab(role, tab), permittedTabs.includes(tab));
      assert.equal(policy.resolveWorkspaceTab(role, tab), permittedTabs.includes(tab) ? tab : 'leads');
    }
    assert.equal(policy.resolveWorkspaceTab(role, 'nonexistent'), 'leads');
  });
}

test('guest, missing, uppercase, inherited property and unknown roles fail closed', () => {
  for (const role of [null, undefined, '', 'guest', 'PRO', 'owner', 'toString', '__proto__', {}, 1]) {
    assert.equal(policy.isKnownRole(role), false);
    assert.deepEqual(policy.getNavigation(role), []);
    assert.equal(policy.resolveWorkspaceTab(role, 'leads'), null);
    assert.equal(policy.canAccessTab(role, 'profile'), false);
    assert.equal(policy.canUseHeaderAction(role, 'wallet'), false);
    assert.equal(policy.canUseHeaderAction(role, 'createRequest'), false);
  }
});

test('header actions are exclusive to the intended role', () => {
  for (const role of Object.keys(expected)) {
    assert.equal(policy.canUseHeaderAction(role, 'wallet'), role === 'pro');
    assert.equal(policy.canUseHeaderAction(role, 'createRequest'), role === 'client');
  }
});

test('role changes cannot preserve a forbidden active tab', () => {
  assert.equal(policy.resolveWorkspaceTab('client', 'disputes'), 'leads');
  assert.equal(policy.resolveWorkspaceTab('pro', 'ai'), 'leads');
  assert.equal(policy.resolveWorkspaceTab('admin', 'chat'), 'leads');
  assert.equal(policy.resolveWorkspaceTab(null, 'profile'), null);
});

function renderApp(role, activeTab, isLoading = false, trace = {}) {
  const imports = {
    '@/config/navigation': policy,
    '@/context/AuthContext': { useAuth: () => ({ user: role == null ? null : { id: 'test-user', role }, isLoading }) },
    '@/context/AppContext': { useApp: () => ({
      leads: [], activeLead: null, activeTab,
      setActiveLeadId: id => { trace.selectedLead = id; },
      setActiveTab: tab => { trace.requestedTab = tab; },
    }) },
  };
  for (const [path, name] of Object.entries({
    'views/Header': 'Header', 'views/WalletModal': 'WalletModal',
    'auth/AuthModal': 'AuthModal', 'views/LeadFeed': 'LeadFeed',
    'views/LeadDetail': 'LeadDetail', 'views/AIArchitect': 'AIArchitect',
    'views/ChatView': 'ChatView', 'views/DisputesView': 'DisputesView',
    'views/AnalyticsView': 'AnalyticsView', 'common/ProfileView': 'ProfileView',
    'common/LeadDetailSheet': 'LeadDetailSheet', 'ui/Skeleton': 'LoadingSpinner',
  })) imports[`@/components/${path}`] = { [name]: function ViewBoundary(props) {
    trace[name] = props;
    return React.createElement('div', { 'data-view': name });
  } };
  const { App } = loadComponent('../components/views/App.tsx', imports);
  return renderToStaticMarkup(React.createElement(App));
}

test('workspace render gate blocks forbidden content even when context is bypassed', () => {
  const views = { leads: 'LeadFeed', ai: 'AIArchitect', chat: 'ChatView', disputes: 'DisputesView', analytics: 'AnalyticsView', profile: 'ProfileView' };
  for (const [role, permittedTabs] of Object.entries(expected)) {
    for (const tab of tabs) {
      const html = renderApp(role, tab);
      const selected = permittedTabs.includes(tab) ? tab : 'leads';
      assert.ok(html.includes(`data-view="${views[selected]}"`), `${role}/${tab}`);
      for (const other of tabs.filter(candidate => candidate !== selected)) {
        assert.ok(!html.includes(`data-view="${views[other]}"`), `${role} must not render ${other}`);
      }
      assert.equal(html.includes('data-view="WalletModal"'), role === 'pro');
    }
  }
});

test('workspace renders no content during loading, logout or unknown roles', () => {
  assert.equal(renderApp('client', 'ai', true), '');
  for (const role of [null, undefined, 'owner', 'PRO']) assert.equal(renderApp(role, 'profile'), '');
});

test('indirect lead/chat callbacks cannot enter another role workspace', () => {
  const admin = {};
  renderApp('admin', 'leads', false, admin);
  admin.LeadDetail.onOpenChat('lead-1');
  assert.equal(admin.requestedTab, undefined);
  assert.equal(admin.selectedLead, undefined);

  const client = {};
  renderApp('client', 'leads', false, client);
  assert.equal(client.LeadDetail.onOpenDispute, undefined);
  client.LeadDetail.onOpenChat('lead-2');
  assert.equal(client.requestedTab, 'chat');
  assert.equal(client.selectedLead, 'lead-2');
  renderApp('client', 'chat', false, client);
  assert.equal(client.ChatView.onOpenDispute, undefined);

  const pro = {};
  renderApp('pro', 'leads', false, pro);
  pro.LeadDetail.onOpenDispute('lead-3');
  assert.equal(pro.requestedTab, 'disputes');
  assert.equal(pro.selectedLead, 'lead-3');
});

test('real header uses the same ordered items on desktop/mobile and hides forbidden actions', () => {
  for (const [role, permittedTabs] of Object.entries(expected)) {
    const { Header } = loadComponent('../components/views/Header.tsx', {
      '@/config/navigation': policy,
      '@/context/AuthContext': { useAuth: () => ({ user: { role, name: 'Test', creditsBalance: 15 }, isLoading: false }) },
      '@/context/AppContext': { useApp: () => ({ darkMode: false }) },
      '@/components/ui/Button': { Button: button },
    });
    const html = renderToStaticMarkup(React.createElement(Header, { currentTab: 'leads' }));
    assert.equal((html.match(/aria-current="true"/g) || []).length, 2);
    assert.ok(html.includes('aria-expanded="false"'));
    assert.ok(html.includes('aria-controls="workspace-mobile-menu"'));
    assert.equal(html.includes('Deschide portofelul'), role === 'pro');
    assert.equal(html.includes('Cerere Nouă'), role === 'client');
    const navs = [...html.matchAll(/<nav\b[^>]*>(.*?)<\/nav>/g)].map(match => match[1]);
    assert.equal(navs.length, 2);
    for (const nav of navs) {
      const labels = [...nav.matchAll(/<button\b[^>]*data-workspace-tab="[^"]+"[^>]*>(.*?)<\/button>/g)].map(match => match[1]);
      const expectedLabels = policy.getNavigation(role).map(item => item.label.replaceAll('&', '&amp;'));
      assert.deepEqual(labels, expectedLabels);
      assert.equal(labels.length, permittedTabs.length);
    }
  }
});

test('homepage preserves guest/loading boundaries and rejects unsupported roles', () => {
  for (const state of [
    { user: null, isLoading: false, view: 'LandingPage' },
    { user: null, isLoading: true, text: 'Se verifică sesiunea' },
    { user: { id: 'test-user', role: 'owner' }, isLoading: false, text: 'Acces indisponibil' },
    ...Object.keys(expected).map(role => ({ user: { id: 'test-user', role }, isLoading: false, view: 'App' })),
  ]) {
    const { default: Home } = loadComponent('../app/page.tsx', {
      '@/config/navigation': policy,
      '@/context/AuthContext': { AuthProvider: ({ children }) => children, useAuth: () => state },
      '@/context/AppContext': { AppProvider: ({ children }) => children },
      '@/components/views/App': { App: marker('App') },
      '@/components/landing/LandingPage': { LandingPage: marker('LandingPage') },
      '@/components/ui/Button': { Button: button },
    });
    const html = renderToStaticMarkup(React.createElement(Home));
    if (state.view) assert.ok(html.includes(`data-view="${state.view}"`));
    else assert.ok(html.includes(state.text));
    if (state.view !== 'App') assert.ok(!html.includes('data-view="App"'));
  }
});

test('wallet itself denies guests/admin/client and explains backend verification for professionals', () => {
  for (const role of [null, 'client', 'admin', 'owner', 'pro']) {
    const { WalletModal } = loadComponent('../components/views/WalletModal.tsx', {
      '@/config/navigation': policy,
      '@/context/AuthContext': { useAuth: () => ({ user: role ? { role, verifiedBadge: false } : null, isLoading: false }) },
      '@/context/AppContext': { useApp: () => ({ showWalletModal: true, proProfile: { creditsBalance: 0 }, transactions: [] }) },
      '@/components/ui/Button': { Button: button },
      '@/components/ui/Badge': { Badge: ({ children }) => React.createElement('span', null, children) },
      '@/services/api/marketplaceService': { marketplaceService: {} },
    });
    const html = renderToStaticMarkup(React.createElement(WalletModal));
    if (role === 'pro') assert.ok(html.includes('Eligibilitatea este verificată la inițierea plății'));
    else assert.equal(html, '');
  }
});
