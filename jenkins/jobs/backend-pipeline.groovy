pipelineJob('backend-build-deploy') {
    displayName('Backend - Build and Deploy')
    description('Pipeline para construir y desplegar el backend Node.js')
    
    properties {
        githubProjectUrl('https://github.com/legarrod/back-Docker.git')
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
                        url('https://github.com/legarrod/back-Docker.git')
                    }
                    branch('*/main')
                }
            }
            scriptPath('Jenkinsfile')
        }
    }
}

// Job simple para el backend
job('backend-simple-build') {
    displayName('Backend - Simple Build')
    description('Job simple para construir el backend Node.js')
    
    scm {
        git {
            remote {
                url('https://github.com/legarrod/back-Docker.git')
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
            
            echo "=== Running linting ==="
            npm run lint || echo "Linting completed with warnings"
            
            echo "=== Running tests ==="
            npm test
            
            echo "=== Building Docker image ==="
            docker build -t node-backend:${BUILD_NUMBER} .
            docker tag node-backend:${BUILD_NUMBER} node-backend:latest
        ''')
    }
    
    publishers {
        archiveArtifacts {
            pattern('coverage/**/*')
            allowEmpty(true)
        }
        wsCleanup()
    }
}