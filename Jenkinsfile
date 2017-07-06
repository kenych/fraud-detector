pipeline {
    agent any

    tools {
        jdk 'jdk-8'
        maven 'maven3'
    }

    stages {
        stage('Install') {
            steps {
                sh "mvn -U clean install"
            }
            post {
                always {
                    junit '**/target/*-reports/TEST-*.xml'
                }
            }
        }
    }
}


