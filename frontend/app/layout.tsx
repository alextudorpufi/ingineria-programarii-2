import type { Metadata } from "next";
import { JetBrains_Mono, Plus_Jakarta_Sans } from "next/font/google";
import "./globals.css";

const plusJakartaSans = Plus_Jakarta_Sans({
  variable: "--font-plus-jakarta-sans",
  subsets: ["latin", "latin-ext"],
  display: "swap",
});

const jetBrainsMono = JetBrains_Mono({
  variable: "--font-jetbrains-mono",
  subsets: ["latin", "latin-ext"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "Skillink - Marketplace Inteligent de Servicii",
  description:
    "Skillink conectează clienții cu profesioniștii prin cereri de servicii. Publică gratuit o cerere sau descoperă oportunități pentru serviciile tale.",
  openGraph: {
    title: "Skillink - Marketplace Inteligent de Servicii",
    description:
      "Skillink conectează clienții cu profesioniștii prin cereri de servicii. Publică gratuit o cerere sau descoperă oportunități pentru serviciile tale.",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
  },
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html
      lang="ro"
      className={`${plusJakartaSans.variable} ${jetBrainsMono.variable} antialiased`}
      suppressHydrationWarning
    >
      <body className="min-h-screen bg-slate-50 font-sans text-slate-900 dark:bg-slate-950 dark:text-slate-100">
        {children}
      </body>
    </html>
  );
}
