cd /home/server/ && rm -rf ./* && tar zxvf /home/app.tgz -C ./ && rm -f /home/app.tgz
JAR="server-2.0.0.jar"
CHECK_URL="http://127.0.0.1:8080"
MAX=100
SECOND=2
PROFILE='production'

is_process_running() { ps -p "$1" &>/dev/null; }

# 停止旧进程
pids=$(jps | awk -v jar="$JAR" '$2==jar {print $1}')
for pid in $pids; do
  echo "STOP PID: $pid"
  kill -15 "$pid"
  while is_process_running "$pid"; do
    echo "Waiting $pid STOP."
    sleep 1
  done
  echo "PID $pid STOPED."
done

# 启动新进程
nohup java -jar "$JAR" --spring.profiles.active="$PROFILE" > "/dev/null" 2>&1 &
echo "$JAR starting."

# 检查启动状态
INDEX=0
while [ $INDEX -lt $MAX ]; do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$CHECK_URL")
  if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "APP up!"
    exit 0
  fi
  echo "HTTP $HTTP_STATUS. Retry."
  sleep $SECOND
  INDEX=$((INDEX + 1))
done

echo "❌ ERROR."
exit 1