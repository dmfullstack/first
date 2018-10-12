pipeline {
    // run on jenkins nodes tha has java 8 label
    agent any
    // global env variables
    environment {
        EMAIL_RECIPIENTS = 'dineshmetkari@gmail.com'
    }
    stages {

        stage('Build with unit testing') {
            steps {
                // Run the maven build
                script {
                    // Get the Maven tool.
                    // ** NOTE: This 'M3' Maven tool must be configured
                    // **       in the global configuration.
                    echo 'Pulling...' + env.BRANCH_NAME
                    //def mvnHome = tool 'Maven3'
                    if (isUnix()) {
                        def targetVersion = getDevVersion()
                        print 'target build version...'
                        print targetVersion
                        //sh "'${mvnHome}/bin/mvn' -Dintegration-tests.skip=true -Dbuild.number=${targetVersion} clean package"
                        sh "'mvn' -Dintegration-tests.skip=true -Dbuild.number=${targetVersion} clean package"
                        def pom = readMavenPom file: 'pom.xml'
                        // get the current development version
                        developmentArtifactVersion = "${pom.version}-${targetVersion}"
                        print pom.version
                        // execute the unit testing and collect the reports
                        // junit '**//*target/surefire-reports/TEST-*.xml'
                        archive 'target*//*.jar'
                    }
                }

            }
        }
        stage('Integration tests') {
            // Run integration test
            steps {
                script {
                  print 'Integration test is skipped'
                }
            }
        }

        stage('Development deploy approval and deployment') {
            steps {
                script {
                    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        timeout(time: 12, unit: 'MINUTES') {
                            // you can use the commented line if u have specific user group who CAN ONLY approve
                            //input message:'Approve deployment?', submitter: 'it-ops'
                            input message: 'Approve deployment?'
                        }
                        timeout(time: 12, unit: 'MINUTES') {
                            //
                            if (developmentArtifactVersion != null && !developmentArtifactVersion.isEmpty()) {
                                // replace it with your application name or make it easily loaded from pom.xml
                                def jarName = "application-${developmentArtifactVersion}.jar"
                                echo "the application is deploying ${jarName}"
                                // NOTE : CREATE your deployemnt JOB, where it can take parameters whoch is the jar name to fetch from jenkins workspace
                                //build job: 'ApplicationToDev', parameters: [[$class: 'StringParameterValue', name: 'jarName', value: jarName]]
                                sh "'mvn' wildfly:deploy"
                                echo 'the application is deployed !'
                            } else {
                                error 'the application is not  deployed as development version is null!'
                            }

                        }
                    }
                }
            }
        }
        stage('Release and publish artifact') {
            when {
                // check if branch is master
                branch 'master'
            }
            steps {
                // create the release version then create a tage with it , then push to nexus releases the released jar
                script {
                    //def mvnHome = tool 'Maven 3.3.9' //
                    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        def v = getReleaseVersion()
                        releasedVersion = v;
                        if (v) {
                            echo "Building version ${v} - so released version is ${releasedVersion}"
                        }
                        // jenkins user credentials ID which is transparent to the user and password change
                        //sshagent(['0000000-3b5a-454e-a8e6-c6b6114d36000']) {
                       //sshagent(['dmfullstack']) {    

                       //TODO CHECKOUT SSHAGENT    
                       // sshagent (credentials: ['dmfullstack']) {     
                       //     sh "git tag -f v${v}"
                        //    sh "git push -f --tags"
                       // }
                        //sh "'${mvnHome}/bin/mvn' -Dmaven.test.skip=true  versions:set  -DgenerateBackupPoms=false -DnewVersion=${v}"
                        //sh "'${mvnHome}/bin/mvn' -Dmaven.test.skip=true clean deploy"

                       sh "'mvn' -Dmaven.test.skip=true  versions:set  -DgenerateBackupPoms=false -DnewVersion=${v}"
                        sh "'mvn' -Dmaven.test.skip=true clean deploy"

                    } else {
                        error "Release is not possible. as build is not successful"
                    }
                }
            }
        }
        stage('Deploy to Acceptance') {
            when {
                // check if branch is master
                branch 'master'
            }
            steps {
                script {
                    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        timeout(time: 12, unit: 'MINUTES') {
                            //input message:'Approve deployment?', submitter: 'it-ops'
                            input message: 'Approve deployment to UAT?'
                        }
                        timeout(time: 12, unit: 'MINUTES') {
                            //  deployment job which will take the relasesed version
                            if (releasedVersion != null && !releasedVersion.isEmpty()) {
                                // make the applciation name for the jar configurable
                                def jarName = "application-${releasedVersion}.war"
                                echo "the application is deploying ${jarName}"
                                // NOTE : DO NOT FORGET to create your UAT deployment jar , check Job AlertManagerToUAT in Jenkins for reference
                                // the deployemnt should be based into Nexus repo
                                //build job: 'AApplicationToACC', parameters: [[$class: 'StringParameterValue', name: 'jarName', value: jarName], [$class: 'StringParameterValue', name: 'appVersion', value: releasedVersion]]
                                sh "'mvn' wildfly:deploy"
                                echo 'the application is deployed !'
                            } else {
                                error 'the application is not  deployed as released version is null!'
                            }

                        }
                    }
                }
            }
        }
        stage('ACC E2E tests') {
            when {
                // check if branch is master
                branch 'master'
            }
            steps {
                script {
                  echo 'ACC E2E test is skipped'
                }
            }
        }
    }
    post {
        // Always runs. And it runs before any of the other post conditions.
        always {
            // Let's wipe out the workspace before we finish!
            deleteDir()
        }
        success {
            sendEmail("Successful");
        }
        unstable {
            sendEmail("Unstable");
        }
        failure {
            sendEmail("Failed");
        }
    }

