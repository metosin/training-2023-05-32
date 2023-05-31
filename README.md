# Advanced Clojure koulutus 2023-05-31

Varmista että sinulla on kaikki [tässä esitellyt esitehtävät](https://github.com/metosin/advanced-clojure-2023-05-31-setup) suoritettuna ennen koulutusta.

## Aloitus

- Kloonaa tämä projekti omalle koneellesi
  ```bash
  $ git clone git@github.com:metosin/training-2023-05-32.git
  $ cd training-2023-05-32
  ```
- Käynnistä palvelut
  ```bash
  $ docker compose up
  ```
- Avaa projekti käyttämääsi IDE:een ja käynnistä REPL
- Aja REPL:ssä komento:
  ```
  user=> (reset)
  ```
- Avaa selain http://localhost:8000

## Tietovarasto

Mukana tuleva PostgreSQL tietokanta sisältää aineistoa MusicBrainz.org palvelusta. Tietokannan rakenne on [kuvattu tässä](./doc/MusicBrainz-subset-ERD.png). Lisää tietoa [ABOUT.md](./public/ABOUT.md) tiedostossa.

## Links

- [Try Clojure](https://tryclojure.org) ClojureScript REPL on web
- [Clojure API](https://clojure.org/api/api) Official API docs
- [ClojureDocs](https://clojuredocs.org) Community docs with examples etc.
- [ClojureScript API](http://cljs.github.io/api/) ClojureScript API docs
- [Clojure Toolbox](https://www.clojure-toolbox.com/) Massive list of libs
- [Google Closure API](https://google.github.io/closure-library/api/) Commonly used Closure lib
- [Shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html) Shadow-cljs build tool docs
- [Clojure cheatsheet](https://clojure.org/api/cheatsheet)
- [ClojureScript cheatsheet](https://cljs.info/cheatsheet/)
- [ReFrame](https://day8.github.io/re-frame/re-frame/)
- [Reagent](https://reagent-project.github.io/)
- [Helix](https://github.com/lilactown/helix)
- [Awesome Clojure libs](https://github.com/mbuczko/awesome-clojure)
- [Awesome Clojure libs also](https://github.com/razum2um/awesome-clojure)
- [Awesome ClojureScript libs](https://github.com/hantuzun/awesome-clojurescript)
- [Metosin in GitHub](https://github.com/metosin)
