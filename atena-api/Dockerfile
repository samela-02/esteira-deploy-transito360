# Usamos uma única imagem base com tudo que é necessário
FROM python:3.12.0

# Instala as dependências do sistema
RUN apt-get update && apt-get install -y --no-install-recommends \
    git \
    libgirepository1.0-dev \
    libcairo2-dev \
    gir1.2-gtk-3.0 \
    locales \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Configura o locale
RUN echo "pt_BR.UTF-8 UTF-8" >> /etc/locale.gen && locale-gen pt_BR.UTF-8

WORKDIR /app

# Copia os arquivos de código-fonte
COPY . .

# Instala as dependências Python.
# O uvicorn e outros executáveis serão instalados em /usr/local/bin/
RUN pip install --no-cache-dir --upgrade -r requirements.txt --timeout 800

# Configura o ambiente Java
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

EXPOSE 8000 4040

# Cria um usuário não-root para segurança
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

# --- INÍCIO DA CORREÇÃO ---
# Dá a propriedade da pasta da aplicação para o novo usuário.
# Isso permite que a aplicação (rodando como 'appuser') leia arquivos como o .env,
# que é injetado pelo docker-compose e pertence ao 'root' por padrão.
RUN chown -R appuser:appgroup /app
# --- FIM DA CORREÇÃO ---

USER appuser

# O comando para iniciar a aplicação agora encontrará o 'uvicorn'
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--log-config", "log-config.json"]

