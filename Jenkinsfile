pipeline {
    environment {


        /**
          * k8s相关变量
        */
        //端口号
        PORT_PLACEHOLDER='8081'
        NODEPORTS_PLACEHOLDER='30011'
        //traefik或 Nginx 负载地址
        LOADBALANCER_PLACEHOLDER='www.baidu.com'
        //docker镜像名称+版本号
        IMAGE_PLACEHOLDER="IMAGE_PLACEHOLDER"
        //k8s或docker中应用的名称
        CONTAINER_NAME = 'test-container'
        // Kubernetes Deployment Template 文件路径
        // 用于存储 Kubernetes 部署的模板文件，在其中占位符会被替换为实际的 Docker 镜像标签
        K8S_TEMPLATE_NAME = 'k8s-deployment-template.yaml'
        // Kubernetes Deployment 文件路径
        // 生成的实际用于部署的文件，包含替换后的 Docker 镜像标签
        K8S_DEPLOYMENT_NAME = 'k8s-deployment.yaml'


        // Docker 镜像仓库地址
        REGISTRY = 'tanguangbin1980/test'
        // Docker 仓库凭据ID
        REGISTRY_CREDENTIAL = 'dockerhub'
        // Docker 镜像名称变量（动态生成）
        DOCKER_IMAGE = ''

        /**
          * GIT相关变量
        */
        // GitHub 用户名
        GIT_USER_NAME = "tanguangbin"
        // GitHub 凭据ID，用于认证推送
        GITHUB_CREDENTIALS_ID = 'github'
        // GitHub 仓库名称
        GIT_REPO_NAME = "git-jenkins-pipeline-project"
        GIT_USER_EMAIL="ADMIN@gmail.com"
        GIT_USERNAME="ADMIN"


        /**
          * jenkins相关变量
        */
        //Jenkins Pipeline 脚本用于自动化构建和部署过程。它根据不同环境（开发、测试、生产）克隆相应的 Git 分支，
        //构建 Docker 镜像，并更新 Kubernetes 配置文件。特别地，将 k8s-deployment.yaml 文件的更新提交到临时分支
        //ARGO-CD-FETCH-BRANCH，该分支专门供 Argo CD 使用，以便部署到 Kubernetes 集群中。这种做法避免了对开发主分支的干扰。
        TEMP_BRANCH="ARGO-CD-FETCH-BRANCH"


        TRIVY_REPORT_PATH = 'trivy-report.json'  // Trivy 报告文件路径

    }

    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'test', 'prod'], description: '选择部署的环境')
    }

    tools {
        maven 'mvn' // 指定Maven的安装名称
    }

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    // 根据选择的环境动态选择分支
                    def branch = params.ENVIRONMENT == 'prod' ? 'main' : params.ENVIRONMENT
//                     if (params.ENVIRONMENT == 'prod') {
//                         branch = 'main'
//                     } else if (params.ENVIRONMENT == 'test') {
//                         branch = 'test'
//                     } else if (params.ENVIRONMENT == 'dev') {
//                         branch = 'dev'
//                     }
                    echo "当前选择的分支为： ${branch}  项目名为： ${GIT_REPO_NAME}.git"

                    git branch: branch, url: "https://github.com/tanguangbin/${GIT_REPO_NAME}.git"
                }
            }
        }

