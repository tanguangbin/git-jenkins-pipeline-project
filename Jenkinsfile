pipeline {
    environment {
        /**
          * k8s相关变量
        */
        //端口号 自动从 application-xxx.yml文件中读取
//         PORT_PLACEHOLDER='8081'
//         NODEPORTS_PLACEHOLDER='30011'
        //traefik或 Nginx 负载地址
        LOADBALANCER_PLACEHOLDER='www.baidu.com'
        //docker镜像名称+版本号
        IMAGE_PLACEHOLDER="IMAGE_PLACEHOLDER"
        //k8s或docker中应用的名称 自动从 application-xxx.yml文件中读取
//         CONTAINER_NAME = 'test-container'
        // Kubernetes Deployment Template 文件路径
        // 用于存储 Kubernetes 部署的模板文件，在其中占位符会被替换为实际的 Docker 镜像标签
        K8S_TEMPLATE_NAME = 'k8s-deployment-template.yaml'
        // Kubernetes Deployment 文件路径
        // 生成的实际用于部署的文件，包含替换后的 Docker 镜像标签
        K8S_DEPLOYMENT_NAME = 'k8s-deployment.yaml'


        // Docker 镜像仓库地址
        REGISTRY = 'tanguangbin1980/test'
        // Docker 仓库凭据ID
        DOCKER_REGISTRY_CREDENTIAL_ID = 'dockerhub'
        // Docker 镜像名称变量（动态生成）
        DOCKER_IMAGE = ''

        /**
          * GIT相关变量
        */
        //git地址
        GIT_URL = "https://github.com/tanguangbin/"
        GIT_PUSH_URL="github.com"
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

        /**
         * elasticsearch 配置
        */
        ES_BASE_DIR = "elasticsearch"
        ELASTIC_CREDENTIALS_ID = "ELASTIC_CREDENTIALS"


        /**
        * SONAR 配置
        */
        SONAR_URL = "http://34.201.116.83:9000"
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

                    git branch: branch, url: "${GIT_URL}/${GIT_REPO_NAME}.git"
                }
            }
        }

        stage('Build with Maven') {
            steps {
                script {
                    // 根据环境设置不同的Maven命令
                    //def mavenGoal = params.ENVIRONMENT == 'prod' ? 'clean package -Pproduction' : 'clean package'
                    //sh "mvn ${mavenGoal}"
                    def profile = params.ENVIRONMENT
                    sh "mvn clean package -P${profile}"
                }
            }
        }

        stage('Setup ES Environment') {
            steps {
                script {

                    // 读取相应环境的配置文件
                     def config = readYaml file: "./target/classes/application-${params.ENVIRONMENT}.yml"
                     echo "开始读取文件 ${"./target/classes/application-${params.ENVIRONMENT}.yml"}"
                    // 输出读取到的内容
                    echo "YAML Content: ${config}"
                    // 读取项目名称，并赋值给环境变量 CONTAINER_NAME
                    env.CONTAINER_NAME = config.spring.application.name ?: "default-container-name"
                    echo "读取 CONTAINER_NAME: ${env.CONTAINER_NAME}"

                    //项目端口号，也是镜像的端口号  从 application-xxx.yml文件中读取
                    env.PORT_PLACEHOLDER = "${config.server.port}"
                    echo "读取 PORT_PLACEHOLDER: ${env.PORT_PLACEHOLDER}"

                    //k8s nodeport端口号  从 application-xxx.yml文件中读取
                    env.NODEPORTS_PLACEHOLDER = "${config.server.nodeport}"
                    echo "读取 NODEPORTS_PLACEHOLDER: ${env.NODEPORTS_PLACEHOLDER}"

                    // 动态设置 ES_HOST
                    env.ES_HOST = "${config.elasticsearch?.scheme}://${config.elasticsearch?.host}:${config.elasticsearch?.port}"
                    echo "Elasticsearch Host: ${env.ES_HOST}"
                }
            }
        }

        // stage('Sonarqube Static Code Analysis') {
        //   environment {
        //     SONAR_URL = SONAR_URL
        //   }
        //   steps {
        //     withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
        //       sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}'
        //     }
        //   }
        // }





        stage('Check and Create Elasticsearch Indices') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: ELASTIC_CREDENTIALS_ID, passwordVariable: 'ES_PASSWORD', usernameVariable: 'ES_USERNAME')]) {
                        def files = findFiles(glob: "${ES_BASE_DIR}**/create/*.json")
                        files.each { file ->
                            // 去掉 .json 扩展名，得到索引名称
                            def indexName = file.name.replace('.json', '')
                            echo "Processing index: ${indexName}"

                           def checkIndexExists = sh(
                               script: """
                               curl -s -u "$ES_USERNAME:$ES_PASSWORD" "${env.ES_HOST}/_cluster/state?filter_path=metadata.indices.${indexName}" | grep ${indexName}
                               """,
                               returnStatus: true
                           )
                           echo "checkIndexExists ======: ${checkIndexExists}"
                           // 0 找到索引 1 没有找到索引
                           if (checkIndexExists == 0) {
                               echo "Index ${indexName} already exists. Skipping creation."
                           } else {// 1 没有找到索引
                               echo "Creating index: ${indexName}"
                               def response = sh(script: """
                               curl -X PUT -u "$ES_USERNAME:$ES_PASSWORD" "${env.ES_HOST}/${indexName}" -H 'Content-Type: application/json' -d @${file.path}
                               """, returnStdout: true).trim()

                               echo "Index creation response for ${indexName}: ${response}"
                           }

                        }
                    }
                }
            }
        }

       stage('Update Elasticsearch Mappings') {
           steps {
               script {
                    withCredentials([usernamePassword(credentialsId: ELASTIC_CREDENTIALS_ID, passwordVariable: 'ES_PASSWORD', usernameVariable: 'ES_USERNAME')]) {
                        // 查找 update 文件夹下的所有 JSON 文件
                        def files = findFiles(glob: "${ES_BASE_DIR}**/update/*.json")

                        files.each { file ->
                           // 根据文件名提取索引名称和更新名称
                           def (indexName, updateName) = file.name.replace('.json', '').split('-')
                           echo "Processing update for index: ${indexName} with update: ${updateName}"
                           def checkIndexExists = sh(
                               script: """
                               curl -s -u "$ES_USERNAME:$ES_PASSWORD" "${env.ES_HOST}/_cluster/state?filter_path=metadata.indices.${indexName}" | grep ${indexName}
                               """,
                               returnStatus: true
                           )
                           echo "checkIndexExists ======: ${checkIndexExists}"
                           // 0 找到索引 1 没有找到索引
                           if (checkIndexExists == 0) {
                              // 索引存在，更新映射
                              echo "Updating index: ${indexName} with file: ${file.name}"
                              def response = sh(script: """
                              curl -X PUT -u "$ES_USERNAME:$ES_PASSWORD" "${env.ES_HOST}/${indexName}/_mapping" -H 'Content-Type: application/json' -d @${file.path}
                              """, returnStdout: true).trim()
                              echo "Mapping update response for ${indexName}: ${response}"
                           } else {
                               // 索引不存在，输出提示信息并跳过更新
                               echo "Index ${indexName} does not exist. Skipping mapping update."
                           }

                        }
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
//                     docker.withRegistry('', DOCKER_REGISTRY_CREDENTIAL_ID) {
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

                        sed 's|IMAGE_PLACEHOLDER|${imageName}|g; s|CONTAINER_NAME|${env.CONTAINER_NAME}|g; s|LOADBALANCER_PLACEHOLDER|${env.LOADBALANCER_PLACEHOLDER}|g; s|PORT_PLACEHOLDER|${env.PORT_PLACEHOLDER}|g; s|NODEPORTS_PLACEHOLDER|${env.NODEPORTS_PLACEHOLDER}|g; s|value: \"dev\"|value: \"${springProfile}\"|g' ${K8S_TEMPLATE_NAME} > ${K8S_DEPLOYMENT_NAME}
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
                            git push -f https://${GITHUB_TOKEN}@${GIT_PUSH_URL}/${GIT_USER_NAME}/${GIT_REPO_NAME} ${TEMP_BRANCH}

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
    }
}