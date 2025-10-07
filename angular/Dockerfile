# --- ESTÁGIO 1: BUILD ---
FROM node:22-alpine AS build

# Define o diretório de trabalho
WORKDIR /app

# Argumento para o token, que será passado pelo docker-compose
ARG NPM_AUTH_TOKEN

# Copia os arquivos de dependência
COPY package*.json ./

# Cria o arquivo .npmrc usando o token recebido como argumento de build
RUN echo "@tivic-team:registry=https://npm.pkg.github.com" > .npmrc && \
    echo "//npm.pkg.github.com/:_authToken=${NPM_AUTH_TOKEN}" >> .npmrc && \
    echo "always-auth=true" >> .npmrc

# Instala as dependências
RUN npm ci
RUN rm -f .npmrc # Remove o .npmrc por segurança

# Copia todo o resto do código da aplicação (que o Jenkins já enviou)
COPY . .

# Compila a aplicação Angular para produção
RUN npm run build

# --- ESTÁGIO 2: RUNTIME ---
FROM nginx:alpine

# Copia os arquivos da aplicação Angular compilada
COPY --from=build /app/dist/angular-base/browser /usr/share/nginx/html

# Copia a configuração personalizada do Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Copia o script de entrada que configura o ambiente em tempo de execução
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]

