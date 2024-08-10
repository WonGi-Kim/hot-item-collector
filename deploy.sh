#!/bin/bash

echo ">>> [+] docker-compose up" >> /home/ubuntu/action/deploy.log
docker-compose up -d --build >> /home/ubuntu/action/docker.log
BUILD_JAR=$(ls /home/ubuntu/action/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build 파일명: $JAR_NAME" >> /home/ubuntu/action/deploy.log

echo ">>> build 파일 복사" >> /home/ubuntu/action/deploy.log
DEPLOY_PATH=/home/ubuntu/action/
cp $BUILD_JAR $DEPLOY_PATH

echo ">>> 현재 실행중인 애플리케이션 pid 확인" >> /home/ubuntu/action/deploy.log
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z $CURRENT_PID ]
then
  echo ">>> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ubuntu/action/deploy.log
else
  echo ">>> 현재 구동중인 애플리케이션 종료: kill -15 $CURRENT_PID" >> /home/ubuntu/action/deploy.log
  kill -15 $CURRENT_PID
  sleep 5
fi

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo ">>> DEPLOY_JAR 배포: $DEPLOY_JAR" >> /home/ubuntu/action/deploy.log
nohup java -jar $DEPLOY_JAR --server.port=8080 >> /home/ubuntu/action/deploy.log 2>> /home/ubuntu/action/deploy_err.log &

# 프론트엔드 애플리케이션을 Nginx를 통해 배포
echo ">>> Nginx 설정 파일 복사" >> /home/ubuntu/action/deploy.log
NGINX_CONFIG_PATH=/etc/nginx/sites-available/default
FRONTEND_BUILD_PATH=/home/ubuntu/action/frontend/dist

yes | sudo cp -rf $FRONTEND_BUILD_PATH/* /var/www/html/

# Nginx 설정 파일 수정 (필요한 경우에만)
# sed 명령어를 이용하여 Nginx 설정 파일에서 포트나 경로를 변경

echo ">>> Nginx 재시작" >> /home/ubuntu/action/deploy.log
sudo systemctl restart nginx

echo ">>> 배포 스크립트 종료" >> /home/ubuntu/action/deploy.log
