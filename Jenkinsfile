// Jenkinsfile para o job 'transito360-orchestrator'

// Lista de todas as cidades e serviços disponíveis
def allCities = ['vca', 'brumado', 'lem'] // Usar os nomes exatos das pastas
def allServices = ['front-end', 'backend', 'atena-api']

// Mapeamento do nome do serviço para o nome do job no Jenkins
def serviceJobMap = [
    'front-end': 'transito360-frontend-deploy',
    'backend'  : 'transito360-backend-deploy',
    'atena-api': 'atena-api-deploy'
]

pipeline {
    agent none

    parameters {
        choice(name: 'CIDADE', choices: ['Todas'] + allCities, description: 'Escolha a cidade de destino.')
        choice(name: 'SERVICO', choices: ['Todos'] + allServices, description: 'Escolha o serviço para deploy.')
        string(name: 'VERSAO', defaultValue: 'main', description: 'Branch ou tag do Git para fazer o deploy.')
    }

    stages {
        stage('Disparar Deploys') {
            steps {
                script {
                    def deploysToRun = [:]
                    
                    // Determina para quais cidades o deploy será feito
                    def targetCities = (params.CIDADE == 'Todas') ? allCities : [params.CIDADE]
                    
                    // Determina quais serviços serão implantados
                    def targetServices = (params.SERVICO == 'Todos') ? allServices : [params.SERVICO]

                    // Monta a lista de deploys para executar em paralelo
                    for (city in targetCities) {
                        for (service in targetServices) {
                            def jobName = serviceJobMap[service]
                            if (jobName) {
                                // A chave do mapa precisa ser única para cada deploy
                                def deployKey = "${city}-${service}"
                                
                                deploysToRun[deployKey] = {
                                    echo "Disparando deploy do serviço '${service}' para a cidade '${city}' na versão '${params.VERSAO}'."
                                    build job: jobName,
                                          parameters: [
                                              string(name: 'CIDADE', value: city),
                                              string(name: 'VERSAO', value: params.VERSAO)
                                          ]
                                }
                            }
                        }
                    }

                    // Executa todos os deploys em paralelo
                    if (!deploysToRun.isEmpty()) {
                        parallel deploysToRun
                    } else {
                        echo "Nenhum deploy para executar."
                    }
                }
            }
        }
    }
}