// The options directive is for configuration that applies to the whole job.
    options {
        // For example, we'd like to make sure we only keep 10 builds at a time, so
        // we don't fill up our storage!
        buildDiscarder(logRotator(numToKeepStr: '5'))

        // And we'd really like to be sure that this build doesn't hang forever, so
        // let's time it out after an hour.
        timeout(time: 25, unit: 'MINUTES')
    }

}
def developmentArtifactVersion = ''
def releasedVersion = ''
// get change log to be send over the mail
@NonCPS
def getChangeString() {
    MAX_MSG_LEN = 100
    def changeString = ""

    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            changeString += " - ${truncated_msg} [${entry.author}] Author is empty\n"
        }
    }

    if (!changeString) {
        changeString = " - No new changes"
    }
    return changeString
}

def sendEmail(status) {
    mail(
            to: "$EMAIL_RECIPIENTS",
            subject: "Build $BUILD_NUMBER - " + status + " (${currentBuild.fullDisplayName})",
            body: "Changes:\n " + getChangeString() + "\n\n Check console output at: $BUILD_URL/console" + "\n")
}

def getDevVersion() {
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber;
    if (gitCommit == null) {
        versionNumber = env.BUILD_NUMBER;
    } else {
        versionNumber = gitCommit.take(8);
    }
    print 'build  versions...'
    print versionNumber
    return versionNumber
}

def getReleaseVersion() {
    def pom = readMavenPom file: 'pom.xml'
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber;
    if (gitCommit == null) {
        versionNumber = env.BUILD_NUMBER;
    } else {
        versionNumber = gitCommit.take(8);
    }
    return pom.version.replace("-SNAPSHOT", ".${versionNumber}")
}

// if you want parallel execution , check below :
/* stage('Quality Gate(Integration Tests and Sonar Scan)') {
           // Run the maven build
           steps {
               parallel(
                       IntegrationTest: {
                           script {
                               def mvnHome = tool 'Maven 3.3.9'
                               if (isUnix()) {
                                   sh "'${mvnHome}/bin/mvn'  verify -Dunit-tests.skip=true"
                               } else {
                                   bat(/"${mvnHome}\bin\mvn" verify -Dunit-tests.skip=true/)
                               }

                           }
                       },
                       SonarCheck: {
                           script {
                               def mvnHome = tool 'Maven 3.3.9'
                               withSonarQubeEnv {
                                   // sh "'${mvnHome}/bin/mvn'  verify sonar:sonar -Dsonar.host.url=http://bicsjava.bc/sonar/ -Dmaven.test.failure.ignore=true"
                                   sh "'${mvnHome}/bin/mvn'  verify sonar:sonar -Dmaven.test.failure.ignore=true"
                               }
                           }
                       },
                       failFast: true)
           }
       }*/
