# --- ESTÁGIO DE BUILD ---
FROM python:3.12.0 AS build

RUN apt-get update && apt-get install -y --no-install-recommends \
    git \
    libgirepository1.0-dev \
    libcairo2-dev \
    gir1.2-gtk-3.0 \
    locales \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

RUN echo "pt_BR.UTF-8 UTF-8" >> /etc/locale.gen && locale-gen pt_BR.UTF-8

WORKDIR /app

COPY . .

RUN pip install --no-cache-dir --upgrade -r requirements.txt --timeout 800

# --- ESTÁGIO DE RUNTIME ---
FROM python:3.12.0-slim

# Instala apenas as dependências de RUNTIME
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgirepository1.0-dev \
    libcairo2-dev \
    gir1.2-gtk-3.0 \
    locales \
    openjdk-17-jre \
    && rm -rf /var/lib/apt/lists/*

RUN echo "pt_BR.UTF-8 UTF-8" >> /etc/locale.gen && locale-gen pt_BR.UTF-8

WORKDIR /app

# Copia o código da aplicação do estágio de build
COPY --from=build /app /app

# --- INÍCIO DA CORREÇÃO ---
# Copia as dependências Python (uvicorn, fastapi, etc.) instaladas pelo pip
COPY --from=build /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
# --- FIM DA CORREÇÃO ---

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

EXPOSE 8000 4040

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
USER appuser

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--log-config", "log-config.json"]

