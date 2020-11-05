@Library(['jpl@master'])
import com.scrippsnetworks.jpl.PipelineSupport

String giturl = 'git@github.com:discoveryinc-cs/ambassador-auth-service.git'
String application = 'ambassador-auth-service'
String owner = 'paul_witt@discovery.com'

String bitbucket_creds = '6e83116e-4fb6-491a-a2c9-9ac9d51d4765'
String artifactory_creds = 'mcd-artifactory-deployer'
String kubeconfig_creds = 'eks-admin-kubeconfig'

def common = new PipelineSupport()

node('eks-cluster') {
    container('eks-cluster') {
        stage('Setup') {
            sh 'rm -rf *'

            env.JPL_DOCKER_ENV = "prod"

            println "Git URL: " + giturl
            println "Git Branch: " + params.BRANCH_NAME

            git branch: params.BRANCH_NAME, url: giturl, credentialsId: github_creds

            try {
                env.GIT_COMMIT = common.gitCommit()
                sh 'echo Git Commit: $GIT_COMMIT'
            } catch(e) {
                common.notifyOnFailure(e, owner, application)
                throw e
            }
        }

        stage('Build Docker') {

            try {
                env.BASE_VERSION = common.getApplicationVersion("version.txt")
                env.HASH = 'b' + env.BUILD_NUMBER
                env.APPLICATION_VERSION = env.BASE_VERSION + '-' + env.HASH

                sh 'echo Application Version: v$APPLICATION_VERSION'
                sh 'echo Version Hash: $HASH'
            } catch(e) {
                common.notifyOnFailure(e, owner, application)
                throw e
            }

            try {
                common.buildImage('prod', '.', 'package/docker-params.yaml', env.APPLICATION_VERSION)
                sh 'docker images'
            } catch(e) {
                common.notifyOnFailure(e, owner, application)
                throw e
            }

        }
    }
}
