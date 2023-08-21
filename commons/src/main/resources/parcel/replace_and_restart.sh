#!/bin/bash
# 抓取目录信息
tar_dir=$(pwd)
cd ~
src_dir=$(pwd)
cd $tar_dir

# 获取源文件
src_jar=$1
service_name=%s
if [ -z "${src_jar}" ]; then
  # 检查是否存在源文件
  src_jar=$(ls ~ | grep $service_name)
  if [ -z "${src_jar}" ]; then
    echo "[${src_dir}] 找不到源文件"
    exit 1
  fi
  # 判断是否存在多个源文件
  jar_num=$(ls ~ | grep -c $service_name)
  if [ $jar_num -gt 1 ]; then
    echo -e "[${src_dir}] 找到 ${jar_num} 个源文件:\n${src_jar}"
    echo "执行脚本时，可指定文件名"
    exit 1
  fi
else
  # 判断 src_jar 是否存在
  if [ $(ls ~ | grep -c ${src_jar}) -eq 0 ]; then
    echo "源文件[${src_dir}/${src_jar}]不存在"
    exit 1
  fi
fi
echo "源文件: [${src_dir}/${src_jar}] 目标文件: [${tar_dir}/${service_name}.jar]"
mv ${src_dir}/${src_jar} ${tar_dir}/${service_name}.jar
chmod +x ${tar_dir}/${service_name}.jar
systemctl restart $service_name
echo "${service_name} 服务覆盖并重启完成, 三秒后打印日志"
sleep 3
tail -f -n 5 ${tar_dir}/logs/${service_name}.log
