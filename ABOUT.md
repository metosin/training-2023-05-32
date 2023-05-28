# Example application for Clojure Advanced training

This is an example application for [Clojure](https://clojure.org/) Advanced training organized by [Metosin Ltd](https://metosin.fi).

The used database is a Postgres image with built in data of music artists, albums, tracks and genres. The database contains about 23k artists, 62k albums, 1M tracks and 700 genres. The database is provided as [metosin/training-musicbrainz-db](https://hub.docker.com/r/metosin/training-musicbrainz-db) docker container (about 500MB). The container its freely available under the [CC0](https://creativecommons.org/publicdomain/zero/1.0/) license.

Cover images are provided in [metosin/training-musicbrainz-covers](https://hub.docker.com/r/metosin/training-musicbrainz-covers) docker container (about 1.7GB). The container contains a Caddy web server and a cover image for about 43k albums. This container is also provided under the [CC0](https://creativecommons.org/publicdomain/zero/1.0/) license.

The content of the database container and the cover images container are a subset of data loaded from excellent [MusicBrainz.org](https://musicbrainz.org/doc/MusicBrainz_Database) database. MusicBrainz database is used under the [CC0](https://creativecommons.org/publicdomain/zero/1.0/) license.

The reference to Epe's record shop is a homage to a legenrady [Epe's record shop](http://www.epes.fi/) that operated from 1972 to 2014 in Tampere, Finland. The reference is intended to show respect and gratitude.

All training materials, including all documentation, presentations and code, is under the Copyright © 2023 of [Metosin Ltd](https://metosin.fi/). All rights are reserved.
