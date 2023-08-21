#!/bin/bash
service_name=%s
final_jar=${service_name}.jar
# 找到当前文件夹下可执行 jar，然后变更它的名字
exist_jar=$(ls ./ |grep *.jar)
if [ ${exist_jar} != $final_jar ]; then
  echo "文件 $exist_jar 将被替换为 $final_jar"
  mv ./$exist_jar ./$final_jar
fi
chmod +x ./${final_jar}
systemctl restart $service_name
echo "$service_name 重启完成, 三秒后打印日志"
sleep 3
tail -f -n 5 ./logs/${service_name}.log
