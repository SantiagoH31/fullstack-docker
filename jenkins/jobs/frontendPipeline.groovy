pipelineJob('frontend-build-deploy') {
    displayName('Frontend - Build and Deploy')
    description('Pipeline para construir y desplegar el frontend Angular con tests')
    
    properties {
        githubProjectUrl('https://github.com/legarrod/front-Docker.git')
    }
    
    triggers {
        githubPush()
        cron('H/10 * * * *')
    }
    
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/legarrod/front-Docker.git')
                    }
                    branch('*/main')
                }
            }
            scriptPath('Jenkinsfile')
        }
    }
}

// Job con steps mejorado incluyendo tests
job('frontend-build-with-tests') {
    displayName('Frontend - Build with Tests')
    description('Job completo para construir el frontend con tests unitarios y cobertura')
    
    scm {
        git {
            remote {
                url('https://github.com/legarrod/front-Docker.git')
            }
            branch('*/main')
        }
    }
    
    triggers {
        githubPush()
        cron('H/10 * * * *')
    }
    
    wrappers {
        nodejs('NodeJS-18')
        timeout {
            absolute(15) // Aumentado para incluir tiempo de tests
        }
        timestamps()
        colorizeOutput()
    }
    
    steps {
        shell('''
            echo "=== Installing dependencies ==="
            npm ci
            
            echo "=== Running linting ==="
            npm run lint || echo "Linting completed with warnings"
            
            echo "=== Running unit tests with coverage ==="
            npm run test -- --watch=false --browsers=ChromeHeadlessCI --code-coverage=true
            
            echo "=== Building application ==="
            npm run build --prod
            
            echo "=== Running e2e tests (opcional) ==="
            # npm run e2e -- --webdriver-update=false
            
            echo "=== Building Docker image ==="
            docker build -t angular-frontend:${BUILD_NUMBER} .
            docker tag angular-frontend:${BUILD_NUMBER} angular-frontend:latest
            
            echo "=== Cleanup ==="
            docker system prune -f
        ''')
    }
    
    publishers {
        // Archivar artefactos
        archiveArtifacts {
            pattern('dist/**')    
            allowEmpty(true)          
        }
        
        // Publicar resultados de tests
        publishHtml([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'coverage',
            reportFiles: 'index.html',
            reportName: 'Coverage Report'
        ])
        
        // Publicar resultados JUnit (si configuras karma para generar XML)
        publishTestResults {
            testResultsPattern('test-results.xml')
            allowEmptyResults(true)
        }
        
        // Notificaciones por email en caso de fallo
        mailer {
            recipients('tu-email@empresa.com')
            notifyEveryUnstableBuild(true)
            sendToIndividuals(true)
        }
        
        wsCleanup()
    }
}

// Pipeline declarativo completo (Jenkinsfile)
// Este sería el contenido de tu Jenkinsfile:

/*
pipeline {
    agent any
    
    tools {
        nodejs 'NodeJS-18'
    }
    
    environment {
        CHROME_BIN = '/usr/bin/google-chrome'
        DISPLAY = ':99'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh 'npm ci'
            }
        }
        
        stage('Lint') {
            steps {
                script {
                    try {
                        sh 'npm run lint'
                    } catch (Exception e) {
                        echo "Linting failed but continuing: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh '''
                    Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &
                    npm run test -- --watch=false --browsers=ChromeHeadlessCI --code-coverage=true
                '''
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'coverage',
                        reportFiles: 'index.html',
                        reportName: 'Coverage Report'
                    ])
                }
            }
        }
        
        stage('Build') {
            steps {
                sh 'npm run build --prod'
            }
        }
        
        stage('Docker Build') {
            steps {
                sh '''
                    docker build -t angular-frontend:${BUILD_NUMBER} .
                    docker tag angular-frontend:${BUILD_NUMBER} angular-frontend:latest
                '''
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    # Aquí van tus comandos de deploy
                    echo "Deploying to production..."
                '''
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'dist/**', allowEmptyArchive: false
            cleanWs()
        }
        failure {
            emailext (
                subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Build failed. Check console output at ${BUILD_URL}",
                to: "tu-email@empresa.com"
            )
        }
    }
}
*/
