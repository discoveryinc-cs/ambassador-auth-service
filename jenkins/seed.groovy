String basepath = '/Sandbox/automation/ambassador-auth-service'
String giturl = 'git@github.com:discoveryinc-cs/ambassador-auth-service.git'
String git_reader = '6e83116e-4fb6-491a-a2c9-9ac9d51d4765'
String github_api = 'github-api'

job("$basepath/00.Seed") {
    label('eks-cluster')
    logRotator {
        numToKeep(10)
    }
    scm {
        git {
            remote {
                credentials(git_reader)
                url(giturl)
            }
            branch('master')
        }
    }
    wrappers { preBuildCleanup() }
    steps {
        dsl {
            external('jenkins/seed.groovy')
            lookupStrategy('SEED_JOB')
            removeAction('DISABLE')
        }
    }
}

pipelineJob("$basepath/01.Build") {
    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }
    parameters {
        stringParam('BRANCH_NAME', 'master')
    }
    throttleConcurrentBuilds {
        maxTotal(1)
    }
    definition {
        cpsScm {
            scm {
                git{
                    remote {
                        url(giturl)
                        credentials(github_api)
                    }
                    branches('${BRANCH_NAME}')
                    extensions { }
                }
                scriptPath('jenkins/pipeline.groovy')
            }
        }
    }
}
