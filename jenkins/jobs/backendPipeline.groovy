pipelineJob('Backend/backend-pipeline') {
    displayName('🔧 Backend - CI/CD Pipeline')
    description('Pipeline completo para el backend Node.js con Docker')

    // 🔧 Mueve esto fuera de properties
    parameters {
        choiceParam('ENVIRONMENT', ['development', 'staging', 'production'], 'Ambiente de despliegue')
        booleanParam('SKIP_TESTS', false, 'Saltar tests unitarios')
        booleanParam('SKIP_INTEGRATION_TESTS', false, 'Saltar tests de integración')
        booleanParam('FORCE_DEPLOY', false, 'Forzar deploy aunque fallen los tests')
        stringParam('DOCKER_TAG', 'latest', 'Tag para la imagen Docker')
    }

    properties {
        githubProjectUrl('https://github.com/legarrod/back-Docker.git')
    }

    triggers {
        githubPush()
        cron('H/10 * * * *') // Ejecuta cada 10 minutos
    }


    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/legarrod/back-Docker.git')
                        credentials('github-credentials')
                    }
                    branches('*/main', '*/develop')
                }
            }
            scriptPath('Jenkinsfile')
        }
    }

    configure { project ->
        project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty' / 'triggers' << 'com.cloudbees.jenkins.GitHubPushTrigger' {
            spec('')
        }
    }
}
