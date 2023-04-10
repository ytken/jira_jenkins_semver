pipeline {
    agent any

    stages {
        stage('env-var') {
            steps {
                script {
                    env.GIT_HOME = "/app"
                }
            }
        }
        stage('Hello') {
            steps {
                script {
                    def commitHash = sh(returnStdout: true, script: "cd ${env.GIT_HOME} ; git rev-parse --short HEAD").trim()
                    sh"""#!/bin/bash
                        shopt -s expand_aliases
                        
                        pip install pytest
                        
                        mkdir build_\${BUILD_NUMBER}
                        touch build_\${BUILD_NUMBER}/tests.log
                        echo ${commitHash} >> build_\${BUILD_NUMBER}/tests.log
                        pytest ${env.GIT_HOME}/test_capitalize.py 2>&1 | tee -a build_\${BUILD_NUMBER}/tests.log
                        status=\${PIPESTATUS[0]} # status of test_capitalize.py
                        
                        if [ \$status -ne 0 ]; then
                            echo 'ERROR: pytest failed, exiting ...'
                            exit \$status
                        fi
                    """
                }
            }
        }
    }
    post {
        // regression
        failure {
            script {
                echo "Failure actions"
                
                echo "Diff between logs"
                
                def lastSuccesfulBuildId = sh(returnStdout: true, script: "cat ../../jobs/\$JOB_NAME/builds/permalinks | grep lastSuccessfulBuild | sed \'s/lastSuccessfulBuild //\'").trim()
                
                sh"""#!/bin/bash
                    shopt -s expand_aliases
                    diff -u build_${lastSuccesfulBuildId}/tests.log build_\${BUILD_NUMBER}/tests.log | tee build_diffs/\${BUILD_NUMBER}_${lastSuccesfulBuildId}_diff.log
                """
                def gitDiffLog = sh(returnStdout: true, script: "cat build_diffs/\${BUILD_NUMBER}_${lastSuccesfulBuildId}_diff.log").trim()
                
                echo "Diff between commits"
                def successCommit = sh(returnStdout: true, script: "head -n 1 build_${lastSuccesfulBuildId}/tests.log").trim()
                def failureCommit = sh(returnStdout: true, script: "head -n 1 build_\$BUILD_NUMBER/tests.log").trim()
                def gitDiffCommit = sh(returnStdout: true, script: "cd ${env.GIT_HOME} && git diff --submodule=diff ${successCommit} ${failureCommit}").trim()
                
                def descriptionPath = "${JENKINS_HOME}/workspace/${env.JOB_BASE_NAME}/build_${BUILD_NUMBER}/description_file.txt"
                echo descriptionPath
                def issueDescription = """Произошло падение теста\nСсылка на запуск: ${RUN_DISPLAY_URL}\nЛог прохождения теста: ${JENKINS_HOME}/workspace/${env.JOB_BASE_NAME}/build_${BUILD_NUMBER}/tests.log\nРазличия между трассами прохождения успешного и упавшего запуска:\n{code}\n${gitDiffLog}\n{code}\nРазличия между успешным и ошибочных коммитами:\n{code}\n${gitDiffCommit}\n{code}"""
                
                def descriptionFile = new File(descriptionPath)
                descriptionFile.createNewFile()
                descriptionFile.write(issueDescription)
                
                sh """
                    python3 ${env.GIT_HOME}/create_issue.py \"[REGRESS][${env.JOB_BASE_NAME}] Тесты падают на регрессе\" \"a.ovchinnikova\" \"${descriptionPath}\" 
                """
            }
        }
    }
}

