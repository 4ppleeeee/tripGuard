//
//  getDefaultWay.c
//  QLBaseCore
//
//  Created by warmywang on 16/5/16.
//  Copyright © 2016年 Tencent. All rights reserved.
//

#include "SPGetDefaultWay.h"
//#include <stdio.h>
//#include <netinet/in.h>
//#include <stdlib.h>
//#include <sys/sysctl.h>

#include "TargetConditionals.h"
#if TARGET_IPHONE_SIMULATOR

#if __IPHONE_OS_VERSION_MAX_ALLOWED < 110000
#include <net/route.h>
#endif

#define TypeEN    "en1"
#else

#if !TARGET_OS_MACCATALYST
#include "SPRoute.h"
#else
#include <net/route.h>
#endif

#define TypeEN    "en0"
#endif

#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 110000

#if !TARGET_OS_MACCATALYST
#include "SPRoute.h"
#else
#include <net/route.h>
#endif

#endif

#include <net/if.h>
#include <string.h>
#include <arpa/inet.h>

#define CTL_NET         4               /* network, see socket.h */


#if defined(BSD) || defined(__APPLE__)

#define ROUNDUP(a) \
((a) > 0 ? (1 + (((a) - 1) | (sizeof(long) - 1))) : sizeof(long))

//检测ipv4网络下的网关
int sp_getdefaultgateway(in_addr_t * addr)
{
    int mib[] = {CTL_NET, PF_ROUTE, 0, AF_INET,
        NET_RT_FLAGS, RTF_GATEWAY};
    
    size_t l;
    char * buf, * p;
    struct rt_msghdr * rt;
    struct sockaddr * sa;
    struct sockaddr * sa_tab[RTAX_MAX];
    int i;
    int r = -1;
    if (sysctl(mib, sizeof(mib)/sizeof(int), 0, &l, 0, 0) < 0) {
        return -1;
    }
    
    if (l > 0) {
        buf = malloc(l);
        if(sysctl(mib, sizeof(mib)/sizeof(int), buf, &l, 0, 0) < 0) {
            return -1;
        }
        
        for (p = buf; p < buf + l; p += rt->rtm_msglen) {
            rt = (struct rt_msghdr *)p;
            sa = (struct sockaddr *)(rt + 1);
            for (i = 0; i < RTAX_MAX; i++) {
                if(rt->rtm_addrs & (1 << i)) {
                    sa_tab[i] = sa;
                    sa = (struct sockaddr *)((char *)sa + ROUNDUP(sa->sa_len));
                } else {
                    sa_tab[i] = NULL;
                }
            }
            
            if (((rt->rtm_addrs & (RTA_DST|RTA_GATEWAY)) == (RTA_DST|RTA_GATEWAY))
               && sa_tab[RTAX_DST]->sa_family == AF_INET
               && sa_tab[RTAX_GATEWAY]->sa_family == AF_INET) {
                
                
                if (((struct sockaddr_in *)sa_tab[RTAX_DST])->sin_addr.s_addr == 0) {
                    *addr = ((struct sockaddr_in *)(sa_tab[RTAX_GATEWAY]))->sin_addr.s_addr;
                    r = 0;
                    
#ifdef DEBUG
                    char str[INET_ADDRSTRLEN];
                    inet_ntop(AF_INET, &(((struct sockaddr_in *)(sa_tab[RTAX_GATEWAY]))->sin_addr.s_addr), str, INET_ADDRSTRLEN); // supports IPv6
                    printf("getdefaultgateway IP: %s\n", str); // prints "192.0.2.33"
#endif
                }
            }
        }
        free(buf);
    }
    return r;
}

#endif
