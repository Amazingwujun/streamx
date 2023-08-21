#!/bin/bash
service_name=%s
systemctl stop $service_name
tail -n 30 -f ./logs/${service_name}.log
