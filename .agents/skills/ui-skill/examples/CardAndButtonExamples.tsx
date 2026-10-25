import React from 'react';
import { Card } from '../references/component-catalog';
import { Button } from '../references/component-catalog';
import { Badge } from '../references/component-catalog';
import { Coins, ShieldCheck, MapPin, Sparkles } from 'lucide-react';

export const LeadCardExample: React.FC = () => {
  return (
    <Card interactive padding="md" className="space-y-4">
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <Badge variant="indigo">Instalații Electrice</Badge>
            <Badge variant="emerald" icon={<ShieldCheck className="w-3 h-3" />}>
              Lead Verificat
            </Badge>
          </div>
          <h3 className="text-base font-semibold text-slate-900 dark:text-white">
            Înlocuire Tablou Electric & Branșament Trifazat
          </h3>
        </div>
        <div className="text-right">
          <span className="text-xs text-slate-400">Preț Lead</span>
          <div className="flex items-center gap-1 text-sm font-bold text-indigo-600 dark:text-indigo-400 tabular-nums">
            <Coins className="w-4 h-4" />
            <span>30 Credite</span>
          </div>
        </div>
      </div>

      <p className="text-sm text-slate-600 dark:text-slate-300 line-clamp-2">
        Apartament 3 camere în Sector 1. Doresc schimbarea completă a tabloului de siguranțe vechi cu echipamente moderne Schneider/Hager și verificare PRAM.
      </p>

      <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800 text-xs text-slate-500">
        <div className="flex items-center gap-1.5">
          <MapPin className="w-3.5 h-3.5 text-slate-400" />
          <span>București, Sector 1 (3.2 km)</span>
        </div>
        <Button variant="success" size="sm" icon={<Coins className="w-3.5 h-3.5" />}>
          Deblochează Contact
        </Button>
      </div>
    </Card>
  );
};
