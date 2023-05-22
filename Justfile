set dotenv-load := true
project := "advanced-clojure-2023-05-31"


help:
  @just --list


# Run CLJS tests
cljs-test:
  @npx shadow-cljs compile test


# Run CLJ tests
clj-test focus=':unit' +opts="":
  @clojure -M:test                                             \
           -m kaocha.runner                                    \
           --reporter kaocha.report/dots                       \
           --focus {{ focus }}                                 \
           {{ opts }}
  

# Run all tests
test: clj-test cljs-test
  @echo "All tests run"


# Launch bash shell on container
sh +args='bash':
  @docker run                                                  \
      --rm -it                                                 \
      -v $(pwd)/.bashrc:/root/.bashrc                          \
      -v $(pwd):/app:cached                                    \
      -w /app                                                  \
      --network {{ project }}_default                          \
      {{ project }}:dev                                        \
      {{ args }}


# PSQL shell to DB
psql +args='':
  @docker run                                                  \
    --rm -it                                                   \
    -e PGPASSWORD=$POSTGRES_PASSWORD                           \
    -v $(pwd)/.psqlrc:/root/.psqlrc                            \
    -v $(pwd):/app:cached                                      \
    -w /app                                                    \
    --network {{ project }}_default                            \
    postgres:15-bullseye                                       \
    psql -h db                                                 \
         -p $POSTGRES_PORT                                     \
         -U $POSTGRES_USER                                     \
         -d $POSTGRES_DB                                       \
         {{ args }}


# Check for outdated deps
outdated:
  @clj -M:outdated


# Initialize dev setup:
init:
  npm i
  clojure -A:cljs:dev:test -P
  docker compose pull
  docker compose build
  @echo "\n\nReady"
