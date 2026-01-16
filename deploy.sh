DOWNLOAD="/home/app.tgz"
DIR="/home/server/"
JAR="server-2.0.0.jar"
CHECK_URL="http://127.0.0.1:8080"
MAX=100
SECOND=2
PROFILE='production'

cd $DIR && rm -rf ./* && tar zxvf $DOWNLOAD -C ./ && rm -f $DOWNLOAD
is_running() { ps -p "$1" &>/dev/null; }
# 停止旧进程
pids=$(jps | awk -v jar="$JAR" '$2==jar {print $1}')
for pid in $pids; do
  echo "STOP $pid"
  kill -15 "$pid"
  while is_running "$pid"; do
    echo "..."
    sleep 1
  done
done
# 启动新进程
nohup java -jar "$JAR" --spring.profiles.active="$PROFILE" > /dev/null 2>&1 &
# 检查启动状态
INDEX=0
while [ $INDEX -lt $MAX ]; do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$CHECK_URL")
  if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "SERVER UP!"
    exit 0
  fi
  echo "$HTTP_STATUS..."
  sleep $SECOND
  INDEX=$((INDEX + 1))
done
# 启动失败
echo "SERVER ERROR."
exit 1