//         stage('Build with Maven') {
//             steps {
//                 script {
//                     // 根据环境设置不同的Maven命令
// //                     def mavenGoal = params.ENVIRONMENT == 'prod' ? 'clean package -Pproduction' : 'clean package'
// //                     sh "mvn ${mavenGoal}"
//                     def profile = params.ENVIRONMENT
//                     sh "mvn clean package -P${profile}"
//                 }
//             }
//         }

        // stage('Sonarqube Static Code Analysis') {
        //   environment {
        //     SONAR_URL = "http://34.201.116.83:9000"
        //   }
        //   steps {
        //     withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
        //       sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}'
        //     }
        //   }
        // }

        stage('Setup ES Environment') {
            steps {
                script {
                    // 读取相应环境的配置文件
                    def config = readYaml file: "./target/classes/application-${params.ENVIRONMENT}.yml"

                    // 动态设置 ES_HOST
                    env.ES_HOST = "http://" + config.es.host
                    echo "Elasticsearch Host: ${env.ES_HOST}"
                }
            }
        }



        stage('Create Elasticsearch Indices') {
            steps {
                script {
//                     def basePath = "elasticsearch/${params.ENVIRONMENT}/"
                    sh 'ls -al'
                    def basePath = "elasticsearch/"
                    def createFiles = findFiles(glob: "${basePath}**/create/*.json")
                    echo "====================findFiles : ${createFiles}"
                    createFiles.each { file ->
                        def indexName = file.path.split('/')[2]
                        echo "Creating index: ${indexName}"

                        def response = sh(script: """
                        curl -X PUT "${env.ES_HOST}/${indexName}" -H 'Content-Type: application/json' -d @${file.path}
                        """, returnStdout: true).trim()

                        echo "Index creation response for ${indexName}: ${response}"
                    }
                }
            }
        }

        stage('Update Elasticsearch Mappings') {
            steps {
                script {
                    def basePath = "elasticsearch/${params.ENVIRONMENT}/"

                    def updateFiles = findFiles(glob: "${basePath}*/update/*.json")
                    updateFiles.each { file ->
                        def indexName = file.path.split('/')[2]
                        echo "Updating index: ${indexName} with file: ${file.name}"

                        def response = sh(script: """
                        curl -X PUT "${env.ES_HOST}/${indexName}/_mapping" -H 'Content-Type: application/json' -d @${file.path}
                        """, returnStdout: true).trim()

                        echo "Mapping update response for ${indexName}: ${response}"
                    }
                }
            }
        }


        stage('Build Docker Image') {
            steps {
                script {
                    env.DOCKER_IMAGE_NAME = "${REGISTRY}-${params.ENVIRONMENT}:${env.BUILD_NUMBER}"
                    DOCKER_IMAGE = docker.build("${env.DOCKER_IMAGE_NAME}")
                }
            }
        }

//         stage('Scan Docker Image with Trivy') {
//             steps {
//                 script {
//                     // 安装 Trivy
//                     sh '''
//                     if ! command -v trivy &> /dev/null; then
//                         echo "Installing Trivy..."
//                         curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
//                     fi
//                     '''
//
//                     // 使用 Trivy 扫描 Docker 镜像
// //                     sh "trivy image ${env.DOCKER_IMAGE_NAME} || true"
//                     // 使用 Trivy 扫描 Docker 镜像，并将结果输出到指定文件
//                     sh "trivy image -f json -o ${TRIVY_REPORT_PATH} ${DOCKER_IMAGE} || true"
//                     echo "Trivy scan completed. Report saved to ${TRIVY_REPORT_PATH}"
//                 }
//             }
//         }

//         stage('Push Docker Image') {
//             steps {
//                 script {
//                     docker.withRegistry('', REGISTRY_CREDENTIAL) {
//                         DOCKER_IMAGE.push()
//                     }
//                 }
//             }
//         }

        stage('Remove Unused Docker Image') {
            steps {

                sh "docker rmi ${env.DOCKER_IMAGE_NAME}"
            }
        }

        stage('Update k8s YAML') {
            steps {
                script {
                    def imageName = "${env.DOCKER_IMAGE_NAME}"
                    def springProfile = params.ENVIRONMENT
                    sh """
                        # 方式1：docker run -e SPRING_PROFILES_ACTIVE=dev -p 8081:8081 my-application
                        # 方式2： kubectl apply -f xxx 会自动加载 SPRING_PROFILES_ACTIVE 对应的值dev test prod
                        #K8S 文件相关变量
                        #PORT_PLACEHOLDER='8081'
                        #NODEPORT_PLACEHOLDER='30011'
                        #LOADBALANCER_PLACEHOLDER='www.baidu.com'
                        #IMAGE_PLACEHOLDER="IMAGE_PLACEHOLDER"
                        #CONTAINER_NAME

                        sed 's|IMAGE_PLACEHOLDER|${imageName}|g; s|CONTAINER_NAME|${CONTAINER_NAME}|g; s|LOADBALANCER_PLACEHOLDER|${LOADBALANCER_PLACEHOLDER}|g; s|PORT_PLACEHOLDER|${PORT_PLACEHOLDER}|g; s|NODEPORTS_PLACEHOLDER|${NODEPORTS_PLACEHOLDER}|g; s|value: \"dev\"|value: \"${springProfile}\"|g' ${K8S_TEMPLATE_NAME} > ${K8S_DEPLOYMENT_NAME}
                        cat ${K8S_DEPLOYMENT_NAME}
                    """
                }
            }
        }

         stage('Update Deployment File') {
            steps {
                withCredentials([string(credentialsId: "${GITHUB_CREDENTIALS_ID}", variable: 'GITHUB_TOKEN')]) {
                    script {
                        sh '''
                            #!/bin/bash
                            git config user.email "${GIT_USER_EMAIL}"
                            git config user.name "${GIT_USERNAME}"

                            # 确保本地仓库是最新的
                            git fetch origin

                            # 删除本地临时分支，如果存在
                            git branch -D ${TEMP_BRANCH} || echo "No local branch ${TEMP_BRANCH} to delete"


                            # 备份 k8s-deployment.yaml 文件，避免冲突
                            if [ -f "${K8S_DEPLOYMENT_NAME}" ]; then
                                mv ${K8S_DEPLOYMENT_NAME} ${K8S_DEPLOYMENT_NAME}.backup
                            fi

                            # 基于 lite 创建新的临时分支
                            git checkout -b ${TEMP_BRANCH} origin/lite

                            # 恢复 k8s-deployment.yaml 文件
                            if [ -f "${K8S_DEPLOYMENT_NAME}.backup" ]; then
                                mv ${K8S_DEPLOYMENT_NAME}.backup ${K8S_DEPLOYMENT_NAME}
                            fi



                            # 提交 k8s-deployment.yaml 文件
                            git add ${K8S_DEPLOYMENT_NAME}
                            git commit -m "Temporary commit for deployment image to version ${BUILD_NUMBER}"
                            git push -f https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} ${TEMP_BRANCH}

                            # 返回原始分支
                            #git checkout ${params.ENVIRONMENT == 'prod' ? 'main' : params.ENVIRONMENT}
                        '''
                    }
                }
            }
         }


