(ns training.web.view.about-page
  (:require [helix.core :as hx :refer [defnc $]]
            [helix.dom :as d]))


(defnc Link [{:keys [href children]}]
  (d/a {:href   href
        :target "_blank"}
       children))


(defnc AboutPage [_]
  (d/div {:class ["about-page"]}
         (d/p
          "This is an example application for Clojure Advanced training organized by "
          ($ Link {:href "https://metosin.fi/"} "Metosin Ltd")
          ".")
         (d/p
          "The used database is a Postgres image with built in data of music artists, albums, 
           tracks and genres. The database contains about 23k artists, 62k albums, 1M tracks and
           700 genres. The database is provided as "
          ($ Link {:href "https://hub.docker.com/r/metosin/training-musicbrainz-db"} "metosin/training-musicbrainz-db")
          " docker container. The container its freely available under the "
          ($ Link {:href "https://creativecommons.org/publicdomain/zero/1.0/"} "CC0 license")
          ".")
         (d/p
          "The album cover images are also loaded from MusicBrainz. Cover images are provided
           in "
          ($ Link {:href "https://hub.docker.com/r/metosin/training-musicbrainz-covers"} "metosin/training-musicbrainz-covers")
          " docker container. The container contains a "
          ($ Link {:href "https://caddyserver.com/"} "Caddy web server")
          " and a cover image for about 43k albums. This container is also provided under the "
          ($ Link {:href "https://creativecommons.org/publicdomain/zero/1.0/"} "CC0 license")
          ".")
         (d/p
          "The content of the database container and the cover images container are a subset of
           data loaded from excellent "
          ($ Link {:href "https://musicbrainz.org/doc/MusicBrainz_Database"} "MusicBrainz.org")
          " database. MusicBrainz database is used under the "
          ($ Link {:href "https://creativecommons.org/publicdomain/zero/1.0/"} "CC0 license")
          ".")
         (d/p
          "The reference to " (d/b "Epe's")
          " record shop is a homage to a legenrady "
          ($ Link {:href "http://www.epes.fi/"} "Epe's record shop")
          " that operated from 1972 to 2014 in Tampere, Finland. The reference is intended to show
           respect and gratitude.")
         (d/p
          "All training materials, including all documentation, presentations and code, is under
           the Copyright © 2023 of "
          ($ Link {:href "https://metosin.fi/"} "Metosin Ltd")
          ". All rights are reserved.")))
