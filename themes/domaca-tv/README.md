# Téma Domáca TV (Mi Box S, Android TV 9)

Moja téma pre Xiaomi Mi Box S (MIBOX4): dok s mojimi aplikáciami, tapeta z vlastných fotiek, 24-hodinové hodiny, bez ponuky Vstupy (box nemá HDMI vstup ani tuner).

## Dok
Netflix, YouTube a Rádio sú zadané v `config.json`. Sweet.tv, Voyo, Magio TV a Seznam sa doplnia
po inštalácii podľa presných názvov balíkov z boxu (`pm list packages`), alebo priamo na TV cez
Nastavenia → Upraviť dok. Aplikácia, ktorá nie je nainštalovaná, sa v doku preskočí.

## Tapeta z vlastných fotiek
Fotky daj do priečinka `wallpapers/` vedľa tohto súboru (napr. `001.jpg`, `002.jpg` …), ideálne
zmenšené na 1920×1080. **Priečinok `wallpapers/` sa do Gitu nenahráva** (je v `.gitignore`),
lebo repozitár je verejný. Popisy k fotkám: `wallpapers/captions.txt`, jeden riadok na fotku
(`001.jpg<TAB>Dovolenka 2025`).

Nahratie do boxu z PC: `TV_HOST=<ip boxu> tools/theme.sh themes/domaca-tv`
(Windows: `tools\theme.ps1`).

Fotky sa striedajú každých 10 minút v náhodnom poradí a sú mierne stmavené (25 %), aby boli
hodiny čitateľné.