//          stage('Run Docker Container for Testing') {
//             steps {
//                 script {
//                     def dockerImageTag = "${env.DOCKER_IMAGE_NAME}"
//
//                     // 停止并移除之前的容器（如果存在）
//                     sh "docker rm -f ${CONTAINER_NAME} || true"
//
//                     // 运行容器并指定 Spring Profile
//                     sh """
//                     docker run --name ${CONTAINER_NAME} -d \
//                         -e SPRING_PROFILES_ACTIVE=${params.ENVIRONMENT} \
//                         -e SERVER_PORT=${PORT_PLACEHOLDER} \
//                         -p ${PORT_PLACEHOLDER}:${PORT_PLACEHOLDER} \
//                         ${dockerImageTag}
//                     """
//
//                     // 测试应用程序
// //                     sleep 10 // 等待应用程序启动
// //                     sh "curl -f http://localhost:8081/actuator/health || echo 'Application failed to start'"
//
//                     // 停止并移除测试容器
// //                     sh "docker rm -f ${CONTAINER_NAME}"
//                 }
//             }
//         }


//         stage('Update Deployment File') {
//             steps {
//                 withCredentials([string(credentialsId: "${GITHUB_CREDENTIALS_ID}", variable: 'GITHUB_TOKEN')]) {
//                     sh """
//                         git config user.email "test@gmail.com"
//                         git config user.name "Andy Tan"
//
//                          # 强制添加被忽略的文件
//                          # 由于 k8s-deployment.yaml 文件在构建过程中被自动生成且可能每次构建都会改变，
//                          # 将其添加到 .gitignore 中避免手动冲突。但有时我们仍然需要将它推送到远程仓库，
//                          # 因此这里使用 git add -f 强制添加此文件。
//                          #git add -f k8s-deployment.yaml
//                          #git commit -m "Update deployment image to version ${BUILD_NUMBER}"
//                          #git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:${params.ENVIRONMENT}
//
//                         # 检查临时分支是否存在并切换
//                         if git rev-parse --verify ${TEMP_BRANCH}; then
//                             #echo "Switching to existing branch ${TEMP_BRANCH}"
//                             #git stash
//                             #git checkout ${TEMP_BRANCH}
//                             #git stash pop
//                             #产出docker中本地的git分支，避免k8s-deployment.yaml冲突
//                             git branch -D ${TEMP_BRANCH}
//                             echo "wait for 2 second for deleting local TEMP_BRANCH"
//                             sleep 2
//
//                         #else
//                         #    echo "Creating new branch ${TEMP_BRANCH}"
//                         #    git checkout -b ${TEMP_BRANCH}
//                         fi
//
//
//                         # 等待 5 秒
//
//                         git checkout -b ${TEMP_BRANCH}
//                         #git pull
//                         echo "wait for 5 second for checkout new TEMP_BRANCH"
//                         sleep 5
//                         # 提交临时文件
//                         git add ${K8S_DEPLOYMENT_NAME}
//                         git commit -m "Temporary commit for deployment image to version ${BUILD_NUMBER}"
//                         git push -f https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} $TEMP_BRANCH
//
//                         #产出docker中本地的git分支，避免冲突
//                         #git branch -D ${TEMP_BRANCH}
//
//                         # 如果需要 返回原始分支
//                         # 根据选择的环境动态选择分支
//                         #def branch = params.ENVIRONMENT == 'prod' ? 'main' : params.ENVIRONMENT
//                         #git checkout branch
//
//                     """
//                 }
//             }
//         }

    }
}