#!/bin/bash
#
# d4s-sa3-toolkit
#
# Authors:
#     Gabriele Giammatteo [gabriele.giammatteo@eng.it]
#
# 2009
# ------------------------------------------------------------------------------
#
#

echo "[_print-sys-info] HOSTNAME = `hostname`"
echo "[_print-sys-info] IP_ADDR = `/sbin/ifconfig eth0 | awk '/inet addr/ {split ($2,A,":"); print A[2]}'`"
echo "[_print-sys-info] DATE = `date`"
echo '[_print-sys-info] output of "cat /proc/cpuinfo" follow'
cat /proc/cpuinfo
echo '[_print-sys-info] output of "cat /proc/meminfo" follow'
cat /proc/meminfo
echo '[_print-sys-info] output of "ps aux" follow'
ps aux
echo '[_print-sys-info] output of "df -h" follow'
df -h
echo "[_print-sys-info] end of _print-sys-info script"
