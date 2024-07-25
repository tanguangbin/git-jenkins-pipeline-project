pipeline {
    environment {
        // Docker 镜像仓库地址
        REGISTRY = 'tanguangbin1980/test'

        // GitHub 仓库名称
        GIT_REPO_NAME = "git-jenkins-pipeline-project"

        // GitHub 用户名
        GIT_USER_NAME = "tanguangbin"

        // Docker 仓库凭据ID
        REGISTRY_CREDENTIAL = 'dockerhub'

        // Docker 镜像名称变量（动态生成）
        DOCKER_IMAGE = ''

        // GitHub 凭据ID，用于认证推送
        GITHUB_CREDENTIALS_ID = 'github'

        // Kubernetes Deployment Template 文件路径
        // 用于存储 Kubernetes 部署的模板文件，在其中占位符会被替换为实际的 Docker 镜像标签
        K8S_TEMPLATE_PATH = 'k8s-deployment-template.yaml'

        // Kubernetes Deployment 文件路径
        // 生成的实际用于部署的文件，包含替换后的 Docker 镜像标签
        K8S_DEPLOYMENT_PATH = 'k8s-deployment.yaml'

        //Jenkins Pipeline 脚本用于自动化构建和部署过程。它根据不同环境（开发、测试、生产）克隆相应的 Git 分支，
        //构建 Docker 镜像，并更新 Kubernetes 配置文件。特别地，将 k8s-deployment.yaml 文件的更新提交到临时分支
        //ARGO-CD-FETCH-BRANCH，该分支专门供 Argo CD 使用，以便部署到 Kubernetes 集群中。这种做法避免了对开发主分支的干扰。
        TEMP_BRANCH="ARGO-CD-FETCH-BRANCH"

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

                    git branch: branch, url: "https://github.com/tanguangbin/${GIT_REPO_NAME}.git"
                }
            }
        }

        stage('Build with Maven') {
            steps {
                script {
                    // 根据环境设置不同的Maven命令
                    def mavenGoal = params.ENVIRONMENT == 'prod' ? 'clean package -Pproduction' : 'clean package'
                    sh "mvn ${mavenGoal}"
                }
            }
        }

        // stage('Static Code Analysis') {
        //   environment {
        //     SONAR_URL = "http://34.201.116.83:9000"
        //   }
        //   steps {
        //     withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
        //       sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}'
        //     }
        //   }
        // }

        stage('Build Docker Image') {
            steps {
                script {
                    DOCKER_IMAGE = docker.build("${REGISTRY}:${env.BUILD_NUMBER}")
                }
            }
        }

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
                sh "docker rmi ${REGISTRY}:${env.BUILD_NUMBER}"
            }
        }

        stage('Update k8s YAML') {
            steps {
                script {
                    def imageName = "${REGISTRY}:${env.BUILD_NUMBER}"
                    sh """
                    pwd
                    ls
                    sed 's|IMAGE_PLACEHOLDER|${imageName}|g' ${K8S_TEMPLATE_PATH} > ${K8S_DEPLOYMENT_PATH}
                    cat k8s-deployment.yaml
                    """
                }
            }
        }

        stage('Update Deployment File') {
            steps {
                withCredentials([string(credentialsId: "${GITHUB_CREDENTIALS_ID}", variable: 'GITHUB_TOKEN')]) {
                    sh """
                        git config user.email "test@gmail.com"
                        git config user.name "Andy Tan"
//                         BUILD_NUMBER=${BUILD_NUMBER}
//                         # 强制添加被忽略的文件
//                         # 由于 k8s-deployment.yaml 文件在构建过程中被自动生成且可能每次构建都会改变，
//                         # 将其添加到 .gitignore 中避免手动冲突。但有时我们仍然需要将它推送到远程仓库，
//                         # 因此这里使用 git add -f 强制添加此文件。
//                         git add -f k8s-deployment.yaml
//                         git commit -m "Update deployment image to version ${BUILD_NUMBER}"
//                         git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:${params.ENVIRONMENT}

                     # 创建临时分支

                    git checkout -b $TEMP_BRANCH

                    # 提交临时文件
                    git add ${K8S_DEPLOYMENT_PATH}
                    git commit -m "Temporary commit for deployment image to version ${BUILD_NUMBER}"
                    git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} $TEMP_BRANCH

                    # 如果需要 返回原始分支
                    // 根据选择的环境动态选择分支
                    def branch = params.ENVIRONMENT == 'prod' ? 'main' : params.ENVIRONMENT
                    git checkout branch

                    """
                }
            }
        }
//          stage('Simulate Run Spring Boot Application') {
//             steps {
//                 script {
//                     echo "Simulating Spring Boot application run..."
//                     sh '''
//                     java -jar target/*.jar &
//                     sleep 30
//                     curl -f http://localhost:8081/actuator/health || echo "Application failed to start"
//                     '''
//                     sh 'pkill -f "java -jar target/*.jar"' // 停止模拟的应用程序
//                 }
//             }
//          }
    }
}