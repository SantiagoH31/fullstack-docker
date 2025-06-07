pipelineJob('frontend-build-deploy') {
    displayName('Frontend - Build and Deploy')
    description('Pipeline para construir y desplegar el frontend Angular')
    
    properties {
        githubProjectUrl('https://github.com/legarrod/front-Docker.git')
    }
    
    triggers {
        githubPush()
        pollSCM('H/5 * * * *')
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

// Job alternativo usando steps directos si no quieres usar Jenkinsfile
job('frontend-simple-build') {
    displayName('Frontend - Simple Build')
    description('Job simple para construir el frontend')
    
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
        pollSCM('H/5 * * * *')
    }
    
    wrappers {
        nodejs('NodeJS-18')
        timeout {
            absolute(10)
        }
        timestamps()
    }
    
    steps {
        shell('''
            echo "=== Installing dependencies ==="
            npm ci
            
            echo "=== Running tests ==="
            npm run test -- --watch=false --browsers=ChromeHeadless
            
            echo "=== Building application ==="
            npm run build --prod
            
            echo "=== Building Docker image ==="
            docker build -t angular-frontend:${BUILD_NUMBER} .
            docker tag angular-frontend:${BUILD_NUMBER} angular-frontend:latest
        ''')
    }
    
    publishers {
        archiveArtifacts {
            pattern('dist/**/*')
            allowEmpty(false)
        }
        wsCleanup()
    }
}