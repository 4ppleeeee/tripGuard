/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPIPStackCheck.m
 Author      : warmywang
 Version     : 1.0
 Date        : 16/5/16
 Description :
 History     : 16/5/16 初始版本
 ***********************************************************/

#include "SPIPStackCheck.h"
#include "SPGetDefaultWay.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <errno.h>
#include <ctype.h>
#include <string.h>

//尝试连接
static int _test_connect(int pf, struct sockaddr *addr, size_t addrlen) {
    int s = socket(pf, SOCK_DGRAM, IPPROTO_UDP);

    if (s < 0) return 0;

    int ret;

    do {
        ret = connect(s, addr, (socklen_t)addrlen);
    } while (ret < 0 && errno == EINTR);

    int success = (ret == 0);

    do {
        ret = close(s);
    } while (ret < 0 && errno == EINTR);

    return success;
}

static int _have_ipv6() {
    static const struct sockaddr_in6 sin6_test = {

        .sin6_len = sizeof(struct sockaddr_in6),
        .sin6_family = AF_INET6,
        .sin6_port = htons(0xFFFF),
        .sin6_addr.s6_addr = {0x20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    return _test_connect(PF_INET6, (struct sockaddr *)&sin6_test, sizeof(sin6_test));
}

static int _have_ipv4() {
    static const struct sockaddr_in sin_test = {

        .sin_len = sizeof(struct sockaddr_in), .sin_family = AF_INET, .sin_port = htons(0xFFFF), .sin_addr.s_addr = htonl(0x08080808L),
    };

    return _test_connect(PF_INET, (struct sockaddr *)&sin_test, sizeof(sin_test));
}

int sp_local_ipstack_cetect() {
    int have_ipv4 = _have_ipv4();
    int have_ipv6 = _have_ipv6();
    int local_stack = 0;

    if (have_ipv4) {
        local_stack |= SPLocalIPStack_IPv4;
        // QLLogS(@"local_ipstack_cetect ELocalIPStack_IPv4");
    }

    if (have_ipv6) {
        local_stack |= SPLocalIPStack_IPv6;
        SPLOGI(@"ipCheck", @"local_ipstack_cetect ELocalIPStack_IPv6");
    }

    if (SPLocalIPStack_Dual == local_stack) {
        struct in_addr addr_gateway = {0};

        if (0 == sp_getdefaultgateway(&addr_gateway.s_addr)) {
            // QLLogS(@"local_ipstack_cetect getdefaultgateway equal zero");
            return local_stack;
        } else {
            SPLOGI(@"ipCheck", @"local_ipstack_cetect getdefaultgateway no equal zero");
            return SPLocalIPStack_IPv6;
        }
    }
    return local_stack;
}
