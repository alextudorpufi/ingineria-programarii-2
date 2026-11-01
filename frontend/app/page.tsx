"use client";

import { AppProvider } from "@/context/AppContext";
import { App } from "@/components/views/App";

export default function Home() {
  return (
    <AppProvider>
      <App />
    </AppProvider>
  );
}
