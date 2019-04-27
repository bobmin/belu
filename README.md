# belu

![](https://img.shields.io/github/languages/code-size/bobmin/belu.svg?style=flat) 
![](https://img.shields.io/github/last-commit/bobmin/belu.svg?style=flat) 
![](https://img.shields.io/github/languages/top/bobmin/belu.svg?style=flat)

[3 tips for organizing your open source project's workflow on GitHub](https://opensource.com/article/18/4/keep-your-project-organized-git-repo)

[Managing project boards in your repository or organization](https://help.github.com/articles/managing-project-boards-in-your-repository-or-organization)

[Mastering Markdown](https://guides.github.com/features/mastering-markdown/)

## Getting started

### Wetterinfo bereitstellen

Daten abrufen; schreibt `%TEMP%/holeSeite_YYYY-MM-DD.htm`

`bob.belu.HoleSeite -url=https://...`

Infos extrahieren; Symbole ist Nummer aus Dateiname (z.B. 16 aus http://vortex.accuweather.com/adc2010/images/slate/icons/16.svg)

`bob.belu.HeuteFinden -file=%TEMP%/holeSeite_YYYY-MM-DD.htm`

### Bilder organisieren

Bilderordner analysieren

`bob.belu.BilderOrdner -path=X:\Bilder`

Bilder auswählen

`bob.belu.BilderAuswahl -path=X:\Bilder`

Bilder kopieren

`bob.belu.BilderKopierer -path=X:\Bilder -to=C:\Web\belu`