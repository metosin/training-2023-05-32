(ns training.web.view.artist-page
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]))


(defnc ArtistsPage [_]
  (d/div "Artists"))


(defnc ArtistPage [_]
  (d/div "Artist"))
