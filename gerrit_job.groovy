pipelineJob("fraud-detector") {
    logRotator(-1, 10, -1, -1)


    triggers {
        githubPush()
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/kenych/fraud-detector')
                    }
                    branch('master')
                }
            }
            scriptPath('Jenkinsfile')
        }
    }
}

