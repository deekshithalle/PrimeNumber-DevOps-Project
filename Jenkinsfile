pipeline {
    agent any

    environment {
        IMAGE_NAME = 'prime-number-web'
        DOCKER_EXE = 'C:\\Users\\deeek\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile Java') {
            steps {
                bat 'javac PrimeNumber.java'
            }
        }

        stage('Smoke Test') {
            steps {
                bat 'java -version'
                bat 'if not exist PrimeNumber.class exit /b 1'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat '"%DOCKER_EXE%" build -t %IMAGE_NAME%:latest .'
            }
        }
    }

    post {
        success {
            echo 'Build successful. Docker image prime-number-web:latest is ready.'
        }
        failure {
            echo 'Pipeline failed. Review the stage logs above.'
        }
    }
}
