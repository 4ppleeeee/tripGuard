/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKMediaPlayerPlugin.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 17/3/3
 Description :
 History     : 17/3/3 初始版本
 ***********************************************************/

#import "TVKMediaPlayerPlugin.h"

@implementation TVKMediaPlayerPlugin

- (instancetype)initWithContext:(TVKMediaPlayerPluginContext *)context {
    if (self = [super init]) {
        self.context = context;
    }

    return self;
}

- (void)load {
}

- (void)unLoad {
}

- (void)onContextUpdated:(TVKMediaPlayerPluginContext *)context key:(NSString *)key {
}

@end
