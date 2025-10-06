# --- ESTÁGIO 1: BUILD ---
# Usamos uma imagem com as ferramentas de build (Maven, JDK)
FROM openjdk:21-jdk-slim AS build

# Instala apenas as dependências de build mínimas
RUN apt-get update && apt-get install -y --no-install-recommends \
    locales \
    maven \
    && sed -i '/pt_BR.UTF-8/s/^# //g' /etc/locale.gen \
    && locale-gen \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV LANG=pt_BR.UTF-8

# Define o diretório de trabalho
WORKDIR /app

# Copia o código fonte (que o Jenkins enviou) para dentro da imagem
COPY . .

# Argumento para a versão da lib, recebido do docker-compose
ARG APOLO_LIB_VERSION

# Instala a dependência local .jar
RUN ./mvnw install:install-file \
    -Dfile="src/main/resources/libs/apolo-lib-${APOLO_LIB_VERSION}.jar" \
    -DgroupId="br.tec.bemtevi" \
    -DartifactId="apolo-lib" \
    -Dversion=${APOLO_LIB_VERSION} \
    -Dpackaging=jar

# Compila o projeto e gera o .jar final
RUN ./mvnw clean package -DskipTests

# --- ESTÁGIO 2: RUNTIME ---
# Usamos uma imagem slim, apenas com o necessário para rodar (JRE)
FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y --no-install-recommends locales \
    && sed -i '/pt_BR.UTF-8/s/^# //g' /etc/locale.gen \
    && locale-gen \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV LANG=pt_BR.UTF-8

WORKDIR /app
# Copia apenas o .jar compilado do estágio de build
COPY --from=build /app/target/transito360-0.0.1-SNAPSHOT.jar app.jar

VOLUME /tmp
EXPOSE 8080

# Cria um usuário não-root para rodar a aplicação (mais seguro)
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]