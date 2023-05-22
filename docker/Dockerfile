FROM debian:bullseye-slim


ARG NODE_VERSION=20
ARG JAVA_VERSION=20
ARG CLOJURE_VERSION=1.11.1.1189


RUN \
  #
  # Base setup:
  #
  apt update -q                                                                    && \
  rm -f /etc/ssl/certs/ca-bundle.crt                                               && \
  apt reinstall -qy                                                                \
    ca-certificates                                                                && \
  update-ca-certificates                                                           && \
  #
  # Install & build tools:
  #
  apt install -qy --no-install-recommends                                          \
    curl                                                                           \
    gcc                                                                            \
    g++                                                                            \
    make                                                                           \
    socat                                                                          \
    gnupg                                                                          \
    apt-transport-https                                                            && \
  #
  # Install Node:
  #
  curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x                    \
    | bash -                                                                       && \
  apt update -q                                                                    && \
  apt install -qy --no-install-recommends                                          \
    nodejs                                                                         && \
  #
  # Install Java:
  #
  curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public          \
    | gpg --dearmor                                                                \
    > /etc/apt/trusted.gpg.d/temurin.gpg                                           && \
  echo "deb https://packages.adoptium.net/artifactory/deb bullseye main"           \
    > /etc/apt/sources.list.d/adoptium.list                                        && \
  apt update -q                                                                    && \
  apt install -qy --no-install-recommends                                          \
    temurin-${JAVA_VERSION}-jdk                                                    && \
  java -version                                                                    && \
  #
  # Install Clojure:
  #
  curl -fsSL https://download.clojure.org/install/linux-install-$CLOJURE_VERSION.sh \
    | bash -                                                                       && \
  clojure -P                                                                       && \
  clojure -e "(clojure-version)"                                                   && \
  #
  # Cleanup:
  #
  apt purge -y --auto-remove                                                       \
    curl                                                                           \
    gcc                                                                            \
    g++                                                                            \
    make                                                                           \
    gnupg                                                                          \
    apt-transport-https                                                            && \
  apt clean -y                                                                     && \
  rm -rf /var/lib/apt/lists/*


ENV LANG C.UTF-8
WORKDIR /app


COPY --chmod=0544 .bashrc /root/.bashrc
COPY --chmod=0755 socat-tunnel.sh /bin


CMD ["bash"]
