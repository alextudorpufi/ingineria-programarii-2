"use client";

import React, { useEffect, useState } from 'react';
import { useApp } from '@/context/AppContext';
import { Coins, History, ArrowDownLeft, ArrowUpRight, X } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import type { CreditBundle } from '@/types';
import { marketplaceService } from '@/services/api/marketplaceService';

function formatBundlePrice(bundle: CreditBundle): string {
  return new Intl.NumberFormat('ro-RO', {
    style: 'currency',
    currency: bundle.currency,
    maximumFractionDigits: 2,
  }).format(bundle.amountMinor / 100);
}

export const WalletModal: React.FC = () => {
  const {
    showWalletModal,
    setShowWalletModal,
    proProfile,
    transactions,
    createCreditCheckout,
    getCreditPurchaseStatus,
    refreshWalletData,
  } = useApp();
  const [packages, setPackages] = useState<CreditBundle[]>([]);
  const [isLoadingPackages, setIsLoadingPackages] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [checkoutPurchaseId, setCheckoutPurchaseId] = useState<string | null>(null);

  useEffect(() => {
    const url = new URL(window.location.href);
    const checkoutResult = url.searchParams.get('walletCheckout');
    if (!checkoutResult) return;

    const timer = window.setTimeout(() => {
      setShowWalletModal(true);
      if (checkoutResult === 'cancelled') {
        setFeedback('Plata a fost anulată. Soldul tău nu a fost modificat.');
        url.searchParams.delete('walletCheckout');
        url.searchParams.delete('purchaseId');
        window.history.replaceState({}, '', `${url.pathname}${url.search}${url.hash}`);
        return;
      }

      const purchaseId = url.searchParams.get('purchaseId');
      if (checkoutResult === 'success' && purchaseId) {
        setCheckoutPurchaseId(purchaseId);
        setFeedback('Verificăm plata. Creditele apar după confirmarea Stripe.');
      } else {
        setFeedback('Nu am putut identifica achiziția pentru verificare.');
      }
    }, 0);
    return () => window.clearTimeout(timer);
  }, [setShowWalletModal]);

  useEffect(() => {
    if (!showWalletModal) return;

    let active = true;
    marketplaceService.getCreditBundles().then(bundles => {
      if (active) setPackages(bundles);
    }).catch(error => {
      if (active) {
        setFeedback(error instanceof Error ? error.message : 'Pachetele nu au putut fi încărcate.');
      }
    }).finally(() => {
      if (active) setIsLoadingPackages(false);
    });

    return () => {
      active = false;
    };
  }, [showWalletModal]);

  useEffect(() => {
    if (!checkoutPurchaseId) return;

    let active = true;
    let attempts = 0;
    let timer: ReturnType<typeof setTimeout>;
    const checkPurchase = async () => {
      try {
        const purchase = await getCreditPurchaseStatus(checkoutPurchaseId);
        if (!active) return;
        if (purchase.status === 'paid') {
          setFeedback(`Plata confirmată. Au fost adăugate ${purchase.credits} credite.`);
          const url = new URL(window.location.href);
          url.searchParams.delete('walletCheckout');
          url.searchParams.delete('purchaseId');
          window.history.replaceState({}, '', `${url.pathname}${url.search}${url.hash}`);
          setCheckoutPurchaseId(null);
          await refreshWalletData();
          return;
        }
        if (purchase.status === 'failed' || purchase.status === 'expired') {
          setFeedback('Plata nu a fost finalizată. Soldul tău nu a fost modificat.');
          const url = new URL(window.location.href);
          url.searchParams.delete('walletCheckout');
          url.searchParams.delete('purchaseId');
          window.history.replaceState({}, '', `${url.pathname}${url.search}${url.hash}`);
          setCheckoutPurchaseId(null);
          return;
        }
        attempts += 1;
        if (attempts < 30) {
          timer = setTimeout(() => void checkPurchase(), 2_000);
        } else {
          setFeedback('Plata este încă în curs de confirmare. Soldul se va actualiza după notificarea Stripe.');
          setCheckoutPurchaseId(null);
        }
      } catch (error) {
        if (active) {
          setFeedback(error instanceof Error ? error.message : 'Statusul plății nu a putut fi verificat.');
          setCheckoutPurchaseId(null);
        }
      }
    };

    void checkPurchase();
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [checkoutPurchaseId, getCreditPurchaseStatus, refreshWalletData]);

  const handleSelectPackage = async (bundle: CreditBundle) => {
    setIsProcessing(true);
    setFeedback(null);
    try {
      const checkoutUrl = await createCreditCheckout(bundle.id, crypto.randomUUID());
      window.location.assign(checkoutUrl);
    } catch (error) {
      setFeedback(error instanceof Error ? error.message : 'Checkout-ul Stripe nu a putut fi pornit.');
      setIsProcessing(false);
    }
  };

  if (!showWalletModal) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-3xl border border-indigo-100 dark:border-slate-800 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-6 border-b border-indigo-100 dark:border-slate-800 flex items-center justify-between bg-gradient-to-r from-amber-50/50 via-white to-indigo-50/30 dark:from-slate-900 dark:to-slate-900">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-amber-500 to-orange-500 text-white flex items-center justify-center shadow-xs">
              <Coins className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  Portofel Credite Profesionist
                </h3>
                <Badge variant="amber">1 Credit = 1 RON</Badge>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Sistem de debitare atomică per lead conform Section 1.3 D din proiect
              </p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="text-right bg-white dark:bg-slate-800 px-3.5 py-1.5 rounded-xl border border-amber-200 dark:border-amber-900 shadow-2xs">
              <span className="text-[10px] text-slate-400 uppercase font-semibold block">Sold disponibil:</span>
              <span className="text-lg font-mono font-extrabold text-amber-600 dark:text-amber-400 tabular-nums">
                {proProfile.creditsBalance} credite
              </span>
            </div>

            <button
              onClick={() => setShowWalletModal(false)}
              className="p-2 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Body Content */}
        <div className="p-6 overflow-y-auto space-y-6">
          {/* Top-Up Packages */}
          <div>
            <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider mb-3">
              Reîncărcare Credite Virtuale
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {packages.map(bundle => (
                <div
                  key={bundle.id}
                  className={`p-4 rounded-2xl border flex flex-col justify-between transition-all duration-200 ${
                    bundle.popular
                      ? 'border-indigo-500 bg-gradient-to-b from-indigo-50/90 to-violet-50/50 dark:from-indigo-950/40 dark:to-violet-950/20 shadow-md ring-2 ring-indigo-500/30'
                      : 'border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 hover:border-indigo-300 shadow-2xs'
                  }`}
                >
                  <div className="space-y-1">
                    <span className="text-[10px] uppercase font-bold text-indigo-600 dark:text-indigo-400 tracking-wide">
                      {bundle.tag}
                    </span>
                    <h5 className="text-sm font-bold text-slate-900 dark:text-white">{bundle.name}</h5>
                    <div className="pt-2 text-2xl font-extrabold font-mono text-slate-900 dark:text-white tabular-nums">
                      {bundle.totalCredits} <span className="text-xs font-sans font-normal text-slate-400">credite</span>
                    </div>
                    {bundle.bonusCredits > 0 && (
                      <span className="text-[11px] text-emerald-600 font-bold block">
                        +{bundle.bonusCredits} credite bonus cadou
                      </span>
                    )}
                  </div>

                  <div className="mt-4">
                    <Button
                      variant={bundle.popular ? 'primary' : 'secondary'}
                      size="sm"
                      className="w-full"
                      isLoading={isProcessing || isLoadingPackages}
                      onClick={() => void handleSelectPackage(bundle)}
                    >
                      Cumpără · {formatBundlePrice(bundle)}
                    </Button>
                  </div>
                </div>
              ))}
            </div>

            {isLoadingPackages && (
              <p role="status" className="mt-3 text-[11px] text-slate-500 dark:text-slate-400">
                Se încarcă pachetele de credite...
              </p>
            )}
            {!isLoadingPackages && packages.length === 0 && (
              <p role="status" className="mt-3 text-[11px] text-slate-500 dark:text-slate-400">
                Pachetele de credite nu sunt disponibile momentan.
              </p>
            )}
            {feedback && (
              <p role="status" className="mt-3 text-[11px] font-semibold text-slate-600 dark:text-slate-300">
                {feedback}
              </p>
            )}
          </div>

          {/* Transaction Ledger */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                <History className="w-3.5 h-3.5 text-indigo-500" />
                Registru Tranzacții & Audit Atomic
              </h4>
              <Badge variant="indigo">Fără Debitări Duplicate</Badge>
            </div>

            <div className="border border-slate-200/90 dark:border-slate-800 rounded-2xl overflow-hidden divide-y divide-slate-100 dark:divide-slate-800 text-xs shadow-2xs">
              {transactions.map(tx => (
                <div key={tx.id} className="p-3.5 flex items-center justify-between bg-white dark:bg-slate-900">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                        tx.amount > 0
                          ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400'
                          : 'bg-indigo-50 dark:bg-slate-800 text-indigo-600 dark:text-indigo-400'
                      }`}
                    >
                      {tx.amount > 0 ? (
                        <ArrowDownLeft className="w-4 h-4" />
                      ) : (
                        <ArrowUpRight className="w-4 h-4" />
                      )}
                    </div>
                    <div>
                      <p className="font-bold text-slate-900 dark:text-white">{tx.description}</p>
                      <span className="text-[10px] text-slate-400 font-mono">{tx.timestamp} · ID: {tx.id}</span>
                    </div>
                  </div>

                  <div className="text-right">
                    <span
                      className={`font-mono font-extrabold block tabular-nums ${
                        tx.amount > 0
                          ? 'text-emerald-600 dark:text-emerald-400'
                          : 'text-slate-900 dark:text-white'
                      }`}
                    >
                      {tx.amount > 0 ? `+${tx.amount}` : tx.amount} credite
                    </span>
                    <span className="text-[10px] text-slate-400 font-mono">
                      Sold după: {tx.balanceAfter}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
