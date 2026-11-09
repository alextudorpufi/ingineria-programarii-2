"use client";

import { AuthProvider, useAuth } from "@/context/AuthContext";
import { AppProvider } from "@/context/AppContext";
import { App } from "@/components/views/App";
import { LandingPage } from "@/components/landing/LandingPage";
import { isKnownRole } from "@/config/navigation";
import { Button } from "@/components/ui/Button";

function HomeContent() {
  const { user, isLoading, logout } = useAuth();

  if (isLoading) {
    return (
      <main className="flex min-h-dvh items-center justify-center p-6" role="status">
        <p className="text-sm text-slate-600 dark:text-slate-300">Se verifică sesiunea Skillink…</p>
      </main>
    );
  }

  if (!user) return <LandingPage />;

  if (!isKnownRole(user.role)) {
    return (
      <main className="flex min-h-dvh flex-col items-center justify-center gap-4 p-6 text-center">
        <h1 className="text-xl font-bold">Acces indisponibil</h1>
        <p className="text-sm text-slate-600 dark:text-slate-300">Rolul contului nu este acceptat. Reconectează-te sau contactează administratorul.</p>
        <Button onClick={() => void logout().catch(() => undefined)}>Deconectare</Button>
      </main>
    );
  }

  return <AppProvider key={`${user.id}:${user.role}`}><App /></AppProvider>;
}

export default function Home() {
  return (
    <AuthProvider>
      <HomeContent />
    </AuthProvider>
  );
}
