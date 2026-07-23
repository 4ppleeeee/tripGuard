/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPIPStackCheck.h
 Author      : warmywang
 Version     : 1.0
 Date        : 16/5/16
 Description : 设备协议栈检测
 History     : 16/5/16 初始版本
 ***********************************************************/
//

#pragma once

#include <stdio.h>

typedef enum {
    SPLocalIPStack_None = 0,
    SPLocalIPStack_IPv4 = 1,
    SPLocalIPStack_IPv6 = 2,
    SPLocalIPStack_Dual = 3,
} SPLocalIPStack;

/**
 * 检查当前设备的网络协议栈支持
 *
 * @return 协议栈支持状态
 */
int sp_local_ipstack_cetect(void);
