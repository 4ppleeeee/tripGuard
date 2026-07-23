/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : getDefaultWay.h
 Author      : warmywang
 Version     : 1.0
 Date        : 16/5/16
 Description : 网关相关方法
 History     : 16/5/16 初始版本
 ***********************************************************/

#pragma once

#include <stdio.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <sys/sysctl.h>

/**
 * 检测ipv4网络下的网关
 *
 * @param addr IPv4地址
 * @return 结果
 */
extern int sp_getdefaultgateway(in_addr_t* addr);
