// Git params
def repoName  = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').last().split("\\.")[0].toLowerCase().replaceAll("[\\p{Punct}\\s\\t]+", "-") // get repo name and replace all special characters
// AWS Params
def awsRegion = "us-west-2"
// Pipeline
pipeline {
  agent { label 'android' }
  options {
    ansiColor('xterm')
  }
  environment {
    ANDROID_HOME = "/opt/android"
  }
  stages {
    stage('Checkout') {
      steps {
        slackSend (color: '#0000FF', message: "*Started*\n${env.JOB_NAME} [${env.BUILD_NUMBER}]'\n${env.BUILD_URL}", channel: 'api-builds', tokenCredentialId: 'cirrent-slack-api')
      }
    }
    stage('Build') {
      steps {
        sh './gradlew clean build'
      }
    }
    stage('Publish') {
      steps {
        sh "find ./"
      }
    }
  }
  post {
    success {
      slackSend (color: '#00FF00', message: "*Staging Deployed*\n${env.JOB_NAME} [${env.BUILD_NUMBER}]'\n${env.BUILD_URL}", channel: 'api-builds', tokenCredentialId: 'cirrent-slack-api')
    }
    failure {
      slackSend (color: '#FF0000', message: "*Staging Failed*\n${env.JOB_NAME} [${env.BUILD_NUMBER}]'\n${env.BUILD_URL}", channel: 'api-builds', tokenCredentialId: 'cirrent-slack-api')
    }
  }
}
