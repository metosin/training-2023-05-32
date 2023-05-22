#!/bin/sh

echo "Running socat to tunnel localhost:9630 -> host.docker.internal:9630"
nohup socat TCP4-LISTEN:9630,fork,reuseaddr TCP4:host.docker.internal:9630 > /tmp/socat.out 2>&1 &

echo "Launching service..."
exec "$@"
