// vars/deployTools.groovy

// Função genérica para deploy de um serviço
def deployService(Map config) {
    // Monta o caminho de deploy no servidor remoto de forma dinâmica
    def remoteDeployPath = "/pdi/transito360/${config.cidade}/${config.servico}"
    
    // **MUDANÇA PRINCIPAL AQUI**
    // O host agora é lido dinamicamente do mapa de configuração vindo do Jenkinsfile.
    def remoteHost = config.remoteHost

    // Adiciona uma verificação para garantir que o host foi passado corretamente.
    if (!remoteHost) {
        error("ERRO: O parâmetro 'remoteHost' não foi encontrado na configuração. Verifique o Jenkinsfile do sub-pipeline e a credencial do .env da cidade.")
    }

    withCredentials([
        sshUserPrivateKey(credentialsId: config.sshCredentialId, keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER'),
        file(credentialsId: config.envCredentialId, variable: 'ENV_FILE')
    ]) {
        
        // 1. Garante que o diretório de deploy exista no servidor (usando o host parametrizado)
        sh "ssh -o StrictHostKeyChecking=no -i \${SSH_KEY} \${SSH_USER}@${remoteHost} 'mkdir -p ${remoteDeployPath}'"

        // 2. Sincroniza o código fonte do diretório atual para o servidor
        sh """
            echo "📦 Sincronizando código de '${config.servico}' para ${remoteHost}:${remoteDeployPath}..."
            rsync -avz --delete --exclude='.git/' -e "ssh -i \${SSH_KEY} -o StrictHostKeyChecking=no" ./ \${SSH_USER}@${remoteHost}:${remoteDeployPath}/
        """
        
        // 3. Envia o arquivo .env específico da cidade
        sh "scp -o StrictHostKeyChecking=no -i \${SSH_KEY} \${ENV_FILE} \${SSH_USER}@${remoteHost}:${remoteDeployPath}/.env"

        // 4. Executa o docker-compose no servidor para build e deploy
        sh """
            echo "🚀 Executando docker-compose remoto em ${remoteHost}..."
            ssh -o StrictHostKeyChecking=no -i \${SSH_KEY} \${SSH_USER}@${remoteHost} "
                cd ${remoteDeployPath} &&
                export CIDADE_SUFFIX=${config.cidade} &&
                docker-compose down --remove-orphans &&
                docker-compose up -d --build --remove-orphans
            "
        """
        echo "✅ Deploy do serviço '${config.servico}' para '${config.cidade}' no host '${remoteHost}' concluído."
    }
}

return this
