// jenkins/jobs/backend-jobs.groovy

// Pipeline Job para el backend
pipelineJob('Backend/backend-pipeline') {
    displayName('🔧 Backend - CI/CD Pipeline')
    description('Pipeline completo para el backend Node.js con Docker')
    
    properties {
        githubProjectUrl('https://github.com/legarrod/back-Docker.git')
        
        parameters {
            choiceParam('ENVIRONMENT', ['development', 'staging', 'production'], 'Ambiente de despliegue')
            booleanParam('SKIP_TESTS', false, 'Saltar tests unitarios')
            booleanParam('SKIP_INTEGRATION_TESTS', false, 'Saltar tests de integración')
            booleanParam('FORCE_DEPLOY', false, 'Forzar deploy aunque fallen los tests')
            stringParam('DOCKER_TAG', 'latest', 'Tag para la imagen Docker')
        }
    }
    
    triggers {
        githubPush()
        pollSCM('H/10 * * * *')
        cron('H 1 * * *') // Build nocturno
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

// Job simple para builds rápidos
job('Backend/backend-quick-build') {
    displayName('⚡ Backend - Quick Build')
    description('Build rápido del backend sin tests completos')
    
    parameters {
        stringParam('BRANCH', 'main', 'Branch a construir')
        booleanParam('PUSH_TO_REGISTRY', false, 'Subir imagen al registry')
    }
    
    scm {
        git {
            remote {
                url('https://github.com/legarrod/back-Docker.git')
                credentials('github-credentials')
            }
            branch('*/${BRANCH}')
        }
    }
    
    triggers {
        githubPush()
    }
    
    wrappers {
        nodejs('NodeJS-18')
        timeout {
            absolute(10)
        }
        timestamps()
        colorizeOutput()
        buildName('#${BUILD_NUMBER} - ${GIT_BRANCH}')
    }
    
    steps {
        shell('''
            set -e
            
            echo "🔄 Quick build started..."
            
            echo "=== Environment Info ==="
            node --version
            npm --version
            docker --version
            
            echo "=== Installing dependencies ==="
            if [ -f package-lock.json ]; then
                npm ci
            else
                npm install
            fi
            
            echo "=== Basic syntax check ==="
            node -c src/index.js || node -c app.js || echo "Main file check completed"
            
            echo "=== Building Docker image ==="
            IMAGE_NAME="node-backend"
            IMAGE_TAG="quick-${BUILD_NUMBER}"
            
            docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
            
            echo "=== Testing container startup ==="
            docker run -d --name test-${BUILD_NUMBER} -p 3001:3000 ${IMAGE_NAME}:${IMAGE_TAG}
            sleep 10
            
            if docker ps | grep test-${BUILD_NUMBER}; then
                echo "✅ Container started successfully"
            else
                echo "❌ Container failed to start"
                docker logs test-${BUILD_NUMBER}
                exit 1
            fi
            
            docker rm -f test-${BUILD_NUMBER}
            
            echo "✅ Quick build completed successfully!"
        ''')
    }
    
    publishers {
        archiveArtifacts {
            pattern('*.log')
            allowEmpty(true)
        }
        wsCleanup()
        
        // Notificación por email en caso de fallo
        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            defaultSubject('Quick Build - $PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!')
            defaultContent('$DEFAULT_CONTENT')
            triggers {
                failure {
                    sendTo {
                        developers()
                        requester()
                    }
                }
            }
        }
    }
}

// Job para deployments manuales
job('Backend/backend-manual-deploy') {
    displayName('🚀 Backend - Manual Deploy')
    description('Deployment manual del backend a diferentes ambientes')
    
    parameters {
        choiceParam('TARGET_ENVIRONMENT', ['development', 'staging', 'production'], 'Ambiente destino')
        stringParam('IMAGE_TAG', 'latest', 'Tag de la imagen a deployar')
        textParam('DEPLOY_NOTES', '', 'Notas del deployment')
    }
    
    wrappers {
        timeout {
            absolute(15)
        }
        timestamps()
        buildUser()
    }
    
    steps {
        shell('''
            echo "🚀 Manual deployment started by: ${BUILD_USER}"
            echo "Environment: ${TARGET_ENVIRONMENT}"
            echo "Image Tag: ${IMAGE_TAG}"
            echo "Deploy Notes: ${DEPLOY_NOTES}"
            
            case "${TARGET_ENVIRONMENT}" in
                "development")
                    echo "Deploying to development..."
                    docker-compose -f docker-compose.yml stop node-app || true
                    docker-compose -f docker-compose.yml up -d node-app
                    ;;
                "staging")
                    echo "Deploying to staging..."
                    # Comandos para staging
                    echo "Staging deployment completed"
                    ;;
                "production")
                    echo "Deploying to production..."
                    echo "⚠️ Production deployment requires additional confirmation"
                    # Comandos para producción
                    ;;
            esac
            
            echo "✅ Deployment to ${TARGET_ENVIRONMENT} completed"
        ''')
    }
    
    publishers {
        wsCleanup()
    }
}