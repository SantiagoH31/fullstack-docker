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
            absolute(15)
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
        archiveArtifacts {
            pattern('dist/**/*')
            allowEmpty(true)
        }

        publishHtml {
            reportDir('coverage')
            reportFiles('index.html')
            reportName('Coverage Report')
            allowMissing(false)
            keepAll(true)
            alwaysLinkToLastBuild(true)
        }

        junit {
            testResults('test-results.xml')
            allowEmptyResults(true)
        }

        mailer {
            recipients('tu-email@empresa.com')
            notifyEveryUnstableBuild(true)
            sendToIndividuals(true)
        }

        wsCleanup()
    }
}
