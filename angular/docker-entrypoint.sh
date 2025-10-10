#!/bin/sh
set -e

# -------- Defaults --------
: "${CIDADE_SUFFIX:=lem}"
: "${FRONT_BASE_HREF:=/${CIDADE_SUFFIX}/}"
: "${BACKEND_PROTOCOL:=http}"
: "${BACKEND_HOST:=localhost}"
: "${BACKEND_PORT:=80}"
: "${BACKEND_APIROOT:=v1}"
: "${BACKEND_CONTEXT:=${CIDADE_SUFFIX}/api}"

DOCROOT="/usr/share/nginx/html"
INDEX="${DOCROOT}/index.html"

# -------- Ajusta <base href> no index.html --------
if [ -f "$INDEX" ]; then
  # casa tanto <base href="/"> quanto <base href="/" />
  sed -ri "s#<base[[:space:]]+href=\"[^\"]*\"[[:space:]]*/?>#<base href=\"${FRONT_BASE_HREF}\">#I" "$INDEX"
fi

# -------- Gera env.js --------
mkdir -p "${DOCROOT}/assets" "${DOCROOT}/public/env"

# Caminho relativo (recomendado) via reverse proxy
API_BASE="/${BACKEND_CONTEXT}"

cat > "${DOCROOT}/assets/env.js" <<EOF
(function (window) {
  window.env = window.env || {};
  window["env"]["protocol"] = "${BACKEND_PROTOCOL}";
  window["env"]["apiroot"]  = "${BACKEND_APIROOT}";
  window["env"]["host"]     = "${BACKEND_HOST}";
  window["env"]["port"]     = "${BACKEND_PORT}";
  window["env"]["context"]  = "${BACKEND_CONTEXT}";
  window["env"]["apiBase"]  = "${API_BASE}";
  window["env"]["city"]     = "${CIDADE_SUFFIX}";
  window["env"]["baseHref"] = "${FRONT_BASE_HREF}";
})(this);
EOF

cp -f "${DOCROOT}/assets/env.js" "${DOCROOT}/public/env/env.js" || true

exec "$@"