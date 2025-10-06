def deployService(Map config) {
    def remoteDeployPath = "/pdi/transito360/${config.cidade}/${config.servico}"
    def remoteHost = config.remoteHost

    if (!remoteHost) {
        error("ERRO: O parâmetro 'remoteHost' não foi encontrado na configuração.")
    }

    withCredentials([
        sshUserPrivateKey(credentialsId: config.sshCredentialId, keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER'),
        file(credentialsId: config.envCredentialId, variable: 'ENV_FILE')
    ]) {
        
        sh "ssh -o StrictHostKeyChecking=no -i \${SSH_KEY} \${SSH_USER}@${remoteHost} 'mkdir -p ${remoteDeployPath}'"

        // --- INÍCIO DA CORREÇÃO ---
        // Adicionamos a flag '--no-times' para não tentar modificar os timestamps no destino.
        sh """
            echo "📦 Sincronizando código de '${config.servico}' para ${remoteHost}:${remoteDeployPath}..."
            rsync -avz --delete --no-times -e "ssh -i \${SSH_KEY} -o StrictHostKeyChecking=no" --exclude='.git/' ./ \${SSH_USER}@${remoteHost}:${remoteDeployPath}/
        """
        // --- FIM DA CORREÇÃO ---
        
        sh "scp -o StrictHostKeyChecking=no -i \${SSH_KEY} \${ENV_FILE} \${SSH_USER}@${remoteHost}:${remoteDeployPath}/.env"

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
