pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/aflouk0020/Maintenance-Service-For-SHM.git'
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvnw.cmd clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                        id: 'maintenance-service-coverage',
                        name: 'Maintenance Service Coverage'
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvnw.cmd sonar:sonar -Dsonar.projectKey=maintenance-service -Dsonar.projectName="Maintenance Service"'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            echo 'Maintenance Service pipeline finished.'
        }
        success {
            echo 'Maintenance Service built successfully.'
        }
        failure {
            echo 'Maintenance Service pipeline failed.'
        }
    }
}