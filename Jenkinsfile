pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw clean verify'
                    } else {
                        bat 'mvnw.cmd clean verify'
                    }
                }
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

        stage('Karate API Tests') {
            steps {
                echo 'Running Karate API integration tests...'
                script {
                    if (isUnix()) {
                        sh './mvnw test -Dtest=KarateRunner -DfailIfNoTests=false'
                    } else {
                        bat 'mvnw.cmd test -Dtest=KarateRunner -DfailIfNoTests=false'
                    }
                }
            }
            post {
                always {
                    echo 'Karate test stage complete.'
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/karate-reports',
                        reportFiles: 'karate-summary.html',
                        reportName: 'Karate Test Report'
                    ])
                }
                success {
                    echo 'Karate API tests passed.'
                }
                failure {
                    echo 'Karate API tests failed.'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    script {
                        if (isUnix()) {
                            sh './mvnw sonar:sonar -Dsonar.projectKey=maintenance-service -Dsonar.projectName="Maintenance Service"'
                        } else {
                            bat 'mvnw.cmd sonar:sonar -Dsonar.projectKey=maintenance-service -Dsonar.projectName="Maintenance Service"'
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 20, unit: 'MINUTES') {
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
            echo 'Maintenance Service build successful.'
        }
        failure {
            echo 'Maintenance Service pipeline failed.'
        }
    }
}