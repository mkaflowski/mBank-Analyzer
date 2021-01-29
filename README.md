# mBank transaction analyzer

Aplikacja analizuje transakcje z emaklera mBanku licząć zysk/stratę oraz podaje z jakim kursem nasępne akcje zostaną sprzedane.

![Alt Text](https://github.com/mkaflowski/mBank-Analyzer/blob/master/raw/1.3.jpg?raw=true)

## Instrukcja

Wersje aplikacji do pobrania: https://github.com/mkaflowski/mBank-Analyzer/releases <br />
Należy pobrać plik csv ze WSZYSTKIMI transakcjami historycznymi ze strony mBanku.<br />

![Alt Text](https://github.com/mkaflowski/mBank-Analyzer/blob/master/raw/mbank%20instr.jpg?raw=true)

Plik csv można przeciągnąć w pole gdzie należy go podać. Na start jest wczytywany najnowszy plik csv z katalogu "Pobrane".<br />
Aby wyświetlić konsolę z kolorami należy włączyć aplikację z wiersza poleceń:
```
java -jar <sciezka do pobranej aplikacji>
```
Dodatkowo dla Windowsa należy włączyć obsługę kolorów w konsoli:
https://superuser.com/a/1300251
