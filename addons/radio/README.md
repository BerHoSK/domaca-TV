# Rádio (Domáca TV)

Internetové rádio pre launcher Domáca TV. Karta **Radio** na domovskej obrazovke otvorí zoznam staníc, ktorý sa ovláda šípkami na ovládači:

- **OK** na stanici ju prehrá, druhé OK ju zastaví.
- **Podržanie OK** na stanici ju odstráni (s potvrdením).
- **+ Pridať stanicu**: názov a adresa streamu (http:// alebo https://).
- **Obnoviť predvolené stanice**: vráti pôvodný zoznam.

Rádio hrá ďalej aj po návrate na Domov. Keď spustíte inú aplikáciu so zvukom (Netflix, YouTube), rádio sa samo zastaví.

Predvolené stanice sú rovnaké ako v Rodinári+: Rádio Lumen, Rádio Vlna, Rádio FM, Fun Radio, Rádio Slovensko, Rádio Devín, Europa 2, Rádio Beta 80-90, Rádio Beta, Rádio Expres, Rádio Melody, Hitrádio Slovakia a Rádio Regina Západ. Vysielanie patrí jednotlivým rozhlasovým staniciam.

Vyskúšané na: Xiaomi Mi Box S (MIBOX4), Android TV 9.

## Permissions

- `INTERNET`: prehrávanie streamov rádií, nič iné. Žiadna telemetria ani reklamy.
- `FOREGROUND_SERVICE` a `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: služba v popredí, aby rádio hralo aj na domovskej obrazovke. Beží len počas prehrávania.
- `WAKE_LOCK`: prehrávač počas hrania nezaspí.

## Zostavenie

APK zostavuje workflow `domaca-tv-apk.yml` a zverejní ho v Releases ako `domaca-tv-radio.apk`.
